package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your MeddVault OTP Code");
        message.setText("Your OTP code is: " + otpCode + "\n\nThis code is valid for 5 minutes.");

        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            // For development: log OTP so you can still test even if email fails
            System.err.println("Failed to send OTP email: " + ex.getMessage());
            System.out.println("DEV OTP for " + toEmail + " is: " + otpCode);
        }
    }

    public void sendResetEmail(String toEmail, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password reset");
        message.setText("Reset your password: " + link + "\nThis link is valid for 15 minutes.");
        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }
        try { mailSender.send(message); } catch (Exception ex) {
            System.err.println("Failed to send reset email: " + ex.getMessage());
            System.out.println("DEV reset link for " + toEmail + " is: " + link);
        }
    }

    public void sendAppointmentBookedEmail(String toEmail, String patientName, String doctorName, java.time.LocalDateTime appointmentTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("MedVault - Appointment booked successfully");
        message.setText(String.format(
                "Hi %s,\n\nYour appointment with Dr. %s is booked for %s.\n\nThank you,\nMedVault",
                patientName != null ? patientName : "Patient",
                doctorName != null ? doctorName : "Doctor",
                appointmentTime != null ? appointmentTime.toString() : "N/A"
        ));
        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            System.err.println("Failed to send appointment booked email: " + ex.getMessage());
        }
    }

    public void sendDoctorAppointmentRequestEmail(String toEmail, String doctorName, String patientName, java.time.LocalDateTime appointmentTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("MedVault - New appointment request");
        message.setText(String.format(
                "Hi Dr. %s,\n\nYou have a new appointment request from %s for %s.\n\nPlease review it in your dashboard.\n\nThanks,\nMedVault",
                doctorName != null ? doctorName : "Doctor",
                patientName != null ? patientName : "Patient",
                appointmentTime != null ? appointmentTime.toString() : "N/A"
        ));
        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            System.err.println("Failed to send doctor appointment request email: " + ex.getMessage());
        }
    }
}

