package persistence;

import config.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    // CREATE: enroll a student in a course
    public void enroll(int studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    // UPDATE: record / change a grade
    public boolean updateGrade(int studentId, int courseId, double grade) throws SQLException {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }
        String sql = "UPDATE enrollments SET grade = ? WHERE student_id = ? AND course_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, grade);
            ps.setInt(2, studentId);
            ps.setInt(3, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE: remove one enrollment
    public boolean unenroll(int studentId, int courseId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    // READ with JOINs: every student with their courses.
    // LEFT JOIN keeps students who have no courses yet (course columns come back NULL).
    public List<RegistrationRecord> findAllWithDetails() throws SQLException {
        String sql = """
                SELECT s.id AS student_id, s.name, c.code, c.title, e.grade
                FROM students s
                LEFT JOIN enrollments e ON e.student_id = s.id
                LEFT JOIN courses c     ON c.id = e.course_id
                ORDER BY s.name, s.id, c.code
                """;
        List<RegistrationRecord> list = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double g = rs.getDouble("grade");
                Double grade = rs.wasNull() ? null : g;   // wasNull() checks if the column was NULL
                list.add(new RegistrationRecord(rs.getInt("student_id"), rs.getString("name"),
                        rs.getString("code"), rs.getString("title"), grade));
            }
        }
        return list;
    }
}
