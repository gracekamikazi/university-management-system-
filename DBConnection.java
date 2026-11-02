package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/universitydb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found! Add mysql-connector jar to lib!");
            System.out.println(e.getMessage());
            return null;
        } catch (SQLException e) {
            System.out.println("SQL Connection Failed! Check URL, database exists, XAMPP running");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}