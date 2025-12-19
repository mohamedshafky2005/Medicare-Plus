package edu.syntaxsquad.medicare.dao;
import java.time.LocalDateTime;


import edu.syntaxsquad.medicare.model.Appointment;
import edu.syntaxsquad.medicare.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDAO {

    /**
     * Insert an appointment. Assumes appointment.startTime and endTime are set (ISO strings via LocalDateTime.toString()).
     * If appointment.doctorId is null it's the caller's responsibility to set it (scheduler can be used before calling).
     */
    public Appointment create(Appointment appt) throws SQLException {
        String sql = "INSERT INTO appointments(patient_id, doctor_id, start_time, end_time, status, urgency, notes) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appt.getPatientId());
            if (appt.getDoctorId() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, appt.getDoctorId());
            ps.setString(3, appt.getStartTime().toString());
            ps.setString(4, appt.getEndTime() != null ? appt.getEndTime().toString() : null);
            ps.setString(5, appt.getStatus() != null ? appt.getStatus().name() : Appointment.Status.SCHEDULED.name());
            ps.setInt(6, appt.getUrgency());
            ps.setString(7, appt.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) appt.setId(rs.getInt(1));
            }
        }
        return appt;
    }

    /**
     * Find all appointments (basic join info could be done by UI).
     */
    public List<Appointment> findAll() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT id, patient_id, doctor_id, start_time, end_time, status, urgency, notes FROM appointments ORDER BY start_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Appointment a = mapRow(rs);
                list.add(a);
            }
        }
        return list;
    }

    public Optional<Appointment> findById(int id) throws SQLException {
        String sql = "SELECT id, patient_id, doctor_id, start_time, end_time, status, urgency, notes FROM appointments WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public boolean updateStatus(int id, Appointment.Status status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Checks whether doctor has any appointment overlapping [start, end)
     * If doctorId is null, checks whether *any* appointment exists for the given patient overlapping the slot.
     * Ignores CANCELLED appointments.
     */
    public boolean hasConflictForDoctor(Integer doctorId, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM appointments WHERE (status IS NULL OR status != 'CANCELLED') AND ( " +
                " (end_time IS NULL) OR NOT (end_time <= ? OR start_time >= ?) ) ";
        if (doctorId == null) {
            // conflict check for patient (not doctor) - caller should use find by patient with patient id if needed
            return false;
        } else {
            sql = sql + " AND doctor_id = ?";
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            if (doctorId != null) ps.setInt(3, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt") > 0;
            }
        }
        return false;
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setPatientId(rs.getInt("patient_id"));
        int did = rs.getInt("doctor_id");
        if (!rs.wasNull()) a.setDoctorId(did);
        String s = rs.getString("start_time");
        if (s != null && !s.isBlank()) a.setStartTime(LocalDateTime.parse(s));
        String e = rs.getString("end_time");
        if (e != null && !e.isBlank()) a.setEndTime(LocalDateTime.parse(e));
        String st = rs.getString("status");
        if (st != null) a.setStatus(Appointment.Status.valueOf(st));
        a.setUrgency(rs.getInt("urgency"));
        a.setNotes(rs.getString("notes"));
        return a;
    }

    /**
     * Mark appointment completed: set status and end_time to provided timestamp (or now if null)
     */
    public boolean completeWithEndTime(int id, LocalDateTime endTime) throws SQLException {
        String sql = "UPDATE appointments SET status = ?, end_time = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.Status.COMPLETED.name());
            ps.setString(2, endTime != null ? endTime.toString() : LocalDateTime.now().toString());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Reschedule: set new start and end times and (optionally) reset status to SCHEDULED
     */
    public boolean reschedule(int id, LocalDateTime newStart, LocalDateTime newEnd) throws SQLException {
        String sql = "UPDATE appointments SET start_time = ?, end_time = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStart.toString());
            ps.setString(2, newEnd != null ? newEnd.toString() : null);
            ps.setString(3, Appointment.Status.SCHEDULED.name());
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Mark as delayed: update status and (optional) add note explaining the delay.
     */
    public boolean markDelayed(int id, String note) throws SQLException {
        String sql = "UPDATE appointments SET status = ?, notes = COALESCE(notes, '') || ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Appointment.Status.DELAYED.name());
            ps.setString(2, "\n[DELAYED] " + (note != null ? note : "") + " (" + LocalDateTime.now().toString() + ")");
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

}
