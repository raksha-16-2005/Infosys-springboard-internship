package com.medvault.controller;

import com.medvault.dto.ApiResponse;
import com.medvault.dto.AuditLogResponse;
import com.medvault.dto.PaginatedAuditLogResponse;
import com.medvault.entity.ActionType;
import com.medvault.service.AuditLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Get audit logs for a patient with pagination and sorting
     * GET /api/audit-logs/patient/{patientId}?page=0&size=10&sortBy=timestamp&sortDirection=desc
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<PaginatedAuditLogResponse>> getPatientAuditLogs(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PaginatedAuditLogResponse response = auditLogService.getPatientAuditLogs(
                patientId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient audit logs retrieved successfully"));
    }

    /**
     * Get audit logs for a specific medical record with pagination and sorting
     * GET /api/audit-logs/record/{recordId}?page=0&size=10&sortBy=timestamp&sortDirection=desc
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/record/{recordId}")
    public ResponseEntity<ApiResponse<PaginatedAuditLogResponse>> getRecordAuditLogs(
            @PathVariable Long recordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PaginatedAuditLogResponse response = auditLogService.getRecordAuditLogs(
                recordId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Record audit logs retrieved successfully"));
    }

    /**
     * Get audit logs for a patient filtered by action type with pagination
     * GET /api/audit-logs/patient/{patientId}/action/{actionType}?page=0&size=10&sortBy=timestamp&sortDirection=desc
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}/action/{actionType}")
    public ResponseEntity<ApiResponse<PaginatedAuditLogResponse>> getPatientAuditLogsByActionType(
            @PathVariable Long patientId,
            @PathVariable ActionType actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PaginatedAuditLogResponse response = auditLogService.getPatientAuditLogsByActionType(
                patientId, actionType, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient audit logs filtered by action type retrieved successfully"));
    }

    /**
     * Get audit logs for a patient within a time range with pagination
     * GET /api/audit-logs/patient/{patientId}/date-range?startTime=...&endTime=...&page=0&size=10
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<ApiResponse<PaginatedAuditLogResponse>> getPatientAuditLogsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PaginatedAuditLogResponse response = auditLogService.getPatientAuditLogsByDateRange(
                patientId, startTime, endTime, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient audit logs within date range retrieved successfully"));
    }

    /**
     * Get audit logs with advanced filtering
     * GET /api/audit-logs/filter?patientId=1&recordId=1&actionType=VIEW&startTime=...&endTime=...&page=0&size=10
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PaginatedAuditLogResponse>> getAuditLogs(
            @RequestParam Long patientId,
            @RequestParam(required = false) Long recordId,
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PaginatedAuditLogResponse response = auditLogService.getAuditLogs(
                patientId, recordId, actionType, startTime, endTime, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "Filtered audit logs retrieved successfully"));
    }

    /**
     * Get all audit logs for a patient (non-paginated, admin only)
     * GET /api/audit-logs/patient/{patientId}/all
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/patient/{patientId}/all")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAllPatientAuditLogs(
            @PathVariable Long patientId) {

        List<AuditLogResponse> response = auditLogService.getAllPatientAuditLogs(patientId);
        return ResponseEntity.ok(ApiResponse.success(response, "All patient audit logs retrieved successfully"));
    }

    /**
     * Get all audit logs across the entire system (admin only)
     * GET /api/audit-logs/system?page=0&size=10&sortBy=timestamp&sortDirection=desc
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<PaginatedAuditLogResponse>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        PaginatedAuditLogResponse response = auditLogService.getAllAuditLogs(
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(ApiResponse.success(response, "System audit logs retrieved successfully"));
    }
}
