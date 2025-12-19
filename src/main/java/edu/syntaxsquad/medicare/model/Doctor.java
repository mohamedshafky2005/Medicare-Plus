package edu.syntaxsquad.medicare.model;

public class Doctor {
    private Integer id;
    private String firstName;
    private String lastName;
    private String specialty;
    private String contact;
    private String workingHours; // simple text

    public Doctor() {}

    // getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + specialty + ")";
    }
}
