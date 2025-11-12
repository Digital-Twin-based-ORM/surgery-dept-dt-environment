package org.example.infrastructureLayer.persistence;

import org.example.businessLayer.adapter.OperatingRoomInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class OperatingRoomsDb {
    // Dettagli per la connessione al database
    private static final String JDBC_URL = "jdbc:mysql://localhost:6033/wldt-db";
    private static final String USER = "user_name";
    private static final String PASSWORD = "root_password";

    public static void createTable(List<OperatingRoomInfo> operatingRooms) {
        System.out.println("Avvio della creazione della tabella e inserimento dati...");

        // Il blocco try-with-resources garantisce che le risorse vengano chiuse automaticamente
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("Connessione al database riuscita.");

            // SQL per creare la tabella OperatingRooms
            String createTableSql = "CREATE TABLE IF NOT EXISTS OperatingRooms ("
                    + "id_string VARCHAR(255) NOT NULL PRIMARY KEY,"
                    + "uri_string VARCHAR(255) NOT NULL"
                    + ")";

            // Esegui la query per creare la tabella
            statement.execute(createTableSql);
            System.out.println("Tabella 'OperatingRooms' creata con successo (se non esisteva già).");

            // SQL per popolare la tabella
            for(OperatingRoomInfo or : operatingRooms) {
                String insertDataSql = String.format("INSERT INTO OperatingRooms (id_string, uri_string) VALUES ('%s', '%s')", or.id(), or.uri());

                // Esegui la query per inserire il dato
                int rowsAffected = statement.executeUpdate(insertDataSql);
                if (rowsAffected > 0) {
                    System.out.println("Elemento di prova inserito con successo.");
                } else {
                    System.out.println("Nessun elemento inserito.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Si è verificato un errore di connessione o di esecuzione della query SQL.");
            System.err.println("Verifica che il server MySQL sia in esecuzione, i dati di accesso siano corretti e che il database esista.");
            e.printStackTrace();
        }
    }
}
