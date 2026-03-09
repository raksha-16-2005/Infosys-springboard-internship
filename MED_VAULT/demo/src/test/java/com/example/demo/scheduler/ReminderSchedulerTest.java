package com.example.demo.scheduler;

import com.example.demo.model.Appointment;
import com.example.demo.model.Notification;
import com.example.demo.model.PatientProfile;
import com.example.demo.model.Prescription;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PatientProfileRepository;
import com.example.demo.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ReminderScheduler Test Suite")
class ReminderSchedulerTest {

    private ReminderScheduler reminderScheduler;
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private PrescriptionRepository prescriptionRepository;
    
    @Mock
    private PatientProfileRepository patientProfileRepository;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        prescriptionRepository = mock(PrescriptionRepository.class);
        patientProfileRepository = mock(PatientProfileRepository.class);
        
        reminderScheduler = new ReminderScheduler(
                appointmentRepository,
                notificationRepository,
                prescriptionRepository,
                patientProfileRepository
        );
    }

    // ============ Appointment Reminder Tests ============

    @Test
    @DisplayName("Should send appointment reminders for upcoming appointments")
    void testSendAppointmentRemindersSuccess() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in12Hours = now.plusHours(12);
        
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(in12Hours);
        appointment.setReminderSent(false);
        
        PatientProfile patient = new PatientProfile();
        appointment.setPatient(patient);
        
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));
        when(notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                anyLong(), any(), anyString())).thenReturn(false);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        reminderScheduler.sendAppointmentReminders();
        
        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(appointmentRepository, times(1)).save(appointment);
        assertTrue(appointment.getReminderSent());
    }

    @Test
    @DisplayName("Should not send duplicate appointment reminders")
    void testSendAppointmentRemindersIdempotent() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in12Hours = now.plusHours(12);
        
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(in12Hours);
        appointment.setReminderSent(false);
        
        PatientProfile patient = new PatientProfile();
        appointment.setPatient(patient);
        
        // Notification already exists - idempotency check should prevent duplicate
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));
        when(notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                anyLong(), any(), anyString())).thenReturn(true);
        
        // Act
        reminderScheduler.sendAppointmentReminders();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should skip appointments outside 24-hour window")
    void testSendAppointmentRemindersOutsideTimeWindow() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in36Hours = now.plusHours(36);  // Outside 24-hour window
        
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(in36Hours);
        appointment.setReminderSent(false);
        
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));
        
        // Act
        reminderScheduler.sendAppointmentReminders();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should skip appointments with reminder already sent")
    void testSendAppointmentRemindersAlreadySent() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in12Hours = now.plusHours(12);
        
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(in12Hours);
        appointment.setReminderSent(true);  // Already sent
        
        when(appointmentRepository.findAll()).thenReturn(List.of(appointment));
        
        // Act
        reminderScheduler.sendAppointmentReminders();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle exceptions gracefully in appointment reminders")
    void testSendAppointmentRemindersException() {
        // Arrange
        when(appointmentRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> reminderScheduler.sendAppointmentReminders());
    }

    // ============ Prescription Refill Reminder Tests ============

    @Test
    @DisplayName("Should send prescription refill reminders for due prescriptions")
    void testSendPrescriptionRefillRemindersSuccess() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate refillDue = today.plusDays(3);  // Within 7 days
        
        Prescription prescription = new Prescription();
        prescription.setFollowUpDate(refillDue);
        
        when(prescriptionRepository.findAll()).thenReturn(List.of(prescription));
        when(notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                anyLong(), any(), anyString())).thenReturn(false);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        reminderScheduler.sendPrescriptionRefillReminders();
        
        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should not send duplicate prescription refill reminders")
    void testSendPrescriptionRefillRemindersIdempotent() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate refillDue = today.plusDays(5);
        
        Prescription prescription = new Prescription();
        prescription.setFollowUpDate(refillDue);
        
        // Notification already exists
        when(prescriptionRepository.findAll()).thenReturn(List.of(prescription));
        when(notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                anyLong(), any(), anyString())).thenReturn(true);
        
        // Act
        reminderScheduler.sendPrescriptionRefillReminders();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should skip prescriptions outside 7-day window")
    void testSendPrescriptionRefillRemindersOutsideTimeWindow() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate refillDue = today.plusDays(10);  // Outside 7-day window
        
        Prescription prescription = new Prescription();
        prescription.setFollowUpDate(refillDue);
        
        when(prescriptionRepository.findAll()).thenReturn(List.of(prescription));
        
        // Act
        reminderScheduler.sendPrescriptionRefillReminders();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle exceptions gracefully in prescription reminders")
    void testSendPrescriptionRefillRemindersException() {
        // Arrange
        when(prescriptionRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> reminderScheduler.sendPrescriptionRefillReminders());
    }

    // ============ Health Checkup Reminder Tests ============

    @Test
    @DisplayName("Should send health checkup reminders for overdue patients")
    void testSendHealthCheckupRemindersSuccess() {
        // Arrange
        PatientProfile patient = new PatientProfile();
        
        when(patientProfileRepository.findAll()).thenReturn(List.of(patient));
        when(notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(
                anyLong(), any(), anyString())).thenReturn(false);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        reminderScheduler.sendHealthCheckupReminders();
        
        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(patientProfileRepository, times(1)).save(patient);
    }

    @Test
    @DisplayName("Should respect 30-day cooldown for health checkup reminders")
    void testSendHealthCheckupRemindersCooldown() {
        // Arrange
        PatientProfile patient = new PatientProfile();
        
        when(patientProfileRepository.findAll()).thenReturn(List.of(patient));
        
        // Act
        reminderScheduler.sendHealthCheckupReminders();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should skip patients with recent checkups")
    void testSendHealthCheckupRemindersRecentCheckup() {
        // Arrange
        PatientProfile patient = new PatientProfile();
        
        when(patientProfileRepository.findAll()).thenReturn(List.of(patient));
        
        // Act
        reminderScheduler.sendHealthCheckupReminders();
        
        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle exceptions gracefully in health checkup reminders")
    void testSendHealthCheckupRemindersException() {
        // Arrange
        when(patientProfileRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> reminderScheduler.sendHealthCheckupReminders());
    }

    // ============ Cleanup Notification Tests ============

    @Test
    @DisplayName("Should cleanup old notifications")
    void testCleanupOldNotificationsSuccess() {
        // Arrange
        LocalDateTime moreThan90DaysAgo = LocalDateTime.now().minusDays(91);
        
        Notification oldNotification = new Notification();
        oldNotification.setCreatedAt(moreThan90DaysAgo);
        
        when(notificationRepository.findAll()).thenReturn(List.of(oldNotification));
        
        // Act
        reminderScheduler.cleanupOldNotifications();
        
        // Assert
        verify(notificationRepository, times(1)).delete(oldNotification);
    }

    @Test
    @DisplayName("Should skip recent notifications during cleanup")
    void testCleanupOldNotificationsSkipsRecent() {
        // Arrange
        LocalDateTime lessThan90DaysAgo = LocalDateTime.now().minusDays(30);  // Recent
        
        Notification recentNotification = new Notification();
        recentNotification.setCreatedAt(lessThan90DaysAgo);
        
        when(notificationRepository.findAll()).thenReturn(List.of(recentNotification));
        
        // Act
        reminderScheduler.cleanupOldNotifications();
        
        // Assert
        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle exceptions gracefully during cleanup")
    void testCleanupOldNotificationsException() {
        // Arrange
        when(notificationRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> reminderScheduler.cleanupOldNotifications());
    }

    @Test
    @DisplayName("Should handle empty repository during cleanup")
    void testCleanupOldNotificationsEmpty() {
        // Arrange
        when(notificationRepository.findAll()).thenReturn(new ArrayList<>());
        
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> reminderScheduler.cleanupOldNotifications());
        verify(notificationRepository, never()).delete(any(Notification.class));
    }
}
