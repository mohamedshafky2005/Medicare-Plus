package edu.syntaxsquad.medicare.model;

import java.time.LocalDateTime;

public class Appointment {
    public enum Status { SCHEDULED, COMPLETED, CANCELLED, DELAYED }

    private Integer id;
    private Integer patientId;
    private Integer doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;
    private int urgency;
    private String notes;

    public Appointment() {}

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getUrgency() { return urgency; }
    public void setUrgency(int urgency) { this.urgency = urgency; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
