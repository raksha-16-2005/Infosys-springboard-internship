package com.medvault.service;

import com.medvault.dto.MedicalRecordResponse;
import com.medvault.entity.ActionType;
import com.medvault.entity.AuditLog;
import com.medvault.entity.MedicalRecord;
import com.medvault.entity.RecordCategory;
import com.medvault.exception.ResourceNotFoundException;
import com.medvault.exception.UnauthorizedAccessException;
import com.medvault.mapper.MedicalRecordMapper;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.repository.PatientConsentRepository;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientConsentRepository patientConsentRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final MedicalRecordMapper medicalRecordMapper;

    @Autowired
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PatientConsentRepository patientConsentRepository,
                                AuditLogRepository auditLogRepository,
                                UserRepository userRepository,
                                FileStorageService fileStorageService,
                                MedicalRecordMapper medicalRecordMapper) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientConsentRepository = patientConsentRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.medicalRecordMapper = medicalRecordMapper;
    }

    /**
     * Get all active records for a patient, sorted by upload date descending
     */
    public List<MedicalRecordResponse> getRecordsForPatient(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        List<MedicalRecord> records = medicalRecordRepository
                .findByPatientIdAndIsDeletedFalseAndIsActiveTrueOrderByUploadDateDesc(patientId);

        saveAuditLog(null, patientId, authContext.userId(), authContext.role(), ActionType.VIEW, "Retrieved all active records for patient");
        return medicalRecordMapper.toResponseList(records);
    }

    /**
     * Get records for a patient filtered by category, sorted by upload date descending
     */
    public List<MedicalRecordResponse> getRecordsByCategory(Long patientId, RecordCategory category) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        List<MedicalRecord> records = medicalRecordRepository
                .findByPatientIdAndCategoryAndIsDeletedFalseAndIsActiveTrueOrderByUploadDateDesc(patientId, category);

        saveAuditLog(null, patientId, authContext.userId(), authContext.role(), ActionType.VIEW, "Retrieved records by category: " + category);
        return medicalRecordMapper.toResponseList(records);
    }

    /**
     * Get records for a patient within a date range, sorted by upload date descending
     */
    public List<MedicalRecordResponse> getRecordsByDateRange(Long patientId,
                                                               LocalDateTime startDate, 
                                                               LocalDateTime endDate) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        List<MedicalRecord> records = medicalRecordRepository
                .findByPatientIdAndDateRange(patientId, startDate, endDate);

        saveAuditLog(null, patientId, authContext.userId(), authContext.role(), ActionType.VIEW, "Retrieved records in date range: " + startDate + " to " + endDate);
        return medicalRecordMapper.toResponseList(records);
    }

    /**
     * Get records for a patient by category and date range, sorted by upload date descending
     */
    public List<MedicalRecordResponse> getRecordsByCategoryAndDateRange(Long patientId,
                                                                          RecordCategory category,
                                                                          LocalDateTime startDate,
                                                                          LocalDateTime endDate) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate and endDate cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        List<MedicalRecord> records = medicalRecordRepository
                .findByPatientIdAndCategoryAndDateRange(patientId, category, startDate, endDate);

        saveAuditLog(null, patientId, authContext.userId(), authContext.role(), ActionType.VIEW, "Retrieved records by category and date range");
        return medicalRecordMapper.toResponseList(records);
    }

    /**
     * Get a single record by ID
     */
    public MedicalRecordResponse getRecordById(Long recordId) {
        if (recordId == null) {
            throw new IllegalArgumentException("recordId cannot be null");
        }

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + recordId));

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(record.getPatientId(), authContext.userId(), authContext.role());

        saveAuditLog(recordId, record.getPatientId(), authContext.userId(), authContext.role(), ActionType.VIEW, "Viewed record: " + record.getFileName());
        return medicalRecordMapper.toResponse(record);
    }

    /**
     * Upload a new medical record with file and notes
     */
    @Transactional
    @SuppressWarnings("null")
    public MedicalRecordResponse uploadRecord(Long patientId, MultipartFile file,
                                               RecordCategory category, String notes) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file cannot be null or empty");
        }
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        Long storageRecordId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        
        // Store file
        String fileUrl = fileStorageService.storeFile(file, patientId, storageRecordId);

        // Create record
        MedicalRecord record = Objects.requireNonNull(MedicalRecord.builder()
                .patientId(patientId)
                .uploadedBy(authContext.userId())
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileUrl(fileUrl)
                .category(category)
                .notes(notes)
                .versionNumber(1)
                .isActive(true)
                .isDeleted(false)
                .uploadDate(LocalDateTime.now())
                .build());

        medicalRecordRepository.save(record);
        saveAuditLog(record.getId(), patientId, authContext.userId(), authContext.role(), ActionType.UPLOAD, "Uploaded file: " + record.getFileName() + ", Category: " + category);
        
        return medicalRecordMapper.toResponse(record);
    }

    /**
     * Update an existing medical record (creates new version)
     */
    @Transactional
    @SuppressWarnings("null")
    public MedicalRecordResponse updateRecord(Long recordId, MultipartFile file, String notes) {
        if (recordId == null) {
            throw new IllegalArgumentException("recordId cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();

        MedicalRecord oldRecord = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + recordId));

        authorizePatientAccess(oldRecord.getPatientId(), authContext.userId(), authContext.role());

        // Mark old record as inactive
        oldRecord.setIsActive(false);
        medicalRecordRepository.save(oldRecord);

        // Create new version
        Long storageRecordId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        String fileUrl = oldRecord.getFileUrl();
        String fileName = oldRecord.getFileName();
        String fileType = oldRecord.getFileType();
        
        // If new file is provided, store it
        if (file != null && !file.isEmpty()) {
            fileUrl = fileStorageService.storeFile(file, oldRecord.getPatientId(), storageRecordId);
            fileName = file.getOriginalFilename();
            fileType = file.getContentType();
        }

        MedicalRecord newRecord = Objects.requireNonNull(MedicalRecord.builder()
                .patientId(oldRecord.getPatientId())
                .uploadedBy(authContext.userId())
                .fileName(fileName)
                .fileType(fileType)
                .fileUrl(fileUrl)
                .category(oldRecord.getCategory())
                .notes(notes != null ? notes : oldRecord.getNotes())
                .versionNumber(oldRecord.getVersionNumber() + 1)
                .isActive(true)
                .isDeleted(false)
                .uploadDate(LocalDateTime.now())
                .build());

        medicalRecordRepository.save(newRecord);
        saveAuditLog(newRecord.getId(), oldRecord.getPatientId(), authContext.userId(), authContext.role(), ActionType.UPDATE, "Updated file: " + newRecord.getFileName() + ", Version: " + newRecord.getVersionNumber());
        
        return medicalRecordMapper.toResponse(newRecord);
    }

    /**
     * Soft delete a medical record (admin only)
     */
    @Transactional
    public void deleteRecord(Long recordId) {
        if (recordId == null) {
            throw new IllegalArgumentException("recordId cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        if (!"ADMIN".equals(authContext.role())) {
            throw new UnauthorizedAccessException("Only admins can delete medical records");
        }

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + recordId));

        record.setIsDeleted(true);
        medicalRecordRepository.save(record);

        saveAuditLog(recordId, record.getPatientId(), authContext.userId(), authContext.role(), ActionType.DELETE, "Deleted record: " + record.getFileName());
    }

    // ========== Private Helper Methods ==========

    private AuthContext getCurrentAuthContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }

        String username = authentication.getName();
        String role = normalizeRole(extractUserRole(authentication));
        Long userId = resolveUserId(username);
        return new AuthContext(userId, role);
    }

    private void authorizePatientAccess(Long patientId, Long currentUserId, String role) {
        if ("PATIENT".equals(role)) {
            if (!currentUserId.equals(patientId)) {
                throw new UnauthorizedAccessException("Patients can only access their own records");
            }
            return;
        }

        if ("DOCTOR".equals(role)) {
            validateActiveConsent(patientId, currentUserId);
            return;
        }

        if (!"ADMIN".equals(role)) {
            throw new UnauthorizedAccessException("Access denied");
        }
    }

    private void validateActiveConsent(Long patientId, Long doctorId) {
        var consent = patientConsentRepository.findByPatientIdAndDoctorId(patientId, doctorId);
        if (consent.isEmpty() || !Boolean.TRUE.equals(consent.get().getConsentGranted()) || consent.get().getRevokedAt() != null) {
            throw new UnauthorizedAccessException("No active consent found");
        }
    }

    @SuppressWarnings("null")
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

    private Long resolveUserId(String username) {
        if (username == null || username.isBlank()) {
            throw new UnauthorizedAccessException("Unable to extract username from authentication");
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get().getId();
        }

        // Fallback for legacy tokens where subject may already be numeric user ID
        return convertToLong(username);
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
