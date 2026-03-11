package com.example.demo.service;

import com.example.demo.dto.DoctorConsentDTO;
import com.example.demo.model.DoctorAccessConsent;
import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorAccessConsentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DoctorConsentService {

    private final DoctorAccessConsentRepository consentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public DoctorConsentService(DoctorAccessConsentRepository consentRepository,
                                UserRepository userRepository,
                                NotificationService notificationService,
                                EmailService emailService) {
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    public DoctorConsentDTO grantConsent(User patientUser, Long doctorId, String reason) {
        if (patientUser == null) {
            throw new IllegalArgumentException("Patient user is required");
        }
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId is required");
        }

        User doctor = getDoctorUserOrThrow(doctorId);

        DoctorAccessConsent consent = consentRepository.findByPatientAndDoctor(patientUser, doctor)
                .orElseGet(() -> {
                    DoctorAccessConsent newConsent = new DoctorAccessConsent();
                    newConsent.setPatient(patientUser);
                    newConsent.setDoctor(doctor);
                    return newConsent;
                });

        LocalDateTime now = LocalDateTime.now();
        consent.setGranted(true);
        consent.setGrantedAt(now);
        consent.setRevokedAt(null);
        consent.setReason(reason);

        DoctorConsentDTO saved = toDto(consentRepository.save(consent));
        System.out.println("Consent granted for patientId=" + patientUser.getId() + " doctorId=" + doctor.getId());
        
        // Create notifications for both doctor and patient
        try {
            String doctorName = getDisplayName(doctor);
            String patientName = getDisplayName(patientUser);
            
            // Notify doctor
            notificationService.createNotification(
                    doctor.getId(),
                    Notification.NotificationType.SYSTEM_NOTIFICATION,
                    "Access granted: " + patientName + " has granted you access to their medical records."
            );
            System.out.println("[CONSENT-GRANT] Doctor notification created");
            
            // Notify patient
            notificationService.createNotification(
                    patientUser.getId(),
                    Notification.NotificationType.SYSTEM_NOTIFICATION,
                    "Consent updated: You granted access to Dr. " + doctorName + "."
            );
            System.out.println("[CONSENT-GRANT] Patient notification created");
            
            // Send email notifications
            if (doctor.getEmail() != null && !doctor.getEmail().isBlank()) {
                emailService.sendSimpleEmail(
                        doctor.getEmail(),
                        "Medical Records Access Granted",
                        "Patient " + patientName + " has granted you access to their medical records."
                );
                System.out.println("[CONSENT-GRANT] Doctor email sent");
            }
            
            if (patientUser.getEmail() != null && !patientUser.getEmail().isBlank()) {
                emailService.sendSimpleEmail(
                        patientUser.getEmail(),
                        "Consent Granted",
                        "You have granted access to Dr. " + doctorName + " for your medical records."
                );
                System.out.println("[CONSENT-GRANT] Patient email sent");
            }
        } catch (Exception e) {
            System.err.println("[CONSENT-GRANT-ERROR] Failed to send notifications/emails: " + e.getMessage());
            e.printStackTrace();
        }
        
        return saved;
    }

    public DoctorConsentDTO revokeConsent(User patientUser, Long doctorId, String reason) {
        if (patientUser == null) {
            throw new IllegalArgumentException("Patient user is required");
        }
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId is required");
        }

        User doctor = getDoctorUserOrThrow(doctorId);
        DoctorAccessConsent consent = consentRepository.findByPatientAndDoctor(patientUser, doctor)
                .orElseThrow(() -> new IllegalArgumentException("Consent not found for selected doctor"));

        consent.setGranted(false);
        consent.setRevokedAt(LocalDateTime.now());
        if (reason != null && !reason.isBlank()) {
            consent.setReason(reason);
        }

        DoctorConsentDTO saved = toDto(consentRepository.save(consent));
        System.out.println("Consent revoked for patientId=" + patientUser.getId() + " doctorId=" + doctor.getId());
        
        // Create notifications for both doctor and patient
        try {
            String doctorName = getDisplayName(doctor);
            String patientName = getDisplayName(patientUser);
            
            // Notify doctor
            notificationService.createNotification(
                    doctor.getId(),
                    Notification.NotificationType.SYSTEM_NOTIFICATION,
                    "Access revoked: " + patientName + " has revoked your access to their medical records."
            );
            System.out.println("[CONSENT-REVOKE] Doctor notification created");
            
            // Notify patient
            notificationService.createNotification(
                    patientUser.getId(),
                    Notification.NotificationType.SYSTEM_NOTIFICATION,
                    "Consent updated: You revoked access from Dr. " + doctorName + "."
            );
            System.out.println("[CONSENT-REVOKE] Patient notification created");
            
            // Send email notifications
            if (doctor.getEmail() != null && !doctor.getEmail().isBlank()) {
                emailService.sendSimpleEmail(
                        doctor.getEmail(),
                        "Medical Records Access Revoked",
                        "Patient " + patientName + " has revoked your access to their medical records."
                );
                System.out.println("[CONSENT-REVOKE] Doctor email sent");
            }
            
            if (patientUser.getEmail() != null && !patientUser.getEmail().isBlank()) {
                emailService.sendSimpleEmail(
                        patientUser.getEmail(),
                        "Consent Revoked",
                        "You have revoked access from Dr. " + doctorName + " for your medical records."
                );
                System.out.println("[CONSENT-REVOKE] Patient email sent");
            }
        } catch (Exception e) {
            System.err.println("[CONSENT-REVOKE-ERROR] Failed to send notifications/emails: " + e.getMessage());
            e.printStackTrace();
        }
        
        return saved;
    }

    public List<DoctorConsentDTO> listConsents(User patientUser) {
        if (patientUser == null) {
            throw new IllegalArgumentException("Patient user is required");
        }

        return consentRepository.findByPatientOrderByUpdatedAtDesc(patientUser)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public boolean hasActiveConsent(User patientUser, User doctorUser) {
        if (patientUser == null || doctorUser == null) {
            return false;
        }
        return consentRepository.existsByPatientAndDoctorAndGrantedTrueAndRevokedAtIsNull(patientUser, doctorUser);
    }

    private User getDoctorUserOrThrow(Long doctorId) {
        Long safeDoctorId = Objects.requireNonNull(doctorId, "doctorId is required");
        User doctor = userRepository.findById(safeDoctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!isDoctorRole(doctor.getRole())) {
            throw new IllegalArgumentException("Selected user is not a doctor");
        }
        return doctor;
    }

    private boolean isDoctorRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        return "DOCTOR".equalsIgnoreCase(role) || "ROLE_DOCTOR".equalsIgnoreCase(role);
    }

    private DoctorConsentDTO toDto(DoctorAccessConsent consent) {
        DoctorConsentDTO dto = new DoctorConsentDTO();
        dto.setId(consent.getId());
        dto.setPatientId(consent.getPatient() != null ? consent.getPatient().getId() : null);
        dto.setDoctorId(consent.getDoctor() != null ? consent.getDoctor().getId() : null);
        dto.setDoctorName(consent.getDoctor() != null
                ? (consent.getDoctor().getFullName() != null && !consent.getDoctor().getFullName().isBlank()
                        ? consent.getDoctor().getFullName()
                        : consent.getDoctor().getUsername())
                : null);
        dto.setGranted(consent.getGranted());
        dto.setGrantedAt(consent.getGrantedAt());
        dto.setRevokedAt(consent.getRevokedAt());
        dto.setReason(consent.getReason());
        dto.setUpdatedAt(consent.getUpdatedAt());
        return dto;
    }

    private String getDisplayName(User user) {
        if (user == null) {
            return "Unknown";
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        return user.getUsername();
    }
}