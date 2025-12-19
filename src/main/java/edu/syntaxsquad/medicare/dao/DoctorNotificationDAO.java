package edu.syntaxsquad.medicare.dao;

import edu.syntaxsquad.medicare.model.DoctorNotification;
import edu.syntaxsquad.medicare.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorNotificationDAO {

    // ✅ INSERT (FIXED)
    public void save(int doctorId, String message) throws SQLException {
        String sql = """
            INSERT INTO doctor_notifications (doctor_id, message, created_at)
            VALUES (?, ?, datetime('now', 'localtime'))
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }

    // ✅ READ ALL WITH DOCTOR NAME
    public List<DoctorNotification> findAll() throws SQLException {
        List<DoctorNotification> list = new ArrayList<>();

        String sql = """
            SELECT dn.id,
                   dn.message,
                   dn.created_at,
                   d.first_name || ' ' || d.last_name AS doctor_name
            FROM doctor_notifications dn
            JOIN doctors d ON dn.doctor_id = d.id
            ORDER BY dn.created_at DESC
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DoctorNotification n = new DoctorNotification();
                n.setId(rs.getInt("id"));
                n.setDoctorName(rs.getString("doctor_name"));
                n.setMessage(rs.getString("message"));
                n.setCreatedAt(rs.getString("created_at"));
                list.add(n);
            }
        }
        return list;
    }
}
