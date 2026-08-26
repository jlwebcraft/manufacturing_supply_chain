package com.mscm.supplier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Replace these values with the team's shared database configuration if available. */
public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/manufacturing_supply_chain";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP default is usually blank.

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
