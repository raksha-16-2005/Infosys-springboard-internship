package com.medvault.service;

import com.medvault.entity.ActionType;
import com.medvault.entity.AuditLog;
import com.medvault.entity.PatientConsent;
import com.medvault.exception.ResourceNotFoundException;
import com.medvault.exception.UnauthorizedAccessException;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.PatientConsentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PatientConsentService {

    private final PatientConsentRepository patientConsentRepository;
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public PatientConsentService(PatientConsentRepository patientConsentRepository,
                                AuditLogRepository auditLogRepository) {
        this.patientConsentRepository = patientConsentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Grant consent for a doctor to access patient records
     */
    @Transactional
    @SuppressWarnings("null")
    public PatientConsent grantConsent(Long patientId, Long doctorId, String reason) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        
        // Only the patient themselves or an admin can grant consent for that patient
        if (!authContext.userId().equals(patientId) && !"ADMIN".equals(authContext.role())) {
            throw new UnauthorizedAccessException("Only the patient or an admin can grant consent");
        }

        // Check if consent already exists
        var existingConsent = patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId);
        
        PatientConsent consent;
        if (existingConsent.isPresent()) {
            // Update existing consent
            consent = existingConsent.get();
            consent.setConsentGranted(true);
            consent.setRevokedAt(null);
            consent.setConsentReason(reason != null ? reason : consent.getConsentReason());
        } else {
            // Create new consent
            consent = new PatientConsent();
            consent.setPatientId(patientId);
            consent.setDoctorId(doctorId);
            consent.setConsentGranted(true);
            consent.setGrantedAt(LocalDateTime.now());
            consent.setConsentReason(reason);
        }

        patientConsentRepository.save(consent);

        // Log the consent grant action
        saveAuditLog(null, patientId, authContext.userId(), authContext.role(), 
                    ActionType.CONSENT_GRANTED, "Granted consent to doctor ID: " + doctorId + (reason != null ? ", Reason: " + reason : ""));

        return consent;
    }

    /**
     * Revoke consent for a doctor to access patient records
     */
    @Transactional
    public PatientConsent revokeConsent(Long patientId, Long doctorId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        
        // Only the patient themselves or an admin can revoke consent for that patient
        if (!authContext.userId().equals(patientId) && !"ADMIN".equals(authContext.role())) {
            throw new UnauthorizedAccessException("Only the patient or an admin can revoke consent");
        }

        PatientConsent consent = patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found for patient " + patientId + " and doctor " + doctorId));

        // Mark consent as revoked
        consent.setRevokedAt(LocalDateTime.now());
        patientConsentRepository.save(consent);

        // Log the consent revocation action
        saveAuditLog(null, patientId, authContext.userId(), authContext.role(), 
                    ActionType.CONSENT_REVOKED, "Revoked consent for doctor ID: " + doctorId);

        return consent;
    }

    /**
     * Check if a doctor has active consent from a patient
     */
    @Transactional(readOnly = true)
    public boolean hasActiveConsent(Long patientId, Long doctorId) {
        if (patientId == null || doctorId == null) {
            return false;
        }

        var consent = patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId);
        return consent.isPresent() && 
               Boolean.TRUE.equals(consent.get().getConsentGranted()) && 
               consent.get().getRevokedAt() == null;
    }

    /**
     * Get consent details for a patient-doctor pair
     */
    @Transactional(readOnly = true)
    public PatientConsent getConsent(Long patientId, Long doctorId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (doctorId == null) {
            throw new IllegalArgumentException("doctorId cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        
        // Only the patient themselves, the doctor, or an admin can view consent details
        if (!authContext.userId().equals(patientId) && 
            !authContext.userId().equals(doctorId) && 
            !"ADMIN".equals(authContext.role())) {
            throw new UnauthorizedAccessException("Access denied");
        }

        return patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found for patient " + patientId + " and doctor " + doctorId));
    }

    // ========== Private Helper Methods ==========

    private void saveAuditLog(Long recordId, Long patientId, Long performedBy, String role, ActionType actionType, String details) {
        AuditLog audit = Objects.requireNonNull(AuditLog.builder()
                .recordId(recordId)
                .patientId(patientId)
                .performedBy(performedBy)
                .role(role)
                .actionType(actionType)
                .details(details)
                .build());
        auditLogRepository.save(audit);
    }

    private AuthContext getCurrentAuthContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }

        String username = authentication.getName();
        String role = normalizeRole(extractUserRole(authentication));
        Long userId = convertToLong(username);
        return new AuthContext(userId, role);
    }

    private Long convertToLong(String value) {
        if (value == null || value.isBlank()) {
            throw new UnauthorizedAccessException("Unable to extract user ID from authentication");
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new UnauthorizedAccessException("Unable to convert user identifier to Long: " + value);
        }
    }

    private String extractUserRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }

        return authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    private String normalizeRole(String authority) {
        if (authority == null || authority.isBlank()) {
            throw new UnauthorizedAccessException("User role not found");
        }
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5);
        }
        return authority;
    }

    private record AuthContext(Long userId, String role) {}
}
