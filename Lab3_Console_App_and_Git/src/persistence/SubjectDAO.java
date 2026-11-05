package persistence;

import config.ConnectionFactory;
import domain.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubjectDAO {

    public void insert(Subject c) throws SQLException {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt("id"));
            }
        }
    }

    public List<Subject> findAll() throws SQLException {
        String sql = "SELECT * FROM courses ORDER BY id";
        List<Subject> list = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Subject> findById(int id) throws SQLException {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public boolean update(Subject c) throws SQLException {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCode());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.setInt(4, c.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        return new Subject(rs.getInt("id"), rs.getString("code"),
                rs.getString("title"), rs.getInt("credits"));
    }
}
