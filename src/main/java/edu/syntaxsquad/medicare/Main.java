package edu.syntaxsquad.medicare;

import edu.syntaxsquad.medicare.util.DBUtil;
import edu.syntaxsquad.medicare.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                DBUtil.initDatabase(); // creates DB and tables if needed
                MainFrame frame = new MainFrame();
                frame.setTitle("MediCare Plus - Starter");
                frame.setSize(1000, 700);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to start: " + e.getMessage());
            }
        });
    }
}