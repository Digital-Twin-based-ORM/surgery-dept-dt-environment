package org.example.infrastructureLayer.persistence.repository;

import org.example.businessLayer.boundaries.KpiDataSourceGateway;
import org.example.digitalAdapter.sql.KpiRepositoryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class KpiDataSourceGatewayImpl implements KpiDataSourceGateway {
    public static final String COL_ID = "id";
    public static final String COL_KPI_TYPE = "kpi";
    public static final String COL_OR_ID = "operating_room_id";
    public static final String COL_SURGERY_ID = "surgery_id";
    public static final String COL_VALUE = "value";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_PERCENTAGE = "percentage";
    public static final String COL_SURGERY_TYPE = "surgeryType";
    private final Logger logger = LoggerFactory.getLogger(KpiDataSourceGatewayImpl.class);
    private KpiRepositoryConfiguration configuration;
    private Connection conn;

    public KpiDataSourceGatewayImpl(KpiRepositoryConfiguration kpiRepositoryConfiguration) throws SQLException {
        this.configuration = kpiRepositoryConfiguration;
        String url = String.format("jdbc:mysql://%s/%s", this.configuration.getConnectionUrl(), this.configuration.getDbName());
        this.conn = DriverManager.getConnection(url, this.configuration.getUser(), this.configuration.getPassword());

        if(!this.checkTableExist(configuration.getDbDepartmentName())) {
            logger.info("Creo la tabella operating_rooms_kpi");
            this.createDepartmentTable(configuration.getDbDepartmentName());
        }
        if(!this.checkTableExist(configuration.getDbSurgeryName())) {
            logger.info("Creo la tabella surgeries_kpi");
            this.createSurgeryTable(configuration.getDbSurgeryName());
        }
        if(!this.checkTableExist(configuration.getTurnOverTimeTableName())) {
            logger.info("Creo la tabella surgeries_kpi");
            this.createTurnoverTimeTable(configuration.getTurnOverTimeTableName());
        }
    }

    public int createDatabase(String dbName, String tableName) {
        try {
            Statement s = this.conn.createStatement();
            String sqlQuery = "CREATE DATABASE ?";
            PreparedStatement statement = conn.prepareStatement(sqlQuery);
            statement.setString(1, dbName);
            return statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Cannot create the database: " + e);
            throw new RuntimeException(e);
        }
    }

    public void createDepartmentTable(String tableName) {
// Using `IF NOT EXISTS` ensures the table is only created once.
        // Note: Using `DATETIME` for `admission_time` to include time component.
        // If you only need date without time, `DATE` is fine for all date fields.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                COL_ID + " INT PRIMARY KEY AUTO_INCREMENT," +
                COL_KPI_TYPE + " TEXT," +
                COL_OR_ID + " TEXT," +
                COL_VALUE + " DOUBLE,"+
                COL_TIMESTAMP + " TEXT" +
                ")";
        try (PreparedStatement pstmt = this.conn.prepareStatement(createTableSQL)) {
            pstmt.executeUpdate();
            logger.info("Table '" + tableName + "' created or already exists.");
        } catch (Exception e) {
            logger.error("Error while creating the table: " + e.getMessage());
        }
    }

    public void createSurgeryTable(String tableName) {
        // Using `IF NOT EXISTS` ensures the table is only created once.
        // Note: Using `DATETIME` for `admission_time` to include time component.
        // If you only need date without time, `DATE` is fine for all date fields.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                COL_ID + " INT PRIMARY KEY AUTO_INCREMENT," +
                COL_KPI_TYPE + " TEXT," +
                COL_SURGERY_ID + " TEXT," +
                COL_SURGERY_TYPE + " TEXT," +
                COL_VALUE + " FLOAT,"+
                COL_TIMESTAMP + " TEXT" +
                ")";
        try (PreparedStatement pstmt = this.conn.prepareStatement(createTableSQL)) {
            pstmt.executeUpdate();
            logger.info("Table '" + tableName + "' created or already exists.");
        } catch (Exception e) {
            logger.error("Error while creating the table: " + e.getMessage());
        }
    }

    public void createTurnoverTimeTable(String tableName) {
        // Using `IF NOT EXISTS` ensures the table is only created once.
        // Note: Using `DATETIME` for `admission_time` to include time component.
        // If you only need date without time, `DATE` is fine for all date fields.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                COL_ID + " INT PRIMARY KEY AUTO_INCREMENT," +
                COL_PERCENTAGE + " FLOAT,"+
                COL_TIMESTAMP + " TEXT" +
                ")";
        try (PreparedStatement pstmt = this.conn.prepareStatement(createTableSQL)) {
            pstmt.executeUpdate();
            logger.info("Table '" + tableName + "' created or already exists.");
        } catch (Exception e) {
            logger.error("Error while creating the table: " + e.getMessage());
        }
    }

    @Override
    public boolean addOperatingRoomKpiRecord(String id, String type, float value, LocalDateTime timestamp) {
        String idColumnName = COL_OR_ID;
        String tableName = this.configuration.getDbDepartmentName();

        String insertSQL = "INSERT INTO " + tableName + " (" +
                COL_KPI_TYPE + ", " + idColumnName + ", " +
                COL_VALUE + ", " + COL_TIMESTAMP + ") VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = this.conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, type);
            pstmt.setString(2, id);
            pstmt.setFloat(3, value);
            pstmt.setTimestamp(4, Timestamp.valueOf(timestamp));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Data inserted for: " + id);
                return true;
            } else {
                logger.info("Failed to insert data for: " + id + " (might be duplicate ID)");
                return false;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addSurgeryKpiRecord(String id, String type, String surgeryType, float value, LocalDateTime timestamp) {
        String idColumnName = COL_SURGERY_ID;
        String tableName = this.configuration.getDbSurgeryName();

        String insertSQL = "INSERT INTO " + tableName + " (" +
                COL_KPI_TYPE + ", " + idColumnName + ", " + COL_SURGERY_TYPE + ", " +
                COL_VALUE + ", " + COL_TIMESTAMP + ") VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = this.conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, type);
            pstmt.setString(2, id);
            pstmt.setString(3, surgeryType);
            pstmt.setFloat(4, value);
            pstmt.setTimestamp(5, Timestamp.valueOf(timestamp));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Data inserted for: " + id);
                return true;
            } else {
                logger.info("Failed to insert data for: " + id + " (might be duplicate ID)");
                return false;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addM22Kpi(float percentage, LocalDate date) {
        String insertSQL = "INSERT INTO " + configuration.getTurnOverTimeTableName() + " (" +
                COL_PERCENTAGE + ", " + COL_TIMESTAMP + ") VALUES (?, ?)";
        try (PreparedStatement pstmt = this.conn.prepareStatement(insertSQL)) {
            pstmt.setFloat(1, percentage);
            pstmt.setDate(2, Date.valueOf(date));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("New M22 record inserted");
                return true;
            } else {
                logger.info("Failed to insert new M22 record");
                return false;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean checkTableExist(String tableName) {
        try {
            DatabaseMetaData dbm = this.conn.getMetaData();
            // check if a table is there
            ResultSet tables = dbm.getTables(null, null, tableName, null);
            if (tables.next()) {
                // Table exists
                return true;
            }
            else {
                // Table does not exist
                return false;
            }
        } catch (SQLException e) {
            logger.error("Cannot connect to the database: " + e);
            throw new RuntimeException(e);
        }
    }
}
