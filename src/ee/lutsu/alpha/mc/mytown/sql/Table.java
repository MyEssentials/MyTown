package ee.lutsu.alpha.mc.mytown.sql;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ee.lutsu.alpha.mc.mytown.sql.Database.Type;

public class Table {

    /**
     * The columns in the table
     */
    private List<Column> columns;

    /**
     * The database object associated with this table
     */
    private Database database;

    /**
     * If this table is to be stored in memory
     */
    private boolean memory;

    /**
     * The table's name
     */
    private String name;

    public Table(Database database, String name) {
        this.database = database;
        this.name = name;

        columns = new ArrayList<Column>();
    }

    /**
     * Add a column to the table
     *
     * @param column
     */
    public void add(Column column) {
        column.setTable(this);

        columns.add(column);
    }
    
    /**
     * Add a column to the table
     * 
     * @param Column name
     * @param Column data type
     */
    public void add(String name, String type)
    {
    	add(name, type, false, false);
    }
    
    /**
     * Add a column to the table
     * 
     * @param Column name
     * @param Column data type
     * @param Set as primary key?
     * @param Set as auto increment?
     */
    public void add(String name, String type, boolean primary, boolean autoIncrement)
    {
    	Column x = new Column(name);
    	x.setType(type);
    	x.setPrimary(primary);
    	x.setAutoIncrement(autoIncrement);
    	add(x);
    }

    /**
     * Create the table
     */
    public void execute() {
        StringBuilder buffer = new StringBuilder("CREATE TABLE IF NOT EXISTS ");

        String prefix = this.database.prefix;

        // the table name
        buffer.append(prefix).append(name);
        buffer.append(" ( ");

        // add the columns
        for (int index = 0; index < columns.size(); index++) {
            Column column = columns.get(index);

            buffer.append(column.getName());
            buffer.append(" ");
            buffer.append(column.getType());
            buffer.append(" ");

            if (column.isPrimary()) {
                buffer.append("PRIMARY KEY ");
            }

            if (column.shouldAutoIncrement() && database.getType() == Type.MySQL) {
                buffer.append("AUTO_INCREMENT ");
            }

            if (!column.getDefaultValue().isEmpty()) {
                buffer.append("DEFAULT ");
                buffer.append(column.getDefaultValue());
                buffer.append(" ");
            }

            // check if there's more columns in the stack
            if (index != (columns.size() - 1)) {
                buffer.append(",");
                buffer.append(" ");
            }
        }

        // finalize
        buffer.append(" ) ");

        // if we're using mysql, check if we're in memory
        if (memory && database.getType() == Type.MySQL) {
            buffer.append("ENGINE = MEMORY");
        }

        // end it
        buffer.append(";");

        // execute it directly to the database
        Statement statement = null;
        try {
            statement = database.getConnection().createStatement();
            statement.executeUpdate(buffer.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
     * @return
     */
    public boolean isInMemory() {
        return memory;
    }

    /**
     * Set if the table is in memory
     *
     * @param memory
     */
    public void setMemory(boolean memory) {
        this.memory = memory;
    }

}
