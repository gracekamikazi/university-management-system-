package dao;

import java.sql.*;
import model.Student;
import util.DBConnection;

public class StudentDAO {

    // Add student
    public void addStudent(Student student) {
        try {
            Connection conn = DBConnection.getConnection();
            String sql = "INSERT INTO students (name, email, course) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, student.getName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getCourse());
            ps.executeUpdate();
            System.out.println("Student added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
        }
    }

    // View all students - FINAL FIX
    public void getAllStudents() {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            System.out.println("\n--- All Students ---");
            while(rs.next()){
                System.out.println(rs.getInt("id") + " | " + rs.getString("name") + " | " + rs.getString("email") + " | " + rs.getString("course"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing students: " + e.getMessage());
        }
    }
}