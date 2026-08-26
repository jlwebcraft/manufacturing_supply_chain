package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection utility for the terminal application.
 * Connection values may be overridden with -Ddb.url, -Ddb.user and -Ddb.password.
 */
public final class DBConnection {
    private static final String URL = System.getProperty(
            "db.url", "jdbc:mysql://localhost:3306/manufacturing_supply_chain");
    private static final String USER = System.getProperty("db.user", "root");
    private static final String PASSWORD = System.getProperty("db.password", "");

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
