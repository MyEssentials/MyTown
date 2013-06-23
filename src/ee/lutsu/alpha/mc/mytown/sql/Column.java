package ee.lutsu.alpha.mc.mytown.sql;

class Column {

    /**
     * If the table should auto increment. Note: This is automatically set for SQLite with:
     * <p>
     * table.setPrimary(true);
     * </p>
     */
    private boolean autoIncrement = false;

    /**
     * The column name
     */
    private String name;

    /**
     * If this column is the primary column
     */
    private boolean primary = false;

    /**
     * The table this column is assigned to
     */
    private Table table;

    /**
     * The column type (INTEGER, BLOB, etc)
     */
    private String type;

    /**
     * The default value
     */
    private String defaultValue;

    public Column(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue == null ? "" : defaultValue;
    }

    public String getType() {
        return type;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
        autoIncrement = primary;
    }

    public void setTable(Table table) {
        if (this.table == null) {
            this.table = table;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean shouldAutoIncrement() {
        return autoIncrement;
    }
}
