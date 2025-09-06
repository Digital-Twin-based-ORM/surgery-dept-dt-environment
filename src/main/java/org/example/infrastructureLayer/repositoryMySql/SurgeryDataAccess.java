package org.example.infrastructureLayer.repositoryMySql;

import org.example.domain.model.PriorityClass;
import org.example.domain.model.Surgery;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class SurgeryDataAccess {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:6033/wldt-db";
    private static final String USER = "user_name"; // Replace with your MySQL username
    private static final String PASS = "root_password"; // Replace with your MySQL password

    // Table and column names
    private static final String TABLE_NAME = "simulation_table"; // Renamed to avoid hyphen in SQL table names
    private static final String COL_ID = "id";
    private static final String COL_ARRIVAL_DATE = "arrival_date";
    private static final String COL_PROGRAMMED_DATE = "programmed_date";
    private static final String COL_ADMISSION_TIME = "admission_time"; // Note: Use TIMESTAMP or DATETIME for time components
    private static final String COL_PRIORITY = "priority";
    private static final String COL_ESTIMATED_TIME = "estimated_time";
    private static final String COL_PATIENT_ID = "patient_id";
    private static final String COL_SURGERY_ID = "surgery_id";

    public static void main(String[] args) {

        Connection conn = getConnection();
        System.out.println("Connected successfully.");
        try {

            // 3. Create the table if it doesn't exist and clear table
            createTable(conn);
            emptyTable(conn);

            // 4. Insert some sample data
            insertSimulationData(
                    conn,
                    1,
                    LocalDateTime.of(LocalDate.of(2025, 2, 28), LocalTime.of(9,0,0)),
                    LocalDateTime.of(LocalDate.of(2025, 2, 28), LocalTime.of(9,1,0)),
                    LocalDateTime.of(LocalDate.of(2025, 2, 28), LocalTime.of(9,0,40)),
                    0,
                    120,
                    "patient_1",
                    "surgery_1");
            insertSimulationData(
                    conn,
                    2,
                    LocalDateTime.of(LocalDate.of(2025, 2, 28), LocalTime.of(9,0,10)),
                    LocalDateTime.of(LocalDate.of(2025, 2, 28), LocalTime.of(9,1,0)),
                    LocalDateTime.of(LocalDate.of(2025, 2, 28), LocalTime.of(9,0,20)),
                    0,
                    120,
                    "patient_2",
                    "surgery_1");

            // 5. Retrieve and display data
            System.out.println("\nRetrieving simulation data:");
            retrieveSimulationData(conn);

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

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // 1. Register the JDBC driver (not strictly necessary for modern JDBC, but good practice)
            // Class.forName("com.mysql.cj.jdbc.Driver"); // For MySQL 8.x and later

            System.out.println("Connecting to database...");
            // 2. Open a connection
            return conn = DriverManager.getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the simulation_table if it does not already exist.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void createTable(Connection conn) throws SQLException {
        // Using `IF NOT EXISTS` ensures the table is only created once.
        // Note: Using `DATETIME` for `admission_time` to include time component.
        // If you only need date without time, `DATE` is fine for all date fields.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INT PRIMARY KEY," +
                COL_ARRIVAL_DATE + " DATETIME," +
                COL_PROGRAMMED_DATE + " DATETIME," +
                COL_ADMISSION_TIME + " DATETIME," + // Changed to DATETIME for better time representation
                COL_PRIORITY + " INT," +
                COL_ESTIMATED_TIME + " INT," +
                COL_PATIENT_ID + " TEXT," +
                COL_SURGERY_ID + " TEXT" +
                ")";
        try (PreparedStatement pstmt = conn.prepareStatement(createTableSQL)) {
            pstmt.executeUpdate();
            System.out.println("Table '" + TABLE_NAME + "' created or already exists.");
        }
    }

    /**
     * Inserts a new row of simulation data into the table.
     *
     * @param conn The database connection.
     * @param id The simulation ID.
     * @param arrivalDate The arrival date.
     * @param programmedDate The programmed date.
     * @param admissionTime The admission time (using LocalDate for simplicity, but consider LocalDateTime for time).
     * @param priority The priority level.
     * @throws SQLException If a database access error occurs.
     */
    private static void insertSimulationData(Connection conn, int id, LocalDateTime arrivalDate,
                                             LocalDateTime programmedDate, LocalDateTime admissionTime, int priority, int estimatedTime, String patientId, String surgery_id) throws SQLException {
        String insertSQL = "INSERT INTO " + TABLE_NAME + " (" +
                COL_ID + ", " + COL_ARRIVAL_DATE + ", " + COL_PROGRAMMED_DATE + ", " +
                COL_ADMISSION_TIME + ", " + COL_PRIORITY + ", " + COL_ESTIMATED_TIME + ", " + COL_PATIENT_ID + ", " + COL_SURGERY_ID + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, id);
            pstmt.setTimestamp(2, Timestamp.valueOf(arrivalDate)); // Convert LocalDate to java.sql.Date
            pstmt.setTimestamp(3, Timestamp.valueOf(programmedDate));
            pstmt.setTimestamp(4, Timestamp.valueOf(admissionTime)); // If using DATETIME, convert to java.sql.Timestamp
            pstmt.setInt(5, priority);
            pstmt.setInt(6, estimatedTime);
            pstmt.setString(7, patientId);
            pstmt.setString(8, surgery_id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Data inserted for ID: " + id);
            } else {
                System.out.println("Failed to insert data for ID: " + id + " (might be duplicate ID)");
            }
        }
    }

    /**
     * Retrieves and prints all simulation data from the table.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    public static ArrayList<Surgery> retrieveSimulationData(Connection conn) throws SQLException {
        String selectSQL = "SELECT " + COL_ID + ", " + COL_ARRIVAL_DATE + ", " + COL_PROGRAMMED_DATE + ", " +
                COL_ADMISSION_TIME + ", " + COL_PRIORITY + ", " + COL_ESTIMATED_TIME + " FROM " + TABLE_NAME;
        ArrayList<Surgery> surgeries = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            // Iterate through the result set and print data
            while (rs.next()) {
                String id = rs.getInt(COL_ID) + "";
                LocalDateTime arrivalDate = rs.getTimestamp(COL_ARRIVAL_DATE).toLocalDateTime();
                LocalDateTime programmedDate = rs.getTimestamp(COL_PROGRAMMED_DATE).toLocalDateTime();
                LocalDateTime admissionTime = rs.getTimestamp(COL_ADMISSION_TIME).toLocalDateTime(); // Or rs.getTimestamp for DATETIME
                int priority = rs.getInt(COL_PRIORITY);
                int estimated_time = rs.getInt(COL_ESTIMATED_TIME);

                System.out.printf("ID: %s, Arrival: %s, Programmed: %s, Admission: %s, Priority: %d%n",
                        id, arrivalDate, programmedDate, admissionTime, priority);
                 surgeries.add(new Surgery(id, arrivalDate, programmedDate, admissionTime, LocalDateTime.now(), PriorityClass.values()[priority], estimated_time));
            }
        }
        return surgeries;
    }

    /**
     * Empties all data from the simulation_table.
     * TRUNCATE TABLE is generally faster than DELETE FROM for emptying an entire table
     * as it resets the auto-increment counter and does not log individual row deletions.
     *
     * @param conn The database connection.
     * @throws SQLException If a database access error occurs.
     */
    private static void emptyTable(Connection conn) throws SQLException {
        String truncateSQL = "TRUNCATE TABLE " + TABLE_NAME;
        try (PreparedStatement pstmt = conn.prepareStatement(truncateSQL)) {
            pstmt.executeUpdate();
            System.out.println("Table '" + TABLE_NAME + "' emptied successfully.");
        }
    }
}
