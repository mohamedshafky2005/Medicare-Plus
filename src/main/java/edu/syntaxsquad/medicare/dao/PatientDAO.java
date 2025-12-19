package edu.syntaxsquad.medicare.dao;

import edu.syntaxsquad.medicare.model.Patient;
import edu.syntaxsquad.medicare.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAO {

    public Patient create(Patient p) throws SQLException {
        String sql = "INSERT INTO patients(first_name,last_name,dob,contact,medical_history) VALUES(?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getDob() != null ? p.getDob().toString() : null);
            ps.setString(4, p.getContact());
            ps.setString(5, p.getMedicalHistory());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
        return p;
    }

    public List<Patient> findAll() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, dob, contact, medical_history FROM patients ORDER BY last_name, first_name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // returns Optional because caller may ask for an id that doesn't exist
    public Optional<Patient> findById(int id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, dob, contact, medical_history FROM patients WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    // update existing patient, returns true if a row was updated
    public boolean update(Patient p) throws SQLException {
        if (p.getId() == null) throw new IllegalArgumentException("Patient id is null for update");
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, dob = ?, contact = ?, medical_history = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getDob() != null ? p.getDob().toString() : null);
            ps.setString(4, p.getContact());
            ps.setString(5, p.getMedicalHistory());
            ps.setInt(6, p.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // delete by id, returns true if a row was deleted
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM patients WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    // helper to map resultset row -> Patient
    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        String dob = rs.getString("dob");
        if (dob != null && !dob.isBlank()) {
            try { p.setDob(LocalDate.parse(dob)); } catch (Exception ignored) {}
        }
        p.setContact(rs.getString("contact"));
        p.setMedicalHistory(rs.getString("medical_history"));
        return p;
    }
}
