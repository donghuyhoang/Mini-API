package com.javaweb.config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/your_db";
    private static final String USER = "your_user"; 
    private static final String PASSWORD = "your_password";
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }
}
