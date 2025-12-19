package edu.syntaxsquad.medicare.ui;

import edu.syntaxsquad.medicare.service.ReportService;
import edu.syntaxsquad.medicare.service.ReportService.DoctorCount;
import edu.syntaxsquad.medicare.service.ReportService.DoctorPerf;
import edu.syntaxsquad.medicare.service.ReportService.PatientCount;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class ReportPanel extends JPanel {
    private final JTextField tfStart;
    private final JTextField tfEnd;
    private final ReportService service = new ReportService();
    private boolean initialLoad = true;

    // models for tabs
    private final DefaultTableModel doctorPerfModel;
    private final DefaultTableModel statusModel;
    private final DefaultTableModel patientModel;
    private final DefaultTableModel monthlyModel;

    public ReportPanel() {
        setLayout(new BorderLayout(8,8));

        // top controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        YearMonth ym = YearMonth.now();
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        tfStart = new JTextField(startOfMonth.toString(), 10);
        tfEnd = new JTextField(endOfMonth.toString(), 10);
        JButton btnRun = new JButton("Run");

        top.add(new JLabel("Start (yyyy-mm-dd):"));
        top.add(tfStart);
        top.add(new JLabel("End (yyyy-mm-dd):"));
        top.add(tfEnd);
        top.add(btnRun);

        add(top, BorderLayout.NORTH);

        // create table models
        doctorPerfModel = new DefaultTableModel(new Object[]{"Doctor ID","Doctor","Specialty","Appointments","Avg Duration (min)"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        statusModel = new DefaultTableModel(new Object[]{"Status","Count"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        patientModel = new DefaultTableModel(new Object[]{"Patient ID","Patient","Visits"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };
        monthlyModel = new DefaultTableModel(new Object[]{"Month (YYYY-MM)","Appointments"}, 0) {
            public boolean isCellEditable(int r,int c){ return false; }
        };

        // create tables for each tab
        JTable tDoctorPerf = new JTable(doctorPerfModel);
        JTable tStatus = new JTable(statusModel);
        JTable tPatients = new JTable(patientModel);
        JTable tMonthly = new JTable(monthlyModel);

        // tabbed pane with 4 tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Doctor Performance", new JScrollPane(tDoctorPerf));
        tabs.addTab("Appointments by Status", new JScrollPane(tStatus));
        tabs.addTab("Patient Visits", new JScrollPane(tPatients));
        tabs.addTab("Monthly Volumes", new JScrollPane(tMonthly));

        add(tabs, BorderLayout.CENTER);

        // run action
        btnRun.addActionListener(e -> runReports());

        // initial run
        runReports();
    }

    private void runReports() {
        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(tfStart.getText().trim());
            end = LocalDate.parse(tfEnd.getText().trim());
            if (end.isBefore(start)) {
                JOptionPane.showMessageDialog(this, "End date must be same or after start date.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use yyyy-mm-dd.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // doctor performance
            List<DoctorPerf> perf = service.getDoctorPerformance(start, end);
            doctorPerfModel.setRowCount(0);
            for (DoctorPerf d : perf) {
                doctorPerfModel.addRow(new Object[]{d.doctorId, d.doctorName, d.specialty, d.appointments, String.format("%.1f", d.avgDurationMinutes)});
            }

            // status
            Map<String,Integer> status = service.getAppointmentsByStatus(start, end);
            statusModel.setRowCount(0);
            for (Map.Entry<String,Integer> e : status.entrySet()) statusModel.addRow(new Object[]{e.getKey(), e.getValue()});

            // patient visits
            List<PatientCount> patients = service.getPatientVisitCounts(start, end);
            patientModel.setRowCount(0);
            for (PatientCount p : patients) patientModel.addRow(new Object[]{p.patientId, p.patientName, p.visits});

            // monthly volumes
            Map<String,Integer> monthly = service.getMonthlyAppointmentVolumes(start, end);
            monthlyModel.setRowCount(0);
            for (Map.Entry<String,Integer> e : monthly.entrySet()) monthlyModel.addRow(new Object[]{e.getKey(), e.getValue()});

            // only show "no appointments" message when user explicitly clicked Run (not initial automatic load)
            boolean empty = doctorPerfModel.getRowCount() == 0 && statusModel.getRowCount() == 0 && patientModel.getRowCount() == 0 && monthlyModel.getRowCount() == 0;
            if (empty && !initialLoad) {
                JOptionPane.showMessageDialog(this, "No appointments in the selected range.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load reports: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            initialLoad = false;
        }
    }
}
