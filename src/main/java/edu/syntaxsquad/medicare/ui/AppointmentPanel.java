package edu.syntaxsquad.medicare.ui;

import edu.syntaxsquad.medicare.dao.*;
import edu.syntaxsquad.medicare.model.*;
import edu.syntaxsquad.medicare.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Optional;


public class AppointmentPanel extends JPanel {

    // SERVICES
    private final PatientNotificationService patientNotify =
            new PatientNotificationService();
    private final DoctorNotificationService doctorNotify =
            new DoctorNotificationService();
    private final SchedulerService scheduler =
            new SchedulerService();

    // DAOs
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    // UI
    private final JComboBox<Patient> cbPatient = new JComboBox<>();
    private final JComboBox<String> cbSpecialty = new JComboBox<>();
    private final JComboBox<Object> cbDoctor = new JComboBox<>();
    private final JTextField tfStart = new JTextField(16);
    private final JSpinner spDuration =
            new JSpinner(new SpinnerNumberModel(30, 5, 240, 5));
    private final JSpinner spUrgency =
            new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));

    private final DefaultTableModel tableModel;

    private final DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AppointmentPanel() {
        setLayout(new BorderLayout(8, 8));

        // ================= FORM =================
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        tfStart.setText(
                LocalDateTime.now().plusDays(1)
                        .withHour(9).withMinute(0)
                        .format(fmt)
        );

        int r = 0;
        g.gridx = 0; g.gridy = r; form.add(new JLabel("Patient:"), g);
        g.gridx = 1; form.add(cbPatient, g);

        g.gridx = 0; g.gridy = ++r; form.add(new JLabel("Specialty:"), g);
        g.gridx = 1; form.add(cbSpecialty, g);

        g.gridx = 0; g.gridy = ++r; form.add(new JLabel("Doctor:"), g);
        g.gridx = 1; form.add(cbDoctor, g);

        g.gridx = 0; g.gridy = ++r;
        form.add(new JLabel("Start (yyyy-MM-dd HH:mm):"), g);
        g.gridx = 1; form.add(tfStart, g);

        g.gridx = 0; g.gridy = ++r;
        form.add(new JLabel("Duration (min):"), g);
        g.gridx = 1; form.add(spDuration, g);

        g.gridx = 0; g.gridy = ++r;
        form.add(new JLabel("Urgency:"), g);
        g.gridx = 1; form.add(spUrgency, g);

        JButton btnSave = new JButton("Assign & Save");
        JButton btnRefresh = new JButton("Refresh");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.add(btnSave);
        btns.add(btnRefresh);

        g.gridx = 0; g.gridy = ++r; g.gridwidth = 2;
        form.add(btns, g);

        add(form, BorderLayout.NORTH);

        // ================= TABLE =================
        tableModel = new DefaultTableModel(
                new Object[]{"ID","Patient","Doctor","Start","End","Status","Urgency"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ================= ACTION BUTTONS =================
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnCompleted = new JButton("Mark Completed");
        JButton btnDelayed = new JButton("Mark Delayed");
        JButton btnReschedule = new JButton("Reschedule");
        JButton btnCancel = new JButton("Cancel");

        bottom.add(btnCompleted);
        bottom.add(btnDelayed);
        bottom.add(btnReschedule);
        bottom.add(btnCancel);

        add(bottom, BorderLayout.SOUTH);

        // ================= LISTENERS =================
        btnSave.addActionListener(e -> createAppointment());
        btnRefresh.addActionListener(e -> reloadAll());
        cbSpecialty.addActionListener(e -> reloadDoctors());

        btnCompleted.addActionListener(e -> markCompleted(table));
        btnDelayed.addActionListener(e -> markDelayed(table));
        btnReschedule.addActionListener(e -> reschedule(table));
        btnCancel.addActionListener(e -> cancel(table));

        reloadAll();
    }

    // ================= CREATE =================
    private void createAppointment() {
        try {
            Patient p = (Patient) cbPatient.getSelectedItem();
            if (p == null) return;

            LocalDateTime start =
                    LocalDateTime.parse(tfStart.getText().replace(" ", "T"));
            int dur = (Integer) spDuration.getValue();
            LocalDateTime end = start.plusMinutes(dur);
            int urgency = (Integer) spUrgency.getValue();

            Integer doctorId;
            Object sel = cbDoctor.getSelectedItem();

            if (sel instanceof String) {
                Doctor d = scheduler.assignDoctor(
                        (String) cbSpecialty.getSelectedItem(),
                        start, dur, urgency
                ).orElseThrow();
                doctorId = d.getId();
            } else {
                doctorId = ((Doctor) sel).getId();
            }

            Appointment a = new Appointment();
            a.setPatientId(p.getId());
            a.setDoctorId(doctorId);
            a.setStartTime(start);
            a.setEndTime(end);
            a.setStatus(Appointment.Status.SCHEDULED);
            a.setUrgency(urgency);

            appointmentDAO.create(a);

            //  DATABASE NOTIFICATIONS (NO POPUPS)
            patientNotify.upcoming(p.getId(), start.format(fmt));
            doctorNotify.notifyDoctor(
                    doctorId,
                    "New appointment scheduled on " + start.format(fmt)
            );

            loadAppointments();

        } catch (Exception ignored) {}
    }

    // ================= STATUS ACTIONS =================
    private void markCompleted(JTable table) {
        Appointment a = getSelected(table);
        if (a == null) return;

        try {
            appointmentDAO.completeWithEndTime(a.getId(), LocalDateTime.now());
            patientNotify.completed(a.getPatientId());
            doctorNotify.notifyDoctor(a.getDoctorId(), "Appointment completed");
            loadAppointments();
        } catch (Exception ignored) {}
    }

    private void markDelayed(JTable table) {
        Appointment a = getSelected(table);
        if (a == null) return;

        try {
            appointmentDAO.markDelayed(a.getId(), "Delayed");
            patientNotify.delayed(a.getPatientId());
            doctorNotify.notifyDoctor(a.getDoctorId(), "Appointment delayed");
            loadAppointments();
        } catch (Exception ignored) {}
    }

    private void reschedule(JTable table) {
        Appointment a = getSelected(table);
        if (a == null) return;

        String s = JOptionPane.showInputDialog(
                this, "New start (yyyy-MM-dd HH:mm):");
        if (s == null) return;

        try {
            LocalDateTime newStart =
                    LocalDateTime.parse(s.replace(" ", "T"));
            long minutes =
                    java.time.Duration.between(
                            a.getStartTime(), a.getEndTime()).toMinutes();
            LocalDateTime newEnd = newStart.plusMinutes(minutes);

            appointmentDAO.reschedule(a.getId(), newStart, newEnd);

            patientNotify.rescheduled(a.getPatientId(), newStart.format(fmt));
            doctorNotify.notifyDoctor(
                    a.getDoctorId(),
                    "Appointment rescheduled to " + newStart.format(fmt)
            );

            loadAppointments();
        } catch (Exception ignored) {}
    }

    private void cancel(JTable table) {
        Appointment a = getSelected(table);
        if (a == null) return;

        try {
            appointmentDAO.updateStatus(a.getId(), Appointment.Status.CANCELLED);
            patientNotify.cancelled(a.getPatientId());
            doctorNotify.notifyDoctor(a.getDoctorId(), "Appointment cancelled");
            loadAppointments();
        } catch (Exception ignored) {}
    }

    // ================= HELPERS =================
    private Appointment getSelected(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        try {
            int id = (Integer) tableModel.getValueAt(r, 0);
            return appointmentDAO.findById(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void reloadAll() {
        try {
            cbPatient.removeAllItems();
            for (Patient p : patientDAO.findAll()) cbPatient.addItem(p);

            cbDoctor.removeAllItems();
            cbDoctor.addItem("Auto-assign");

            Set<String> specs = new TreeSet<>();
            for (Doctor d : doctorDAO.findAll()) {
                cbDoctor.addItem(d);
                specs.add(d.getSpecialty());
            }

            cbSpecialty.removeAllItems();
            for (String s : specs) cbSpecialty.addItem(s);

            loadAppointments();
        } catch (Exception ignored) {}
    }

    private void reloadDoctors() {
        try {
            cbDoctor.removeAllItems();
            cbDoctor.addItem("Auto-assign");
            for (Doctor d : doctorDAO.findBySpecialty(
                    (String) cbSpecialty.getSelectedItem())) {
                cbDoctor.addItem(d);
            }
        } catch (Exception ignored) {}
    }

    private void loadAppointments() {
        try {
            tableModel.setRowCount(0);
            for (Appointment a : appointmentDAO.findAll()) {
                tableModel.addRow(new Object[]{
                        a.getId(),
                        getName(patientDAO.findAll(), a.getPatientId()),
                        getName(doctorDAO.findAll(), a.getDoctorId()),
                        a.getStartTime().format(fmt),
                        a.getEndTime().format(fmt),
                        a.getStatus(),
                        a.getUrgency()
                });
            }
        } catch (Exception ignored) {}
    }

    private String getName(List<?> list, Integer id) {
        return list.stream()
                .filter(o -> (o instanceof Patient && ((Patient)o).getId().equals(id)) ||
                        (o instanceof Doctor && ((Doctor)o).getId().equals(id)))
                .map(o -> o instanceof Patient
                        ? ((Patient)o).getFirstName() + " " + ((Patient)o).getLastName()
                        : ((Doctor)o).getFirstName() + " " + ((Doctor)o).getLastName())
                .findFirst()
                .orElse("N/A");
    }
}
