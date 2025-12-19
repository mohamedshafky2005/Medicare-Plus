package edu.syntaxsquad.medicare.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private static final String DB_FOLDER = "data";
    private static final String DB_FILE = DB_FOLDER + "/medicare.db";
    private static final String URL = "jdbc:sqlite:" + DB_FILE;

    // --------------------------------------------------
    // Get DB connection
    // --------------------------------------------------
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // --------------------------------------------------
    // Initialize database & tables
    // --------------------------------------------------
    public static void initDatabase() throws Exception {

        // Ensure data folder exists
        Path folder = Path.of(DB_FOLDER);
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }

        // Open connection (creates DB file if not exists)
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            // ---------------- PATIENTS ----------------
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS patients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    dob TEXT,
                    contact TEXT,
                    medical_history TEXT
                );
            """);

            // ---------------- DOCTORS ----------------
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS doctors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    first_name TEXT NOT NULL,
                    last_name TEXT NOT NULL,
                    specialty TEXT NOT NULL,
                    contact TEXT,
                    working_hours TEXT
                );
            """);

            // ---------------- APPOINTMENTS ----------------
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS appointments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER NOT NULL,
                    doctor_id INTEGER NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    status TEXT CHECK(status IN ('SCHEDULED','COMPLETED','CANCELLED','DELAYED')),
                    urgency INTEGER DEFAULT 0,
                    notes TEXT,
                    FOREIGN KEY (patient_id) REFERENCES patients(id),
                    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
                );
            """);

            // ---------------- PATIENT NOTIFICATIONS ----------------
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS patient_notifications (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "patient_id INTEGER NOT NULL," +
                            "message TEXT NOT NULL," +
                            "created_at DATETIME DEFAULT (datetime('now','localtime'))" +
                            ");"
            );


            // ---------------- DOCTOR NOTIFICATIONS ----------------
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS doctor_notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    doctor_id INTEGER NOT NULL,
                    message TEXT NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
                );
            """);

            // ---------------- INDEXES (PERFORMANCE) ----------------
            st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_appt_patient
                ON appointments(patient_id);
            """);

            st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_appt_doctor
                ON appointments(doctor_id);
            """);

            st.executeUpdate("""
                CREATE INDEX IF NOT EXISTS idx_doc_notify
                ON doctor_notifications(doctor_id);
            """);
        }
    }
}
