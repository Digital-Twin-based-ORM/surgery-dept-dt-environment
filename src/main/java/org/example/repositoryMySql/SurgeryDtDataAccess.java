package org.example.repositoryMySql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date; // For java.sql.Date (for arrival_date, programmed_date, estimatedSurgeryDate)
import java.sql.Timestamp; // For java.sql.Timestamp (for admission_time)
import java.sql.Types; // Import for SQL data types
import java.time.LocalDate; // For modern date handling
import java.time.LocalDateTime; // For modern date and time handling
import java.time.LocalTime; // For specifying time components

public class SurgeryDtDataAccess {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/wldt-db";
    private static final String USER = "your_mysql_username"; // Replace with your MySQL username
    private static final String PASS = "your_mysql_password"; // Replace with your MySQL password

    // --- Simulation Table Details ---
    private static final String SIMULATION_TABLE_NAME = "simulation_table"; // Renamed to avoid hyphen in SQL table names
    private static final String SIM_COL_ID = "id";
    private static final String SIM_COL_ARRIVAL_DATE = "arrival_date";
    private static final String SIM_COL_PROGRAMMED_DATE = "programmed_date";
    private static final String SIM_COL_ADMISSION_TIME = "admission_time"; // Mapped to DATETIME in DB
    private static final String SIM_COL_PRIORITY = "priority";

    // --- Surgery DTS Table Details ---
    private static final String SURGERY_TABLE_NAME = "surgery_dts";
    private static final String SURG_COL_REASON = "reason";
    private static final String SURG_COL_CATEGORY = "category";
    private static final String SURG_COL_CODE = "code";
    private static final String SURG_COL_ESTIMATED_SURGERY_DATE = "estimated_surgery_date";


    public static void main(String[] args) {
        // --- IMPORTANT: Ensure you have the MySQL JDBC Driver in your project's classpath ---
        // If you are using Maven, add this to your pom.xml:
        /*
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version> <!-- Use the latest compatible version -->
        </dependency>
        */
        // If not using Maven, download the JAR from MySQL website and add it to your project's build path.

        Connection conn = null;
        try {
            // 1. Register the JDBC driver (not strictly necessary for modern JDBC, but good practice)
            // Class.forName("com.mysql.cj.jdbc.Driver"); // For MySQL 8.x and later

            System.out.println("Connecting to database...");
            // 2. Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected successfully.");

            // --- Operations for simulation_table ---
            System.out.println("\n--- SIMULATION TABLE OPERATIONS ---");
            createSimulationTable(conn);
            System.out.println("\nAttempting to empty the simulation table...");
            emptySimulationTable(conn);

            // Get current local date
            LocalDate today = LocalDate.now();
            // Manually set a specific time for the admission (e.g., 10:30:00)
            LocalTime specificAdmissionTime = LocalTime.of(10, 30, 0);
            // Combine current date with specific time
            LocalDateTime admissionDateTime1 = LocalDateTime.of(today, specificAdmissionTime);

            // You can also get the current date and time directly:
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Insert some sample data using LocalDateTime for admission_time
            insertSimulationData(conn, 1, LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 15), admissionDateTime1, 1);
            insertSimulationData(conn, 2, LocalDate.of(2025, 6, 12), LocalDate.of(2025, 6, 18), currentDateTime, 2);
            insertSimulationData(conn, 3, LocalDate.of(2025, 6, 11), LocalDate.of(2025, 6, 16), LocalDateTime.of(2025, 6, 16, 14, 0, 0), 3);
            insertSimulationData(conn, 4, null, LocalDate.of(2025, 6, 20), LocalDateTime.of(2025, 6, 20, 9, 0, 0), 4);


            System.out.println("\nRetrieving simulation data:");
            retrieveSimulationData(conn);

            // --- Operations for surgery_dts table ---
            System.out.println("\n--- SURGERY DTS TABLE OPERATIONS ---");
            createSurgeryTable(conn);
            System.out.println("\nAttempting to empty the surgery DTS table...");
            emptySurgeryTable(conn);

            // Insert some sample data into surgery_dts
            insertSurgeryData(conn, "Appendix Removal", "Emergency", "S001", LocalDate.of(2025, 7, 1));
            insertSurgeryData(conn, "Knee Replacement", "Elective", "S002", LocalDate.of(2025, 9, 10));
            insertSurgeryData(conn, "Gallbladder Surgery", "Urgent", "S003", LocalDate.of(2025, 7, 25));
            insertSurgeryData(conn, "Cataract Surgery", "Elective", "S004", null); // Example with null date

            System.out.println("\nRetrieving surgery DTS data:");
            retrieveSurgeryData(conn);


        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // 6. Close resources
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        System.out.println("\nProgram finished.");
    }

    /**
     * Creates the simulation_table if it does not already exist.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void createSimulationTable(Connection conn) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + SIMULATION_TABLE_NAME + " (" +
                SIM_COL_ID + " INT PRIMARY KEY," +
                SIM_COL_ARRIVAL_DATE + " DATE," +
                SIM_COL_PROGRAMMED_DATE + " DATE," +
                SIM_COL_ADMISSION_TIME + " DATETIME," +
                SIM_COL_PRIORITY + " INT" +
                ")";
        try (PreparedStatement pstmt = conn.prepareStatement(createTableSQL)) {
            pstmt.executeUpdate();
            System.out.println("Table '" + SIMULATION_TABLE_NAME + "' created or already exists.");
        }
    }

    /**
     * Inserts a new row of simulation data into the table.
     *
     * @param conn The database connection.
     * @param id The simulation ID.
     * @param arrivalDate The arrival date.
     * @param programmedDate The programmed date.
     * @param admissionTime The admission date and time.
     * @param priority The priority level.
     * @throws SQLException If a database access error occurs.
     */
    private static void insertSimulationData(Connection conn, int id, LocalDate arrivalDate,
                                             LocalDate programmedDate, LocalDateTime admissionTime, int priority) throws SQLException {
        String insertSQL = "INSERT INTO " + SIMULATION_TABLE_NAME + " (" +
                SIM_COL_ID + ", " + SIM_COL_ARRIVAL_DATE + ", " + SIM_COL_PROGRAMMED_DATE + ", " +
                SIM_COL_ADMISSION_TIME + ", " + SIM_COL_PRIORITY + ") VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, id);

            if (arrivalDate != null) {
                pstmt.setDate(2, Date.valueOf(arrivalDate));
            } else {
                pstmt.setNull(2, Types.DATE);
            }

            if (programmedDate != null) {
                pstmt.setDate(3, Date.valueOf(programmedDate));
            } else {
                pstmt.setNull(3, Types.DATE);
            }

            if (admissionTime != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(admissionTime));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }

            pstmt.setInt(5, priority);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted into " + SIMULATION_TABLE_NAME + " for ID: " + id);
            } else {
                System.out.println("Failed to insert data into " + SIMULATION_TABLE_NAME + " for ID: " + id + " (might be duplicate ID)");
            }
        }
    }

    /**
     * Empties all data from the simulation_table.
     * TRUNCATE TABLE is generally faster than DELETE FROM for emptying an entire table
     * as it resets the auto-increment counter and does not log individual row deletions.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void emptySimulationTable(Connection conn) throws SQLException {
        String truncateSQL = "TRUNCATE TABLE " + SIMULATION_TABLE_NAME;
        try (PreparedStatement pstmt = conn.prepareStatement(truncateSQL)) {
            pstmt.executeUpdate();
            System.out.println("Table '" + SIMULATION_TABLE_NAME + "' emptied successfully.");
        }
    }

    /**
     * Retrieves and prints all simulation data from the table.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void retrieveSimulationData(Connection conn) throws SQLException {
        String selectSQL = "SELECT " + SIM_COL_ID + ", " + SIM_COL_ARRIVAL_DATE + ", " + SIM_COL_PROGRAMMED_DATE + ", " +
                SIM_COL_ADMISSION_TIME + ", " + SIM_COL_PRIORITY + " FROM " + SIMULATION_TABLE_NAME;
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            // Iterate through the result set and print data
            while (rs.next()) {
                int id = rs.getInt(SIM_COL_ID);
                Date arrivalDate = rs.getDate(SIM_COL_ARRIVAL_DATE);
                Date programmedDate = rs.getDate(SIM_COL_PROGRAMMED_DATE);
                Timestamp admissionTimestamp = rs.getTimestamp(SIM_COL_ADMISSION_TIME);
                int priority = rs.getInt(SIM_COL_PRIORITY);

                LocalDateTime admissionDateTime = admissionTimestamp != null ? admissionTimestamp.toLocalDateTime() : null;


                System.out.printf("Sim ID: %d, Arrival: %s, Programmed: %s, Admission: %s, Priority: %d%n",
                        id, arrivalDate, programmedDate, admissionDateTime, priority);
            }
        }
    }

    // --- New Methods for Surgery DTS Table ---

    /**
     * Creates the surgery_dts table if it does not already exist.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void createSurgeryTable(Connection conn) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + SURGERY_TABLE_NAME + " (" +
                SURG_COL_REASON + " VARCHAR(255) NOT NULL," + // Assuming reason cannot be null
                SURG_COL_CATEGORY + " VARCHAR(100)," +
                SURG_COL_CODE + " VARCHAR(50) PRIMARY KEY," + // Assuming code is unique and acts as PK
                SURG_COL_ESTIMATED_SURGERY_DATE + " DATE" +
                ")";
        try (PreparedStatement pstmt = conn.prepareStatement(createTableSQL)) {
            pstmt.executeUpdate();
            System.out.println("Table '" + SURGERY_TABLE_NAME + "' created or already exists.");
        }
    }

    /**
     * Inserts a new row of surgery data into the surgery_dts table.
     *
     * @param conn The database connection.
     * @param reason The reason for surgery.
     * @param category The category of surgery.
     * @param code The unique surgery code.
     * @param estimatedSurgeryDate The estimated date of the surgery.
     * @throws SQLException If a database access error occurs.
     */
    private static void insertSurgeryData(Connection conn, String reason, String category,
                                          String code, LocalDate estimatedSurgeryDate) throws SQLException {
        String insertSQL = "INSERT INTO " + SURGERY_TABLE_NAME + " (" +
                SURG_COL_REASON + ", " + SURG_COL_CATEGORY + ", " +
                SURG_COL_CODE + ", " + SURG_COL_ESTIMATED_SURGERY_DATE + ") VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, reason);
            pstmt.setString(2, category);
            pstmt.setString(3, code);

            // Handle potential null LocalDate for estimatedSurgeryDate
            if (estimatedSurgeryDate != null) {
                pstmt.setDate(4, Date.valueOf(estimatedSurgeryDate));
            } else {
                pstmt.setNull(4, Types.DATE);
            }

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted into " + SURGERY_TABLE_NAME + " for Code: " + code);
            } else {
                System.out.println("Failed to insert data into " + SURGERY_TABLE_NAME + " for Code: " + code + " (might be duplicate CODE)");
            }
        }
    }

    /**
     * Empties all data from the surgery_dts table.
     * TRUNCATE TABLE is used for efficient clearing.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void emptySurgeryTable(Connection conn) throws SQLException {
        String truncateSQL = "TRUNCATE TABLE " + SURGERY_TABLE_NAME;
        try (PreparedStatement pstmt = conn.prepareStatement(truncateSQL)) {
            pstmt.executeUpdate();
            System.out.println("Table '" + SURGERY_TABLE_NAME + "' emptied successfully.");
        }
    }

    /**
     * Retrieves and prints all surgery data from the surgery_dts table.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void retrieveSurgeryData(Connection conn) throws SQLException {
        String selectSQL = "SELECT " + SURG_COL_REASON + ", " + SURG_COL_CATEGORY + ", " +
                SURG_COL_CODE + ", " + SURG_COL_ESTIMATED_SURGERY_DATE + " FROM " + SURGERY_TABLE_NAME;
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String reason = rs.getString(SURG_COL_REASON);
                String category = rs.getString(SURG_COL_CATEGORY);
                String code = rs.getString(SURG_COL_CODE);
                Date estimatedSurgeryDate = rs.getDate(SURG_COL_ESTIMATED_SURGERY_DATE);

                System.out.printf("Surgery Code: %s, Reason: %s, Category: %s, Estimated Date: %s%n",
                        code, reason, category, estimatedSurgeryDate);
            }
        }
    }
}
