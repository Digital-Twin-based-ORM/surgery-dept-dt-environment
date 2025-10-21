package org.example.digitalAdapter.sql;

public class KpiRepositoryConfiguration {
    private final String dtName;
    private final String dtId;
    private final String connectionUrl;
    private final String user;
    private final String pass;
    private final String dbName;
    private final String dbSurgeryName;
    private final String turnOverTimeTableName;

    public String getTurnOverTimeTableName() {
        return turnOverTimeTableName;
    }

    public String getDbName() {
        return dbName;
    }

    private final String dbDepartmentName;

    public String getDbSurgeryName() {
        return dbSurgeryName;
    }

    public String getDbDepartmentName() {
        return dbDepartmentName;
    }

    public KpiRepositoryConfiguration(String dtName, String dtId, String connectionUrl, String user, String pass, String dbName, String dbSurgeryName, String turnOverTimeTableName, String dbDepartmentName) {
        this.dtName = dtName;
        this.dtId = dtId;
        this.connectionUrl = connectionUrl;
        this.user = user;
        this.pass = pass;
        this.dbName = dbName;
        this.dbSurgeryName = dbSurgeryName;
        this.turnOverTimeTableName = turnOverTimeTableName;
        this.dbDepartmentName = dbDepartmentName;
    }

    public String getDtName() {
        return dtName;
    }

    public String getDtId() {
        return dtId;
    }

    /**
     *
     * @return the url in the format of host:port (eg. 127.0.0.1:8080)
     */
    public String getConnectionUrl() {
        return connectionUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return pass;
    }
}
