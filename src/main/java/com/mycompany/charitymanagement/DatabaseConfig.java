package com.mycompany.charitymanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {

    private static final String URL = System.getProperty(
            "charity.db.url",
            "jdbc:oracle:thin:@//localhost:1522/XE"
    );
    private static final String USER = System.getProperty("charity.db.user", "CHARITY");
    private static final String PASSWORD = System.getProperty("charity.db.password", "charity123");

    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String connectionLabel() {
        return USER + "@" + URL;
    }
}
