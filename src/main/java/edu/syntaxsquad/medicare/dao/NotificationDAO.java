package edu.syntaxsquad.medicare.dao;

import edu.syntaxsquad.medicare.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class NotificationDAO {

    /** Inserts a new notification for a patient. */
    public void send(int patientId, String message) throws Exception {
        String sql = "INSERT INTO notifications(patient_id, message) VALUES(?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, patientId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }
}
