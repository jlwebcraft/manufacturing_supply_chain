package util;

import exception.DatabaseException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single shared Database Connection Utility for the Manufacturing and Supply Chain System.
 * Shared across Member 1, Member 2, Member 3, and Member 4.
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/manufacturing_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Ensure mysql-connector-j jar is added to classpath.");
        }
    }

    private DBConnection() {
        // Private constructor to prevent instantiation
    }

    /**
     * Obtains a connection to the MySQL database.
     * @return Connection object
     * @throws DatabaseException if connection fails
     */
    public static Connection getConnection() throws DatabaseException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the given Connection safely.
     * @param connection Connection object to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
