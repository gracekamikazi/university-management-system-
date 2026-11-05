package persistence;

import config.ConnectionFactory;
import domain.PostgradLearner;
import domain.Learner;
import domain.UndergradLearner;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LearnerDAO {

    // CREATE
    public void insert(Learner s) throws SQLException {
        String sql = "INSERT INTO students (name, email, student_type) VALUES (?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getLearnerType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.setId(keys.getInt("id"));   // put the new id back on the object
            }
        }
    }

    // READ (all)
    public List<Learner> findAll() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY id";
        List<Learner> list = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // READ (one)
    public Optional<Learner> findById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // UPDATE
    public boolean update(Learner s) throws SQLException {
        String sql = "UPDATE students SET name = ?, email = ?, student_type = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getLearnerType());
            ps.setInt(4, s.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;   // true if a row was really deleted
        }
    }

    // Turns a database row into the right Java object (polymorphism again)
    private Learner mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String type = rs.getString("student_type");
        if ("GRADUATE".equals(type)) {
            return new PostgradLearner(id, name, email);
        }
        return new UndergradLearner(id, name, email);
    }
}
