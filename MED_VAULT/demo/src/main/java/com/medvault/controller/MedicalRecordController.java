package com.medvault.controller;

import com.medvault.dto.ApiResponse;
import com.medvault.dto.MedicalRecordResponse;
import com.medvault.entity.RecordCategory;
import com.medvault.exception.UnauthorizedAccessException;
import com.medvault.exception.ValidationException;
import com.medvault.service.MedicalRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Autowired
    private UserRepository userRepository;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    /**
     * Upload a new medical record
     * POST /api/medical-records/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedicalRecord(
            @RequestParam(required = false) Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam RecordCategory category,
            @RequestParam(required = false) String notes) {
        
        try {
            String currentRole = getCurrentUserRole();
            Long currentUserId = getCurrentUserId();

            // For patient dashboard uploads, always bind to authenticated patient ID.
            if ("PATIENT".equals(currentRole)) {
                patientId = currentUserId;
            } else if (patientId == null) {
                patientId = currentUserId;
            }
            
            MedicalRecordResponse response = medicalRecordService.uploadRecord(patientId, file, category, notes);
            return ResponseEntity.ok(ApiResponse.success(response, "Medical record uploaded successfully"));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error(e.getMessage())
            );
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error uploading medical record: " + e.getMessage())
            );
        }
    }

    /**
     * Get all active records for a patient (sorted by date descending)
     * GET /api/medical-records/patient/{patientId}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getPatientRecords(
            @PathVariable Long patientId) {
        
        List<MedicalRecordResponse> records = medicalRecordService.getRecordsForPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success(records, "Records retrieved successfully"));
    }

    /**
     * Get records by category for a patient (sorted by date descending)
     * GET /api/medical-records/patient/{patientId}/category/{category}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}/category/{category}")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getRecordsByCategory(
            @PathVariable Long patientId,
            @PathVariable RecordCategory category) {
        
        List<MedicalRecordResponse> records = medicalRecordService.getRecordsByCategory(patientId, category);
        return ResponseEntity.ok(ApiResponse.success(records, "Records retrieved successfully"));
    }

    /**
     * Get records within a date range for a patient (sorted by date descending)
     * GET /api/medical-records/patient/{patientId}/date-range?startDate=...&endDate=...
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> getRecordsByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<MedicalRecordResponse> records = medicalRecordService.getRecordsByDateRange(patientId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(records, "Records retrieved successfully"));
    }

    /**
     * Get records by category and date range for a patient (sorted by date descending)
     * GET /api/medical-records/patient/{patientId}/filter?category=...&startDate=...&endDate=...
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/patient/{patientId}/filter")
    public ResponseEntity<ApiResponse<List<MedicalRecordResponse>>> filterRecords(
            @PathVariable Long patientId,
            @RequestParam RecordCategory category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<MedicalRecordResponse> records = medicalRecordService
                .getRecordsByCategoryAndDateRange(patientId, category, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(records, "Records retrieved successfully"));
    }

    /**
     * Get a single medical record by ID
     * GET /api/medical-records/{recordId}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> getRecordDetail(
            @PathVariable Long recordId) {
        
        MedicalRecordResponse record = medicalRecordService.getRecordById(recordId);
        return ResponseEntity.ok(ApiResponse.success(record, "Record retrieved successfully"));
    }

    /**
     * Update an existing medical record (creates new version)
     * PUT /api/medical-records/{recordId}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @PutMapping("/{recordId}")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> updateMedicalRecord(
            @PathVariable Long recordId,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String notes) {
        
        MedicalRecordResponse response = medicalRecordService.updateRecord(recordId, file, notes);
        return ResponseEntity.ok(ApiResponse.success(response, "Medical record updated successfully"));
    }

    /**
     * Soft delete a medical record (admin only)
     * DELETE /api/medical-records/{recordId}
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable Long recordId) {
        medicalRecordService.deleteRecord(recordId);
        return ResponseEntity.ok(ApiResponse.success(null, "Record deleted successfully"));
    }

    // ========== Private Helper Methods ==========

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        return user.get().getId();
    }

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null || authentication.getAuthorities().isEmpty()) {
            throw new RuntimeException("User role not found");
        }

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        if (authority != null && authority.startsWith("ROLE_")) {
            return authority.substring(5);
        }
        return authority;
    }
}

