package edu.syntaxsquad.medicare.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    // Keep reference for refresh
    private final DoctorNotificationPanel doctorNotificationPanel =
            new DoctorNotificationPanel();

    public MainFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Medicare Plus");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ---------- MENU ----------
        JMenuBar menuBar = new JMenuBar();
        JMenu mFile = new JMenu("File");
        JMenuItem miExit = new JMenuItem("Exit");
        miExit.addActionListener(e -> System.exit(0));
        mFile.add(miExit);
        menuBar.add(mFile);
        setJMenuBar(menuBar);

        // ---------- LEFT NAV ----------
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JButton bPatients = new JButton("Patients");
        JButton bDoctors = new JButton("Doctors");
        JButton bAppointments = new JButton("Appointments");
        JButton bReports = new JButton("Reports");
        JButton bDoctorNotifications = new JButton("Doctor Notifications");

        left.add(bPatients);
        left.add(Box.createVerticalStrut(8));
        left.add(bDoctors);
        left.add(Box.createVerticalStrut(8));
        left.add(bAppointments);
        left.add(Box.createVerticalStrut(8));
        left.add(bReports);
        left.add(Box.createVerticalStrut(8));
        left.add(bDoctorNotifications);


        JButton btnPatientNotifications = new JButton("Patient Notifications");
        left.add(btnPatientNotifications);

        PatientNotificationPanel patientPanel =
                new PatientNotificationPanel();

        cards.add(patientPanel, "patientNotifications");

        btnPatientNotifications.addActionListener(e -> {
            patientPanel.refresh();
            cardLayout.show(cards, "patientNotifications");
        });
        // ---------- CARDS ----------
        cards.add(new PatientPanel(), "patients");
        cards.add(new DoctorPanel(), "doctors");
        cards.add(new AppointmentPanel(), "appointments");
        cards.add(new ReportPanel(), "reports");
        cards.add(doctorNotificationPanel, "doctorNotifications");

        // ---------- ACTIONS ----------
        bPatients.addActionListener(e -> cardLayout.show(cards, "patients"));
        bDoctors.addActionListener(e -> cardLayout.show(cards, "doctors"));
        bAppointments.addActionListener(e -> cardLayout.show(cards, "appointments"));
        bReports.addActionListener(e -> cardLayout.show(cards, "reports"));

        // 🔁 AUTO REFRESH ON OPEN
        bDoctorNotifications.addActionListener(e -> {
            doctorNotificationPanel.refresh();
            cardLayout.show(cards, "doctorNotifications");
        });

        // ---------- LAYOUT ----------
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(left, BorderLayout.WEST);
        getContentPane().add(cards, BorderLayout.CENTER);
    }
}
