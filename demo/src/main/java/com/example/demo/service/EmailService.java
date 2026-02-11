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

    public void sendAppointmentBookedEmail(String toEmail, String patientName, String doctorName, java.time.LocalDateTime time) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("MedVault - Appointment booked");
        message.setText(String.format(
                "Hi %s,\n\nYour appointment with Dr. %s is booked for %s.\n\nThank you,\nMedVault",
                patientName != null ? patientName : "",
                doctorName != null ? doctorName : "",
                time != null ? time.toString() : "N/A"
        ));
        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            System.err.println("Failed to send appointment email: " + ex.getMessage());
        }
    }

    public void sendDoctorApprovalEmail(String toEmail, String doctorName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("MedVault - Doctor account approved");
        message.setText(String.format(
                "Hi %s,\n\nYour doctor account has been approved by the admin. You can now log in and use the platform.\n\nThank you,\nMedVault",
                doctorName != null ? doctorName : "Doctor"
        ));
        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            System.err.println("Failed to send doctor approval email: " + ex.getMessage());
        }
    }

    public void sendAppointmentReminderEmail(String toEmail, String patientName, String doctorName, java.time.LocalDateTime time) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("MedVault - Upcoming appointment reminder");
        message.setText(String.format(
                "Hi %s,\n\nThis is a reminder for your appointment with Dr. %s at %s.\n\nThank you,\nMedVault",
                patientName != null ? patientName : "",
                doctorName != null ? doctorName : "",
                time != null ? time.toString() : "N/A"
        ));
        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            System.err.println("Failed to send appointment reminder email: " + ex.getMessage());
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
}

