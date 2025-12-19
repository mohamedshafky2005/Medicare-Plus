package edu.syntaxsquad.medicare.service;

import edu.syntaxsquad.medicare.dao.PatientNotificationDAO;

public class PatientNotificationService {

    private final PatientNotificationDAO dao =
            new PatientNotificationDAO();

    public void upcoming(int patientId, String time) {
        save(patientId, "Upcoming appointment at " + time);
    }

    public void completed(int patientId) {
        save(patientId, "Your appointment has been completed.");
    }

    public void delayed(int patientId) {
        save(patientId, "Your appointment has been delayed.");
    }

    public void rescheduled(int patientId, String time) {
        save(patientId, "Your appointment was rescheduled to " + time);
    }

    public void cancelled(int patientId) {
        save(patientId, "Your appointment was cancelled.");
    }

    private void save(int patientId, String msg) {
        try {
            dao.save(patientId, msg);
        } catch (Exception ignored) {}
    }
}
