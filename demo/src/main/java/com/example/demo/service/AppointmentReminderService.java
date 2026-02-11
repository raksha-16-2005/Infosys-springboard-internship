package com.example.demo.service;

import com.example.demo.model.Appointment;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentReminderService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public AppointmentReminderService(AppointmentRepository appointmentRepository, EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedDelay = 60000)
    public void sendUpcomingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inOneHour = now.plusHours(1);

        List<Appointment> upcoming = appointmentRepository.findAll().stream()
                .filter(a -> !a.isReminderSent())
                .filter(a -> a.getStatus() == Appointment.Status.SCHEDULED)
                .filter(a -> a.getAppointmentDate() != null &&
                        !a.getAppointmentDate().isBefore(now) &&
                        !a.getAppointmentDate().isAfter(inOneHour))
                .toList();

        for (Appointment a : upcoming) {
            try {
                String email = a.getPatient().getUser().getEmail();
                String patientName = a.getPatient().getFullName();
                String doctorName = a.getDoctor().getFullName();
                emailService.sendAppointmentReminderEmail(email, patientName, doctorName, a.getAppointmentDate());
                a.setReminderSent(true);
                appointmentRepository.save(a);
            } catch (Exception ex) {
                System.err.println("Failed to send reminder for appointment " + a.getId() + ": " + ex.getMessage());
            }
        }
    }
}

