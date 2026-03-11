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

    public void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        if (fromAddress != null && !fromAddress.isEmpty()) {
            message.setFrom(fromAddress);
        }

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            System.err.println("Failed to send email: " + ex.getMessage());
            ex.printStackTrace();
            System.out.println("DEV email fallback -> to: " + toEmail + ", subject: " + subject + ", body: " + body);
        }
    }
}

