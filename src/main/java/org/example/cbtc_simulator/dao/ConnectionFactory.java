package org.example.cbtc_simulator.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = ConnectionFactory.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) props.load(in);
        } catch (IOException ignored) {}

        URL = env("DB_URL",  props.getProperty("db.url",  "jdbc:mysql://localhost:3306/cbtc_simulator"));
        USER = env("DB_USER", props.getProperty("db.user", "root"));
        PASSWORD = env("DB_PASSWORD", props.getProperty("db.password", ""));
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private ConnectionFactory() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
