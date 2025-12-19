package edu.syntaxsquad.medicare.dao;

import edu.syntaxsquad.medicare.model.Doctor;
import edu.syntaxsquad.medicare.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DoctorDAO {

    public Doctor create(Doctor d) throws SQLException {
        String sql = "INSERT INTO doctors (first_name, last_name, specialty, contact, working_hours) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getFirstName());
            ps.setString(2, d.getLastName());
            ps.setString(3, d.getSpecialty());
            ps.setString(4, d.getContact());
            ps.setString(5, d.getWorkingHours());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) d.setId(rs.getInt(1));
            }
        }
        return d;
    }

    public void update(Doctor d) throws SQLException {
        String sql = "UPDATE doctors SET first_name=?, last_name=?, specialty=?, contact=?, working_hours=? WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getFirstName());
            ps.setString(2, d.getLastName());
            ps.setString(3, d.getSpecialty());
            ps.setString(4, d.getContact());
            ps.setString(5, d.getWorkingHours());
            ps.setInt(6, d.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM doctors WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** REQUIRED BY SchedulerService **/
    public Doctor findById(int id) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** REQUIRED BY SchedulerService **/
    public List<Doctor> findBySpecialty(String specialty) throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE specialty=? ORDER BY last_name";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, specialty);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Doctor> findAll() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = "SELECT * FROM doctors ORDER BY last_name";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Doctor map(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setId(rs.getInt("id"));
        d.setFirstName(rs.getString("first_name"));
        d.setLastName(rs.getString("last_name"));
        d.setSpecialty(rs.getString("specialty"));
        d.setContact(rs.getString("contact"));
        d.setWorkingHours(rs.getString("working_hours"));
        return d;
    }

    /**
     * Returns true if the doctor has NO conflicting appointments.
     * Checks time overlap (excluding CANCELLED).
     */
    public boolean isDoctorAvailable(int doctorId, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql =
                "SELECT COUNT(*) AS cnt FROM appointments " +
                        "WHERE doctor_id = ? " +
                        "AND (status IS NULL OR status != 'CANCELLED') " +
                        "AND (" +
                        "     NOT (end_time <= ? OR start_time >= ?)" +
                        ")";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setString(2, start.toString());  // existing.end_time <= new.start
            ps.setString(3, end.toString());    // existing.start_time >= new.end

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt") == 0; // no conflicts → available
                }
            }
        }
        return false;
    }
    /**
     * Returns the number of appointments assigned to a doctor
     * in a given date/time range. Used for workload balancing.
     */
    public int getAppointmentCountBetween(int doctorId, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM appointments " +
                "WHERE doctor_id = ? AND start_time BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setString(2, start.toString()); // ISO format for LocalDateTime
            ps.setString(3, end.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }
    /**
     * Returns the earliest end_time of any upcoming appointment for the doctor,
     * starting from a given time. Used to determine when a busy doctor becomes free.
     */
    public LocalDateTime getEarliestNextFreeAfter(int doctorId, LocalDateTime from) throws SQLException {

        String sql = "SELECT MIN(end_time) AS next_end " +
                "FROM appointments " +
                "WHERE doctor_id = ? " +
                "AND start_time >= ? " +
                "AND (status IS NULL OR status != 'CANCELLED') " +
                "AND end_time IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorId);
            ps.setString(2, from.toString());

            try (ResultSet rs = ps.executeQuery()) {
                String next = rs.getString("next_end");
                if (next != null && !next.isBlank()) {
                    return LocalDateTime.parse(next);  // Convert database string → LocalDateTime
                }
            }
        }

        return null; // Means no upcoming appointments → free the whole day
    }

}
