package com.example.demo.service;

import com.example.demo.dto.DoctorConsentDTO;
import com.example.demo.model.DoctorAccessConsent;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorAccessConsentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorConsentService {

    private final DoctorAccessConsentRepository consentRepository;
    private final UserRepository userRepository;

    public DoctorConsentService(DoctorAccessConsentRepository consentRepository,
                                UserRepository userRepository) {
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
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

        return toDto(consentRepository.save(consent));
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

        return toDto(consentRepository.save(consent));
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
        User doctor = userRepository.findById(doctorId)
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
}