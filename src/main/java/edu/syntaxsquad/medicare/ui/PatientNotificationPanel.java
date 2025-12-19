package edu.syntaxsquad.medicare.ui;

import edu.syntaxsquad.medicare.dao.PatientNotificationDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PatientNotificationPanel extends JPanel {

    private final DefaultTableModel model;
    private final PatientNotificationDAO dao =
            new PatientNotificationDAO();

    public PatientNotificationPanel() {
        setLayout(new BorderLayout(8,8));

        model = new DefaultTableModel(
                new Object[]{"Patient", "Message", "Date"}, 0
        ) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());
        add(btnRefresh, BorderLayout.SOUTH);

        refresh();
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            for (String[] row : dao.findAll()) {
                model.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load patient notifications");
        }
    }
}
