package edu.syntaxsquad.medicare.service;

import edu.syntaxsquad.medicare.dao.DoctorNotificationDAO;
import edu.syntaxsquad.medicare.model.DoctorNotification;

import java.sql.SQLException;
import java.util.List;

public class DoctorNotificationService {

    private final DoctorNotificationDAO dao = new DoctorNotificationDAO();

    public void notifyDoctor(int doctorId, String message) throws SQLException {
        dao.save(doctorId, message);
    }

    public List<DoctorNotification> getAllNotifications() throws SQLException {
        return dao.findAll();
    }
}
