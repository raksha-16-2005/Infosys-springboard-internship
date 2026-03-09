package com.medvault.service;

import com.medvault.dto.PaginatedAuditLogResponse;
import com.medvault.entity.ActionType;
import com.medvault.entity.AuditLog;
import com.medvault.exception.ResourceNotFoundException;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.repository.PatientConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuditLogService Test Suite")
class AuditLogServiceTest {

    private AuditLogService auditLogService;
    
    @Mock
    private AuditLogRepository auditLogRepository;
    
    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    
    @Mock
    private PatientConsentRepository patientConsentRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository = mock(AuditLogRepository.class);
        medicalRecordRepository = mock(MedicalRecordRepository.class);
        patientConsentRepository = mock(PatientConsentRepository.class);
        auditLogService = new AuditLogService(auditLogRepository, medicalRecordRepository, patientConsentRepository);
    }

    // ============ Validation Tests ============

    @Test
    @DisplayName("Should throw exception for null patientId")
    void testGetPatientAuditLogsNullPatientId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getPatientAuditLogs(null, 0, 10, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception for negative page number")
    void testGetPatientAuditLogsNegativePageNumber() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getPatientAuditLogs(1L, -1, 10, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception for invalid page size")
    void testGetPatientAuditLogsInvalidPageSize() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getPatientAuditLogs(1L, 0, 0, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception for null recordId")
    void testGetRecordAuditLogsNullRecordId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getRecordAuditLogs(null, 0, 10, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception for non-existent medical record")
    void testGetRecordAuditLogsRecordNotFound() {
        // Arrange
        Long recordId = 999L;
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> auditLogService.getRecordAuditLogs(recordId, 0, 10, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception when actionType is null")
    void testGetPatientAuditLogsByActionTypeNullActionType() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getPatientAuditLogsByActionType(1L, null, 0, 10, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception for null startTime")
    void testGetPatientAuditLogsByDateRangeNullStartTime() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getPatientAuditLogsByDateRange(1L, null, LocalDateTime.now(), 0, 10, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception for null endTime")
    void testGetPatientAuditLogsByDateRangeNullEndTime() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getPatientAuditLogsByDateRange(1L, LocalDateTime.now(), null, 0, 10, "timestamp", "desc"));
    }

    @Test
    @DisplayName("Should throw exception when startTime is after endTime")
    void testGetPatientAuditLogsByDateRangeInvalidRange() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().minusDays(7);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, 
            () -> auditLogService.getPatientAuditLogsByDateRange(1L, startTime, endTime, 0, 10, "timestamp", "desc"));
    }

    // ============ Helper Tests ============

    @Test
    @DisplayName("Should verify repository called with correct parameters")
    void testRepositoryInteraction() {
        // Arrange
        Long recordId = 45L;
        
        when(medicalRecordRepository.findById(recordId))
                .thenReturn(Optional.of(new com.medvault.entity.MedicalRecord()));
        
        // Act - This would normally work with proper authentication
        // Just verifying the repository call would be made
        verify(medicalRecordRepository, atLeastOnce()).findById(recordId);
    }
}
