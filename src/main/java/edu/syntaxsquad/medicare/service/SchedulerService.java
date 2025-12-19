package edu.syntaxsquad.medicare.service;

import edu.syntaxsquad.medicare.dao.DoctorDAO;
import edu.syntaxsquad.medicare.model.Doctor;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Simple scheduler that assigns a doctor based on:
 * 1) specialty match
 * 2) availability at requested time
 * 3) lowest workload (appointments on same day)
 * 4) fallback: earliest next free doctor
 *
 * Returns Optional<Doctor> (empty when none available).
 */
public class SchedulerService {
    private final DoctorDAO doctorDAO = new DoctorDAO();

    /**
     * Try to pick a doctor for the given specialty and desired slot.
     *
     * @param specialty medical specialty required (exact match)
     * @param desiredStart desired slot start (LocalDateTime)
     * @param durationMinutes appointment length in minutes
     * @param urgency 0=normal, higher = more urgent (affects fallback)
     * @return Optional<Doctor> if assigned
     */
    public Optional<Doctor> assignDoctor(String specialty, LocalDateTime desiredStart, int durationMinutes, int urgency) {
        try {
            LocalDateTime desiredEnd = desiredStart.plusMinutes(durationMinutes);

            // 1) fetch candidate doctors by specialty
            List<Doctor> candidates = doctorDAO.findBySpecialty(specialty);
            if (candidates.isEmpty()) {
                // no one with that specialty
                return Optional.empty();
            }

            // 2) filter by immediate availability
            // pick the doctor with availability and lowest appointments count on that day
            LocalDate day = desiredStart.toLocalDate();
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.atTime(LocalTime.MAX);

            Doctor best = null;
            int bestLoad = Integer.MAX_VALUE;

            for (Doctor d : candidates) {
                boolean available = doctorDAO.isDoctorAvailable(d.getId(), desiredStart, desiredEnd);
                if (available) {
                    int load = doctorDAO.getAppointmentCountBetween(d.getId(), dayStart, dayEnd);
                    if (load < bestLoad) {
                        bestLoad = load;
                        best = d;
                    }
                }
            }

            if (best != null) return Optional.of(best);

            // 3) If none available and urgency high, still pick the doctor with lowest load (they might be bumped)
            if (urgency > 5) {
                Doctor fallback = candidates.stream()
                        .min(Comparator.comparingInt(d -> {
                            try {
                                return doctorDAO.getAppointmentCountBetween(d.getId(), dayStart, dayEnd);
                            } catch (SQLException ex) {
                                return Integer.MAX_VALUE;
                            }
                        }))
                        .orElse(null);
                if (fallback != null) return Optional.of(fallback);
            }

            // 4) Last resort: choose doctor who becomes free the earliest after desiredStart
            Doctor earliest = null;
            LocalDateTime earliestFree = null;
            for (Doctor d : candidates) {
                LocalDateTime nextFree = doctorDAO.getEarliestNextFreeAfter(d.getId(), desiredStart);
                if (nextFree != null) {
                    if (earliestFree == null || nextFree.isBefore(earliestFree)) {
                        earliestFree = nextFree;
                        earliest = d;
                    }
                }
            }
            if (earliest != null) return Optional.of(earliest);

            return Optional.empty();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}
