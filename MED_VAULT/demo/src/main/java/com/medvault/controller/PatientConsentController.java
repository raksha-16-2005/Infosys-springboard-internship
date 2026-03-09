package com.medvault.controller;

import com.medvault.dto.ApiResponse;
import com.medvault.entity.PatientConsent;
import com.medvault.service.PatientConsentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consent")
@CrossOrigin(origins = "*")
public class PatientConsentController {

    private final PatientConsentService patientConsentService;

    public PatientConsentController(PatientConsentService patientConsentService) {
        this.patientConsentService = patientConsentService;
    }

    /**
     * Grant consent for a doctor to access patient records
     * POST /api/consent/grant
     */
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @PostMapping("/grant")
    public ResponseEntity<ApiResponse<PatientConsent>> grantConsent(
            @RequestParam Long patientId,
            @RequestParam Long doctorId,
            @RequestParam(required = false) String reason) {

        PatientConsent consent = patientConsentService.grantConsent(patientId, doctorId, reason);
        return ResponseEntity.ok(ApiResponse.success(consent, "Consent granted successfully for doctor to access patient records"));
    }

    /**
     * Revoke consent for a doctor to access patient records
     * POST /api/consent/revoke
     */
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN')")
    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<PatientConsent>> revokeConsent(
            @RequestParam Long patientId,
            @RequestParam Long doctorId) {

        PatientConsent consent = patientConsentService.revokeConsent(patientId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(consent, "Consent revoked successfully"));
    }

    /**
     * Get consent details for a patient-doctor pair
     * GET /api/consent?patientId={patientId}&doctorId={doctorId}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PatientConsent>> getConsent(
            @RequestParam Long patientId,
            @RequestParam Long doctorId) {

        PatientConsent consent = patientConsentService.getConsent(patientId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(consent, "Consent details retrieved successfully"));
    }

    /**
     * Check if a doctor has active consent from a patient
     * GET /api/consent/check?patientId={patientId}&doctorId={doctorId}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveConsent(
            @RequestParam Long patientId,
            @RequestParam Long doctorId) {

        boolean hasConsent = patientConsentService.hasActiveConsent(patientId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(hasConsent, "Consent status retrieved successfully"));
    }
}
