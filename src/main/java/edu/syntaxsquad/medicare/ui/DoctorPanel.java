package edu.syntaxsquad.medicare.ui;

import edu.syntaxsquad.medicare.dao.DoctorDAO;
import edu.syntaxsquad.medicare.model.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DoctorPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;
    private final DoctorDAO dao = new DoctorDAO();

    public DoctorPanel() {
        setLayout(new BorderLayout(8,8));

        model = new DefaultTableModel(
                new Object[]{"ID", "First Name", "Last Name", "Specialty", "Contact", "Working Hours"},
                0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Update");
        JButton btnDelete = new JButton("Delete");

        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        add(top, BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> loadDoctors());
        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnDelete.addActionListener(e -> deleteDoctor());

        // initial load
        loadDoctors();
    }

    private void loadDoctors() {
        try {
            List<Doctor> list = dao.findAll();
            model.setRowCount(0);
            for (Doctor d : list) {
                model.addRow(new Object[]{
                        d.getId(),
                        d.getFirstName(),
                        d.getLastName(),
                        d.getSpecialty(),
                        d.getContact(),
                        d.getWorkingHours()
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load doctors: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog() {
        JTextField tfFirst = new JTextField(20);
        JTextField tfLast = new JTextField(20);
        JTextField tfSpecialty = new JTextField(20);
        JTextField tfContact = new JTextField(20);
        JTextField tfHours = new JTextField("Mon-Fri 09:00-17:00", 20);

        JPanel panel = new JPanel(new GridLayout(0,1,4,4));
        panel.add(new JLabel("First Name:")); panel.add(tfFirst);
        panel.add(new JLabel("Last Name:")); panel.add(tfLast);
        panel.add(new JLabel("Specialty:")); panel.add(tfSpecialty);
        panel.add(new JLabel("Contact:")); panel.add(tfContact);
        panel.add(new JLabel("Working Hours:")); panel.add(tfHours);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Doctor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Doctor d = new Doctor();
            d.setFirstName(tfFirst.getText().trim());
            d.setLastName(tfLast.getText().trim());
            d.setSpecialty(tfSpecialty.getText().trim());
            d.setContact(tfContact.getText().trim());
            d.setWorkingHours(tfHours.getText().trim());

            try {
                dao.create(d);
                loadDoctors();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to save doctor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a doctor to update."); return; }

        int id = (int) model.getValueAt(row, 0);
        String first = (String) model.getValueAt(row, 1);
        String last = (String) model.getValueAt(row, 2);
        String specialty = (String) model.getValueAt(row, 3);
        String contact = (String) model.getValueAt(row, 4);
        String hours = (String) model.getValueAt(row, 5);

        JTextField tfFirst = new JTextField(first, 20);
        JTextField tfLast = new JTextField(last, 20);
        JTextField tfSpecialty = new JTextField(specialty, 20);
        JTextField tfContact = new JTextField(contact, 20);
        JTextField tfHours = new JTextField(hours, 20);

        JPanel panel = new JPanel(new GridLayout(0,1,4,4));
        panel.add(new JLabel("First Name:")); panel.add(tfFirst);
        panel.add(new JLabel("Last Name:")); panel.add(tfLast);
        panel.add(new JLabel("Specialty:")); panel.add(tfSpecialty);
        panel.add(new JLabel("Contact:")); panel.add(tfContact);
        panel.add(new JLabel("Working Hours:")); panel.add(tfHours);

        int result = JOptionPane.showConfirmDialog(this, panel, "Update Doctor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Doctor d = new Doctor();
            d.setId(id);
            d.setFirstName(tfFirst.getText().trim());
            d.setLastName(tfLast.getText().trim());
            d.setSpecialty(tfSpecialty.getText().trim());
            d.setContact(tfContact.getText().trim());
            d.setWorkingHours(tfHours.getText().trim());

            try {
                dao.update(d);
                loadDoctors();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to update doctor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteDoctor() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a doctor to delete."); return; }
        int id = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Delete selected doctor?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(id);
            loadDoctors();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
