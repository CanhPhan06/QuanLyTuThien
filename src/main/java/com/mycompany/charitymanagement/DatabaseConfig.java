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
        java.util.Properties props = new java.util.Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);
        props.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
        props.setProperty("oracle.jdbc.ReadTimeout", "10000");
        return DriverManager.getConnection(URL, props);
    }

    public static String connectionLabel() {
        return USER + "@" + URL;
    }
}
