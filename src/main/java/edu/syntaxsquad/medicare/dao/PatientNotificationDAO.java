package edu.syntaxsquad.medicare.dao;

import edu.syntaxsquad.medicare.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientNotificationDAO {

    public void save(int patientId, String message) throws SQLException {
        String sql = """
            INSERT INTO patient_notifications (patient_id, message)
            VALUES (?, ?)
        """;

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }

    public List<String[]> findAll() throws SQLException {
        List<String[]> list = new ArrayList<>();

        String sql = """
        SELECT p.first_name || ' ' || p.last_name AS patient,
               n.message,
               n.created_at
        FROM patient_notifications n
        JOIN patients p ON n.patient_id = p.id
        ORDER BY n.created_at DESC
    """;

        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("patient"),
                        rs.getString("message"),
                        rs.getString("created_at")
                });
            }
        }
        return list;
    }

}
