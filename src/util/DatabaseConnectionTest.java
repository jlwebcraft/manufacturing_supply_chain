package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String sql = "SELECT 1 AS connection_test";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                System.out.println("Database connection test passed: " + resultSet.getInt("connection_test"));
            }
        } catch (SQLException exception) {
            System.out.println("Database connection test failed: " + exception.getMessage());
        }
    }
}
