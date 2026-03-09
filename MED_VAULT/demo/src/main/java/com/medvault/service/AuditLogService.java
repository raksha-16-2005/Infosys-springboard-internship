package com.medvault.service;

import com.medvault.dto.AuditLogResponse;
import com.medvault.dto.PaginatedAuditLogResponse;
import com.medvault.entity.ActionType;
import com.medvault.entity.AuditLog;
import com.medvault.exception.ResourceNotFoundException;
import com.medvault.exception.UnauthorizedAccessException;
import com.medvault.repository.AuditLogRepository;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.repository.PatientConsentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientConsentRepository patientConsentRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository,
                          MedicalRecordRepository medicalRecordRepository,
                          PatientConsentRepository patientConsentRepository) {
        this.auditLogRepository = auditLogRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientConsentRepository = patientConsentRepository;
    }

    /**
     * Get audit logs for a patient with pagination and sorting
     */
    @Transactional(readOnly = true)
    public PaginatedAuditLogResponse getPatientAuditLogs(
            Long patientId,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection) {

        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        // Validate and set sort direction
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp";

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        Page<AuditLog> auditPage = auditLogRepository.findByPatientIdOrderByTimestampDesc(patientId, pageable);

        return buildPaginatedResponse(auditPage);
    }

    /**
     * Get audit logs for a specific medical record with pagination and sorting
     */
    @Transactional(readOnly = true)
    public PaginatedAuditLogResponse getRecordAuditLogs(
            Long recordId,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection) {

        if (recordId == null) {
            throw new IllegalArgumentException("recordId cannot be null");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }

        // Validate record exists
        var record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + recordId));

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(record.getPatientId(), authContext.userId(), authContext.role());

        // Validate and set sort direction
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp";

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        Page<AuditLog> auditPage = auditLogRepository.findByRecordIdOrderByTimestampDesc(recordId, pageable);

        return buildPaginatedResponse(auditPage);
    }

    /**
     * Get audit logs for a patient filtered by action type with pagination
     */
    @Transactional(readOnly = true)
    public PaginatedAuditLogResponse getPatientAuditLogsByActionType(
            Long patientId,
            ActionType actionType,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection) {

        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (actionType == null) {
            throw new IllegalArgumentException("actionType cannot be null");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp";

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        Page<AuditLog> auditPage = auditLogRepository.findByPatientIdAndActionTypeOrderByTimestampDesc(patientId, actionType, pageable);

        return buildPaginatedResponse(auditPage);
    }

    /**
     * Get audit logs for a patient within a time range with pagination
     */
    @Transactional(readOnly = true)
    public PaginatedAuditLogResponse getPatientAuditLogsByDateRange(
            Long patientId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection) {

        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime and endTime cannot be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp";

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        Page<AuditLog> auditPage = auditLogRepository.findByPatientIdAndTimestampBetweenOrderByTimestampDesc(patientId, startTime, endTime, pageable);

        return buildPaginatedResponse(auditPage);
    }

    /**
     * Get audit logs with advanced filtering
     */
    @Transactional(readOnly = true)
    public PaginatedAuditLogResponse getAuditLogs(
            Long patientId,
            Long recordId,
            ActionType actionType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection) {

        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }
        if (startTime == null) {
            startTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now().plusYears(1);
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }

        AuthContext authContext = getCurrentAuthContext();
        authorizePatientAccess(patientId, authContext.userId(), authContext.role());

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp";

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        Page<AuditLog> auditPage = auditLogRepository.findAuditLogs(patientId, recordId, actionType, startTime, endTime, pageable);

        return buildPaginatedResponse(auditPage);
    }

    /**
     * Get all audit logs for a patient (non-paginated, admin only)
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllPatientAuditLogs(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId cannot be null");
        }

        AuthContext authContext = getCurrentAuthContext();
        if (!"ADMIN".equals(authContext.role())) {
            throw new UnauthorizedAccessException("Only admins can view all audit logs");
        }

        List<AuditLog> auditLogs = auditLogRepository.findByPatientIdOrderByTimestampDesc(patientId);
        return auditLogs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all audit logs across the entire system (admin only)
     */
    @Transactional(readOnly = true)
    public PaginatedAuditLogResponse getAllAuditLogs(
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection) {

        if (pageNumber < 0) {
            throw new IllegalArgumentException("pageNumber must be >= 0");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }

        AuthContext authContext = getCurrentAuthContext();
        if (!"ADMIN".equals(authContext.role())) {
            throw new UnauthorizedAccessException("Only admins can view system audit logs");
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = sortBy != null && !sortBy.isBlank() ? sortBy : "timestamp";

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
        Page<AuditLog> auditPage = auditLogRepository.findAll(pageable);

        return buildPaginatedResponse(auditPage);
    }

    // ========== Private Helper Methods ==========

    private PaginatedAuditLogResponse buildPaginatedResponse(Page<AuditLog> auditPage) {
        List<AuditLogResponse> content = auditPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PaginatedAuditLogResponse.builder()
                .content(content)
                .pageNumber(auditPage.getNumber())
                .pageSize(auditPage.getSize())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .isFirst(auditPage.isFirst())
                .isLast(auditPage.isLast())
                .build();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .recordId(auditLog.getRecordId())
                .patientId(auditLog.getPatientId())
                .performedBy(auditLog.getPerformedBy())
                .role(auditLog.getRole())
                .actionType(auditLog.getActionType())
                .timestamp(auditLog.getTimestamp())
                .details(auditLog.getDetails())
                .build();
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

    private void authorizePatientAccess(Long patientId, Long currentUserId, String role) {
        if ("PATIENT".equals(role)) {
            if (!currentUserId.equals(patientId)) {
                throw new UnauthorizedAccessException("Patients can only access their own audit logs");
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
