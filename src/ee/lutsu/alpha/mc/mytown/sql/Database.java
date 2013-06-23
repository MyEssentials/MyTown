
package ee.lutsu.alpha.mc.mytown.sql;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import ee.lutsu.alpha.mc.mytown.Log;
import ee.lutsu.alpha.mc.mytown.MyTown;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Database {

    public enum Type {
        MySQL("mysql.jar"),
        SQLite("sqlite.jar"),
        NONE("nil");

        private String driver;

        Type(String driver) {
            this.driver = driver;
        }

        public String getDriver() {
            return driver;
        }

        /**
         * Match the given string to a database type
         *
         * @param str
         * @return
         */
        public static Type matchType(String str) {
            for (Type type : values()) {
                if (type.toString().equalsIgnoreCase(str)) {
                    return type;
                }
            }

            return null;
        }

    }

    /**
     * The database prefix (only if we're using MySQL.)
     */
    public String prefix = "";
    /**
     * The database engine being used for this connection
     */
    public Type currentType = Type.NONE;
    public String username = "";
    public String password = "";
    
    public String host = "";
    public String dbname = "";
    public String dbpath = "";

    /**
     * Store cached prepared statements.
     * <p/>
     * Since SQLite JDBC doesn't cache them.. we do it ourselves :S
     */
    private Map<String, PreparedStatement> statementCache = new HashMap<String, PreparedStatement>();

    /**
     * The connection to the database
     */
    protected Connection connection = null;

    /**
     * The default database engine being used. This is set via config
     *
     * @default SQLite
     */
    public static Type DefaultType = Type.NONE;

    /**
     * If we are connected to sqlite
     */
    private boolean connected = false;

    /**
     * If the database has been loaded
     */
    protected boolean loaded = false;

    /**
     * If the high level statement cache should be used. If this is false, already cached statements are ignored
     */
    private boolean useStatementCache = true;

    /**
     * Set the value of auto commit
     *
     * @param autoCommit
     * @return TRUE if successful, FALSE if exception was thrown
     */
    public boolean setAutoCommit(boolean autoCommit) {
        try {
            // Commit the database if we are setting auto commit back to true
            if (autoCommit) {
                connection.commit();
            }

            connection.setAutoCommit(autoCommit);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return the table prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Print an exception to stdout
     *
     * @param exception
     */
    protected void printException(Exception exception) {
        // check for disconnect
        if (exception instanceof CommunicationsException) {
            // reconnect!
            try {
                connect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return;
        }

        throw new RuntimeException(exception);
    }

    /**
     * Connect to MySQL
     *
     * @return if the connection was successful
     */
    public boolean connect() throws Exception {
        if (connection != null)
            return true;

        if (currentType == null || currentType == Type.NONE)
        	throw new Exception("Unknown Connection type");
        
        // What class should we try to load?
        String className = "";
        if (currentType == Type.MySQL) {
            className = "com.mysql.jdbc.Driver";
        } else {
            className = "org.sqlite.JDBC";
        }

        Driver driver;
        
        // Load the database jar,Load the driver class
    	driver = (Driver)Class.forName(className).newInstance();

        // Create the properties to pass to the driver
        Properties properties = new Properties();

        // if we're using mysql, append the database info
        if (currentType == Type.MySQL) {
            properties.put("autoReconnect", "true");
            properties.put("user", username);
            properties.put("password", password);
        }

        // Connect to the database
        connection = driver.connect("jdbc:" + currentType.toString().toLowerCase() + ":" + getDatabasePath(), properties);
        if (connection == null)
        	throw new NullPointerException("Connecting to database failed: unknown error - using jdbc:" + currentType.toString().toLowerCase() + ":" + getDatabasePath());
        
        connected = true;
        return true;
    }

    public void dispose() {
        statementCache.clear();

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        connection = null;
    }

    /**
     * @return the connection to the database
     */
    public Connection getConnection() {
    	if (connection == null)
    		throw new NullPointerException("No connection!");
    	
        return connection;
    }

    /**
     * @return the path where the database file should be saved
     */
    public String getDatabasePath() {
        if (currentType == Type.MySQL)
            return "//" + host + "/" + dbname;
        else
        	return dbpath;
    }

    /**
     * @return the database engine type
     */
    public Type getType() {
        return currentType;
    }

    /**
     * Load the database
     */
    public abstract void load();

    /**
     * Prepare a statement unless it's already cached (and if so, just return it)
     *
     * @param sql
     * @return
     */
    public PreparedStatement prepare(String sql) throws SQLException {
        return prepare(sql, false);
    }

    /**
     * Prepare a statement unless it's already cached (and if so, just return it)
     *
     * @param sql
     * @param returnGeneratedKeys
     * @return
     * @throws SQLException 
     */
    public PreparedStatement prepare(String sql, boolean returnGeneratedKeys) throws SQLException {
        if (connection == null)
        	throw new SQLException("No connection");

        if (useStatementCache && statementCache.containsKey(sql))
            return statementCache.get(sql);

        PreparedStatement preparedStatement = returnGeneratedKeys ?
        	connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) :
        	connection.prepareStatement(sql);

        statementCache.put(sql, preparedStatement);

        return preparedStatement;
    }

    /**
     * Add a column to a table
     *
     * @param table
     * @param column
     */
    public boolean addColumn(String table, String column, String type) {
        return executeUpdateNoException("ALTER TABLE " + table + " ADD " + column + " " + type);
    }

    /**
     * Add a column to a table
     *
     * @param table
     * @param column
     */
    public boolean dropColumn(String table, String column) {
        return executeUpdateNoException("ALTER TABLE " + table + " DROP COLUMN " + column);
    }

    /**
     * Rename a table
     *
     * @param table
     * @param newName
     */
    public boolean renameTable(String table, String newName) {
        return executeUpdateNoException("ALTER TABLE " + table + " RENAME TO " + newName);
    }

    /**
     * Drop a table
     *
     * @param table
     */
    public boolean dropTable(String table) {
        return executeUpdateNoException("DROP TABLE " + table);
    }

    /**
     * Execute an update, ignoring any exceptions
     *
     * @param query
     * @return true if an exception was thrown
     */
    public boolean executeUpdateNoException(String query) {
        Statement statement = null;
        boolean exception = false;

        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            exception = true;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
            }
        }

        return exception;
    }

    /**
     * @return true if connected to the database
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Returns true if the high level statement cache should be used. If this is false, already cached statements are ignored
     *
     * @return
     */
    public boolean useStatementCache() {
        return useStatementCache;
    }

    /**
     * Set if the high level statement cache should be used.
     *
     * @param useStatementCache
     * @return
     */
    public void setUseStatementCache(boolean useStatementCache) {
        this.useStatementCache = useStatementCache;
    }

}
