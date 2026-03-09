package com.medvault.service;

import com.medvault.entity.ActionType;
import com.medvault.entity.AuditLog;
import com.medvault.entity.PatientConsent;
import com.medvault.exception.ResourceNotFoundException;
import com.medvault.exception.UnauthorizedAccessException;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.PatientConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PatientConsentService Test Suite")
class PatientConsentServiceTest {

    private PatientConsentService patientConsentService;
    
    @Mock
    private PatientConsentRepository patientConsentRepository;
    
    @Mock
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        patientConsentRepository = mock(PatientConsentRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        patientConsentService = new PatientConsentService(patientConsentRepository, auditLogRepository);
    }

    // ============ Check Active Consent Tests ============

    @Test
    @DisplayName("Should return true when doctor has active consent")
    void testHasActiveConsentTrue() {
        // Arrange
        Long patientId = 1L;
        Long doctorId = 2L;
        
        PatientConsent consent = new PatientConsent();
        consent.setPatientId(patientId);
        consent.setDoctorId(doctorId);
        consent.setConsentGranted(true);
        consent.setRevokedAt(null);
        
        when(patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId))
                .thenReturn(Optional.of(consent));
        
        // Act
        boolean result = patientConsentService.hasActiveConsent(patientId, doctorId);
        
        // Assert
        assertTrue(result);
        verify(patientConsentRepository, times(1)).findByPatientIdAndDoctorId(patientId, doctorId);
    }

    @Test
    @DisplayName("Should return false when consent is revoked")
    void testHasActiveConsentRevoked() {
        // Arrange
        Long patientId = 1L;
        Long doctorId = 2L;
        
        PatientConsent consent = new PatientConsent();
        consent.setPatientId(patientId);
        consent.setDoctorId(doctorId);
        consent.setConsentGranted(true);
        consent.setRevokedAt(LocalDateTime.now());
        
        when(patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId))
                .thenReturn(Optional.of(consent));
        
        // Act
        boolean result = patientConsentService.hasActiveConsent(patientId, doctorId);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when consent does not exist")
    void testHasActiveConsentNotFound() {
        // Arrange
        Long patientId = 1L;
        Long doctorId = 2L;
        
        when(patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId))
                .thenReturn(Optional.empty());
        
        // Act
        boolean result = patientConsentService.hasActiveConsent(patientId, doctorId);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when patientId is null")
    void testHasActiveConsentNullPatient() {
        // Act
        boolean result = patientConsentService.hasActiveConsent(null, 2L);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when doctorId is null")
    void testHasActiveConsentNullDoctor() {
        // Act
        boolean result = patientConsentService.hasActiveConsent(1L, null);
        
        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should throw exception when patientId is null for grant")
    void testGrantConsentNullPatientId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> patientConsentService.grantConsent(null, 2L, "reason"));
    }

    @Test
    @DisplayName("Should throw exception when doctorId is null for grant")
    void testGrantConsentNullDoctorId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> patientConsentService.grantConsent(1L, null, "reason"));
    }

    @Test
    @DisplayName("Should throw exception when patientId is null for revoke")
    void testRevokeConsentNullPatientId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> patientConsentService.revokeConsent(null, 2L));
    }

    @Test
    @DisplayName("Should throw exception when doctorId is null for revoke")
    void testRevokeConsentNullDoctorId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> patientConsentService.revokeConsent(1L, null));
    }

    @Test
    @DisplayName("Should throw exception when patientId is null for get")
    void testGetConsentNullPatientId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> patientConsentService.getConsent(null, 2L));
    }

    @Test
    @DisplayName("Should throw exception when doctorId is null for get")
    void testGetConsentNullDoctorId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> patientConsentService.getConsent(1L, null));
    }
}
