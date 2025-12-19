package edu.syntaxsquad.medicare.model;

import java.time.LocalDate;

public class Patient {
    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String contact;
    private String medicalHistory;

    public Patient() {}

    public Patient(Integer id, String firstName, String lastName, LocalDate dob, String contact, String medicalHistory) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.contact = contact;
        this.medicalHistory = medicalHistory;
    }

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
