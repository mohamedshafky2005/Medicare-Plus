package edu.syntaxsquad.medicare.service;

import edu.syntaxsquad.medicare.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Reporting queries used by ReportPanel.
 */
public class ReportService {

    public static class DoctorCount {
        public int doctorId;
        public String doctorName;
        public String specialty;
        public int count;
        public DoctorCount(int id, String name, String specialty, int c) {
            this.doctorId = id; this.doctorName = name; this.specialty = specialty; this.count = c;
        }
    }

    public static class DoctorPerf {
        public int doctorId;
        public String doctorName;
        public String specialty;
        public int appointments;
        public double avgDurationMinutes; // may be null -> use 0
        public DoctorPerf(int id, String name, String specialty, int appointments, double avgDurationMinutes) {
            this.doctorId = id; this.doctorName = name; this.specialty = specialty;
            this.appointments = appointments; this.avgDurationMinutes = avgDurationMinutes;
        }
    }

    public static class PatientCount {
        public int patientId;
        public String patientName;
        public int visits;
        public PatientCount(int id, String name, int visits) {
            this.patientId = id; this.patientName = name; this.visits = visits;
        }
    }

    /**
     * Helper: convert date-only range to datetime strings (inclusive).
     */
    private static String toStartTimestamp(LocalDate d) { return d.atStartOfDay().toString(); }
    private static String toEndTimestamp(LocalDate d) { return d.atTime(LocalTime.MAX).toString(); }

    /**
     * Appointments per doctor (simple counts) - kept for backward compatibility.
     */
    public List<DoctorCount> getAppointmentsPerDoctor(LocalDate startDate, LocalDate endDate) throws SQLException {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        String sql =
                "SELECT d.id AS doctor_id, d.first_name || ' ' || d.last_name AS doctor_name, d.specialty, COUNT(a.id) AS cnt " +
                        "FROM appointments a " +
                        "JOIN doctors d ON a.doctor_id = d.id " +
                        "WHERE a.start_time BETWEEN ? AND ? " +
                        "GROUP BY d.id, doctor_name, d.specialty " +
                        "ORDER BY cnt DESC";

        List<DoctorCount> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("doctor_id");
                    String name = rs.getString("doctor_name");
                    String specialty = rs.getString("specialty");
                    int cnt = rs.getInt("cnt");
                    list.add(new DoctorCount(id, name, specialty, cnt));
                }
            }
        }
        return list;
    }

    /**
     * Doctor performance: count of appointments and average duration in minutes (calculated from start_time/end_time).
     * Uses SQLite strftime('%s', ...) to compute seconds difference -> convert to minutes.
     */
    public List<DoctorPerf> getDoctorPerformance(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql =
                "SELECT d.id AS doctor_id, d.first_name || ' ' || d.last_name AS doctor_name, d.specialty, " +
                        "COUNT(a.id) AS cnt, " +
                        "AVG( (strftime('%s', a.end_time) - strftime('%s', a.start_time)) / 60.0 ) AS avg_mins " +
                        "FROM appointments a " +
                        "JOIN doctors d ON a.doctor_id = d.id " +
                        "WHERE a.start_time BETWEEN ? AND ? AND a.end_time IS NOT NULL " +
                        "GROUP BY d.id, doctor_name, d.specialty " +
                        "ORDER BY cnt DESC";

        List<DoctorPerf> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, toStartTimestamp(startDate));
            ps.setString(2, toEndTimestamp(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("doctor_id");
                    String name = rs.getString("doctor_name");
                    String specialty = rs.getString("specialty");
                    int cnt = rs.getInt("cnt");
                    double avg = rs.getDouble("avg_mins"); // avg may be 0 if null; we keep as double
                    list.add(new DoctorPerf(id, name, specialty, cnt, avg));
                }
            }
        }
        return list;
    }

    /**
     * Patient visit counts in the date range.
     */
    public List<PatientCount> getPatientVisitCounts(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql =
                "SELECT p.id AS patient_id, p.first_name || ' ' || p.last_name AS patient_name, COUNT(a.id) AS visits " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.id " +
                        "WHERE a.start_time BETWEEN ? AND ? " +
                        "GROUP BY p.id, patient_name " +
                        "ORDER BY visits DESC";

        List<PatientCount> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, toStartTimestamp(startDate));
            ps.setString(2, toEndTimestamp(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PatientCount(rs.getInt("patient_id"), rs.getString("patient_name"), rs.getInt("visits")));
                }
            }
        }
        return list;
    }

    /**
     * Monthly appointment volumes grouped by yyyy-MM (e.g. 2025-12) for the appointments.start_time column.
     * This returns a LinkedHashMap preserving chronological order (ascending).
     */
    public Map<String, Integer> getMonthlyAppointmentVolumes(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql =
                "SELECT substr(start_time,1,7) AS ym, COUNT(*) AS cnt " +
                        "FROM appointments " +
                        "WHERE start_time BETWEEN ? AND ? " +
                        "GROUP BY ym " +
                        "ORDER BY ym ASC";

        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, toStartTimestamp(startDate));
            ps.setString(2, toEndTimestamp(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("ym"), rs.getInt("cnt"));
                }
            }
        }
        return map;
    }

    /**
     * Appointments by status in the date range (keeps existing behavior).
     */
    public Map<String, Integer> getAppointmentsByStatus(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT status, COUNT(*) AS cnt FROM appointments WHERE start_time BETWEEN ? AND ? GROUP BY status ORDER BY cnt DESC";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, toStartTimestamp(startDate));
            ps.setString(2, toEndTimestamp(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("status"), rs.getInt("cnt"));
                }
            }
        }
        return map;
    }
}
