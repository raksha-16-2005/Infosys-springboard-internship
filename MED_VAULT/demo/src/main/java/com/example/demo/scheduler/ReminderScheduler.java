package com.example.demo.scheduler;

import com.example.demo.model.Appointment;
import com.example.demo.model.Notification;
import com.example.demo.model.PatientProfile;
import com.example.demo.model.Prescription;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PatientProfileRepository;
import com.example.demo.repository.PrescriptionRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientProfileRepository patientProfileRepository;

    public ReminderScheduler(AppointmentRepository appointmentRepository,
                             NotificationRepository notificationRepository,
                             PrescriptionRepository prescriptionRepository,
                             PatientProfileRepository patientProfileRepository) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientProfileRepository = patientProfileRepository;
    }

    /**
     * Send appointment reminders for upcoming appointments (next 24 hours)
     * Runs every hour
     * Idempotent: Only sends reminder if reminderSent is false
     */
    @Scheduled(cron = "0 0 * * * *")  // every hour
    @Transactional
    public void sendAppointmentReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusHours(24);

            // Find upcoming appointments within next 24 hours where reminder hasn't been sent
            List<Appointment> upcomingAppointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getAppointmentDate() != null &&
                            a.getAppointmentDate().isAfter(now) &&
                            a.getAppointmentDate().isBefore(tomorrow) &&
                            (a.getReminderSent() == null || !a.getReminderSent()))
                    .toList();

            for (Appointment appointment : upcomingAppointments) {
                Long patientId = appointment.getPatient() != null && appointment.getPatient().getUser() != null
                        ? appointment.getPatient().getUser().getId()
                        : null;

                if (patientId != null) {
                    String message = "Reminder: You have an appointment at " + appointment.getAppointmentDate();

                    // Idempotency check: avoid creating duplicate unread reminders
                    boolean reminderExists = notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                            patientId,
                            Notification.NotificationType.APPOINTMENT_REMINDER,
                            message
                    );

                    if (!reminderExists) {
                        Notification notification = new Notification(
                                patientId,
                                Notification.NotificationType.APPOINTMENT_REMINDER,
                                message
                        );
                        notificationRepository.save(notification);
                    }

                    // Mark reminder as sent
                    appointment.setReminderSent(true);
                    appointmentRepository.save(appointment);
                }
            }
        } catch (Exception e) {
            // Log error but don't throw to prevent scheduler from stopping
            System.err.println("Error in sendAppointmentReminders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send prescription refill reminders for upcoming follow-up dates
     * Runs every day at 8 AM
     * Idempotent: Checks if reminder already exists before creating
     */
    @Scheduled(cron = "0 0 8 * * *")  // every day at 8 AM
    @Transactional
    public void sendPrescriptionRefillReminders() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);

            // Find all patients
            List<PatientProfile> patients = patientProfileRepository.findAll();

            for (PatientProfile patient : patients) {
                if (patient.getUser() != null) {
                    Long patientId = patient.getUser().getId();

                    // Find prescriptions with follow-up dates approaching (within next 7 days)
                    List<Prescription> prescriptions = prescriptionRepository.findFollowUpsByPatient(patient);

                    for (Prescription prescription : prescriptions) {
                        if (prescription.getFollowUpDate() != null &&
                                !prescription.getFollowUpDate().isBefore(today) &&
                                !prescription.getFollowUpDate().isAfter(nextWeek)) {

                            String message = "Prescription Refill Reminder: Your follow-up prescription visit is on " + prescription.getFollowUpDate();

                            // Idempotency check
                            boolean reminderExists = notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                                    patientId,
                                    Notification.NotificationType.PRESCRIPTION_REFILL_REMINDER,
                                    message
                            );

                            if (!reminderExists) {
                                Notification notification = new Notification(
                                        patientId,
                                        Notification.NotificationType.PRESCRIPTION_REFILL_REMINDER,
                                        message
                                );
                                notificationRepository.save(notification);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in sendPrescriptionRefillReminders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send health checkup reminders for periodic check-ups
     * Runs every day at 9 AM
     * Idempotent: Checks if reminder already exists before creating
     * Rules-based: Send reminder if last appointment was > 6 months ago for completed appointments
     */
    @Scheduled(cron = "0 0 9 * * *")  // every day at 9 AM
    @Transactional
    public void sendHealthCheckupReminders() {
        try {
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

            // Find all patients
            List<PatientProfile> patients = patientProfileRepository.findAll();

            for (PatientProfile patient : patients) {
                if (patient.getUser() != null) {
                    Long patientId = patient.getUser().getId();

                    // Find completed appointments from more than 6 months ago
                    List<Appointment> appointments = appointmentRepository.findAll().stream()
                            .filter(a -> a.getPatient() != null && a.getPatient().getId().equals(patient.getId()) &&
                                    a.getStatus() != null && a.getStatus().equals(Appointment.Status.COMPLETED) &&
                                    a.getAppointmentDate() != null && a.getAppointmentDate().isBefore(sixMonthsAgo))
                            .toList();

                    if (!appointments.isEmpty()) {
                        // Check if we already sent a reminder recently (within last 30 days)
                        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                        List<Notification> recentReminders = notificationRepository.findAll().stream()
                                .filter(n -> n.getUserId().equals(patientId) &&
                                        n.getType() == Notification.NotificationType.HEALTH_CHECKUP_REMINDER &&
                                        n.getCreatedAt() != null && n.getCreatedAt().isAfter(thirtyDaysAgo))
                                .toList();

                        // Only send if no recent reminder exists
                        if (recentReminders.isEmpty()) {
                            String message = "Health Checkup Reminder: It's been more than 6 months since your last checkup. Please schedule an appointment with your doctor.";

                            // Additional idempotency check
                            boolean reminderExists = notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                                    patientId,
                                    Notification.NotificationType.HEALTH_CHECKUP_REMINDER,
                                    message
                            );

                            if (!reminderExists) {
                                Notification notification = new Notification(
                                        patientId,
                                        Notification.NotificationType.HEALTH_CHECKUP_REMINDER,
                                        message
                                );
                                notificationRepository.save(notification);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in sendHealthCheckupReminders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cleanup old notifications (older than 90 days) for all users
     * Runs once daily at 2 AM
     * Idempotent: Just deletes old records, can be run multiple times safely
     */
    @Scheduled(cron = "0 0 2 * * *")  // every day at 2 AM
    @Transactional
    public void cleanupOldNotifications() {
        try {
            LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

            // Delete all notifications older than 90 days
            int deletedCount = 0;
            List<Notification> allNotifications = notificationRepository.findAll();

            for (Notification notification : allNotifications) {
                if (notification.getCreatedAt() != null && notification.getCreatedAt().isBefore(ninetyDaysAgo)) {
                    notificationRepository.delete(notification);
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                System.out.println("Cleanup: Deleted " + deletedCount + " notifications older than 90 days");
            }
        } catch (Exception e) {
            System.err.println("Error in cleanupOldNotifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
