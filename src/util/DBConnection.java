package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private static final String CONFIG_FILE = "config/database.properties";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties properties = loadProperties();
        String driver = getRequiredProperty(properties, "db.driver");
        String url = getRequiredProperty(properties, "db.url");
        String username = getRequiredProperty(properties, "db.username");
        String password = getRequiredProperty(properties, "db.password");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException exception) {
            throw new SQLException("Database driver not found. Add the MySQL Connector/J jar to the classpath.", exception);
        }

        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadProperties() throws SQLException {
        Properties properties = new Properties();

        try (InputStream fileInput = new FileInputStream(CONFIG_FILE)) {
            properties.load(fileInput);
            return properties;
        } catch (IOException ignored) {
            try (InputStream classpathInput = DBConnection.class.getClassLoader()
                    .getResourceAsStream("database.properties")) {
                if (classpathInput != null) {
                    properties.load(classpathInput);
                }
            } catch (IOException exception) {
                throw new SQLException("Unable to read database configuration.", exception);
            }
        }

        if (properties.isEmpty()) {
            throw new SQLException("Database configuration file not found: " + CONFIG_FILE);
        }

        return properties;
    }

    private static String getRequiredProperty(Properties properties, String key) throws SQLException {
        if (!properties.containsKey(key)) {
            throw new SQLException("Missing database configuration value: " + key);
        }

        return properties.getProperty(key);
    }
}
