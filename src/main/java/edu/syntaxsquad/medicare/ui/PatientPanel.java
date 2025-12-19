package edu.syntaxsquad.medicare.ui;

import edu.syntaxsquad.medicare.dao.PatientDAO;
import edu.syntaxsquad.medicare.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PatientPanel extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;

    public PatientPanel() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"ID", "First Name", "Last Name", "DOB", "Contact"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");
        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        add(top, BorderLayout.NORTH);

        btnRefresh.addActionListener(e -> loadPatients());
        btnAdd.addActionListener(e -> addPatient());
        btnEdit.addActionListener(e -> editSelectedPatient());
        btnDelete.addActionListener(e -> deleteSelected());

        // double-click to edit
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedPatient();
                }
            }
        });

        loadPatients();
    }

    private void loadPatients() {
        try {
            PatientDAO dao = new PatientDAO();
            List<Patient> list = dao.findAll();
            model.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (Patient p : list) {
                model.addRow(new Object[]{
                        p.getId(),
                        p.getFirstName(),
                        p.getLastName(),
                        p.getDob() != null ? p.getDob().format(fmt) : "",
                        p.getContact()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load patients: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPatient() {
        Patient p = showPatientFormDialog(null);
        if (p == null) return;
        try {
            PatientDAO dao = new PatientDAO();
            dao.create(p);
            loadPatients();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save patient: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedPatient() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient to edit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Integer id = (Integer) model.getValueAt(sel, 0);
        try {
            PatientDAO dao = new PatientDAO();
            Optional<Patient> op = dao.findById(id);
            if (op.isPresent()) {
                Patient updated = showPatientFormDialog(op.get());
                if (updated != null) {
                    boolean ok = dao.update(updated);
                    if (!ok) {
                        JOptionPane.showMessageDialog(this, "Update failed (patient may not exist).", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                    loadPatients();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selected patient not found.", "Warning", JOptionPane.WARNING_MESSAGE);
                loadPatients();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to edit patient: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a patient to delete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Integer id = (Integer) model.getValueAt(sel, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected patient (ID=" + id + ")?", "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            PatientDAO dao = new PatientDAO();
            boolean ok = dao.delete(id);
            if (ok) loadPatients();
            else JOptionPane.showMessageDialog(this, "Delete failed (patient may not exist).", "Warning", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete patient: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows a modal dialog to add or edit a patient.
     * If 'existing' is null -> add mode. Otherwise edit mode (pre-filled).
     * Returns a Patient object to be saved, or null if user cancelled or validation failed.
     */
    private Patient showPatientFormDialog(Patient existing) {
        // Optimized field sizes for typical 1080p screens
        JTextField tfFirst = new JTextField(22);
        JTextField tfLast = new JTextField(22);
        JTextField tfDob = new JTextField(12); // yyyy-mm-dd
        JTextField tfContact = new JTextField(22);

        JTextArea taHistory = new JTextArea(5, 22);
        taHistory.setLineWrap(true);
        taHistory.setWrapStyleWord(true);

        if (existing != null) {
            tfFirst.setText(existing.getFirstName());
            tfLast.setText(existing.getLastName());
            tfDob.setText(existing.getDob() != null ? existing.getDob().toString() : "");
            tfContact.setText(existing.getContact());
            taHistory.setText(existing.getMedicalHistory());
        }

        // --- Form layout ---
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("First name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(tfFirst, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0;
        form.add(new JLabel("Last name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(tfLast, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0;
        form.add(new JLabel("DOB (yyyy-mm-dd):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(tfDob, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.weightx = 0;
        form.add(new JLabel("Contact:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(tfContact, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0;
        form.add(new JLabel("Medical history:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;

        JScrollPane historyScroll = new JScrollPane(taHistory);
        historyScroll.setPreferredSize(new Dimension(380, 110)); // tuned for 1920x1080
        form.add(historyScroll, gbc);

        // Buttons
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);

        // --- Dialog Setup ---
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, existing == null ? "Add Patient" : "Edit Patient", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(form, BorderLayout.CENTER);
        dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null); // center on screen

        final boolean[] okPressed = {false};

        btnOk.addActionListener(e -> {
            okPressed[0] = true;
            dialog.dispose();
        });
        btnCancel.addActionListener(e -> {
            okPressed[0] = false;
            dialog.dispose();
        });

        dialog.setVisible(true);

        if (!okPressed[0]) return null;

        // --- Validate ---
        String first = tfFirst.getText().trim();
        String last = tfLast.getText().trim();
        String dobStr = tfDob.getText().trim();
        String contact = tfContact.getText().trim();
        String history = taHistory.getText().trim();

        if (first.isEmpty() || last.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First name and last name are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Patient p = existing == null ? new Patient() : existing;
        p.setFirstName(first);
        p.setLastName(last);

        if (!dobStr.isEmpty()) {
            try {
                p.setDob(java.time.LocalDate.parse(dobStr));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid DOB format. Use yyyy-mm-dd.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } else {
            p.setDob(null);
        }

        p.setContact(contact);
        p.setMedicalHistory(history);
        return p;
    }
}
