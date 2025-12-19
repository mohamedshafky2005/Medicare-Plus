package edu.syntaxsquad.medicare.ui;

import edu.syntaxsquad.medicare.model.DoctorNotification;
import edu.syntaxsquad.medicare.service.DoctorNotificationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DoctorNotificationPanel extends JPanel {

    private final DefaultTableModel model;
    private final DoctorNotificationService service =
            new DoctorNotificationService();

    public DoctorNotificationPanel() {
        setLayout(new BorderLayout(8, 8));

        model = new DefaultTableModel(
                new Object[]{"Doctor", "Message", "Date"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadNotifications());
        add(btnRefresh, BorderLayout.SOUTH);

        loadNotifications();
    }

    // 🔁 CALLED FROM MAINFRAME WHEN PANEL IS SHOWN
    public void refresh() {
        loadNotifications();
    }

    private void loadNotifications() {
        try {
            model.setRowCount(0);

            List<DoctorNotification> list =
                    service.getAllNotifications();

            for (DoctorNotification n : list) {
                model.addRow(new Object[]{
                        n.getDoctorName(),
                        n.getMessage(),
                        n.getCreatedAt()
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load notifications: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
