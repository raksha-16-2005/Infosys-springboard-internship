package com.example.demo.controller;

import com.example.demo.model.MedicalRecord;
import com.example.demo.model.User;
import com.example.demo.repository.MedicalRecordRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordsBridgeController {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedicalRecord(
            @RequestParam(value = "patientId", required = false) Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "notes", required = false) String notes) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File cannot be empty"));
        }

        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role)) {
            patientId = currentUser.getId();
        } else if (patientId == null) {
            patientId = currentUser.getId();
        }

        final Long resolvedPatientId = patientId;
        User patient = userRepository.findById(resolvedPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + resolvedPatientId));

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), patient.getId())) {
            throw new AccessDeniedException("Patients can only upload their own records");
        }

        String relativePath = storeFile(file, patient.getId());

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setFilename(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "record")));
        record.setFilePath(relativePath);
        record.setContent(notes == null ? "" : notes);
        record.setDoctorName(category == null ? "OTHER" : category);

        medicalRecordRepository.save(record);

        return ResponseEntity.ok(toClientRecord(record));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getRecords(@PathVariable Long patientId) {
        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), patientId)) {
            throw new AccessDeniedException("Patients can only view their own records");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        List<MedicalRecord> records = medicalRecordRepository.findByPatient(patient);
        records.sort(Comparator.comparing(MedicalRecord::getUploadedAt).reversed());

        List<Map<String, Object>> response = records.stream().map(this::toClientRecord).toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/patient/{patientId}/category/{category}")
    public ResponseEntity<?> getRecordsByCategory(@PathVariable Long patientId, @PathVariable String category) {
        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), patientId)) {
            throw new AccessDeniedException("Patients can only view their own records");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        List<MedicalRecord> records = medicalRecordRepository.findByPatient(patient);
        records = records.stream()
                .filter(r -> category.equalsIgnoreCase(r.getDoctorName()))
                .sorted(Comparator.comparing(MedicalRecord::getUploadedAt).reversed())
                .toList();

        List<Map<String, Object>> response = records.stream().map(this::toClientRecord).toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<?> getRecordsByDateRange(
            @PathVariable Long patientId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), patientId)) {
            throw new AccessDeniedException("Patients can only view their own records");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);

        List<MedicalRecord> records = medicalRecordRepository.findByPatient(patient);
        records = records.stream()
                .filter(r -> r.getUploadedAt() != null && 
                           !r.getUploadedAt().isBefore(start) && 
                           !r.getUploadedAt().isAfter(end))
                .sorted(Comparator.comparing(MedicalRecord::getUploadedAt).reversed())
                .toList();

        List<Map<String, Object>> response = records.stream().map(this::toClientRecord).toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/patient/{patientId}/filter")
    public ResponseEntity<?> getRecordsWithFilter(
            @PathVariable Long patientId,
            @RequestParam String category,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), patientId)) {
            throw new AccessDeniedException("Patients can only view their own records");
        }

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);

        List<MedicalRecord> records = medicalRecordRepository.findByPatient(patient);
        records = records.stream()
                .filter(r -> category.equalsIgnoreCase(r.getDoctorName()) &&
                           r.getUploadedAt() != null && 
                           !r.getUploadedAt().isBefore(start) && 
                           !r.getUploadedAt().isAfter(end))
                .sorted(Comparator.comparing(MedicalRecord::getUploadedAt).reversed())
                .toList();

        List<Map<String, Object>> response = records.stream().map(this::toClientRecord).toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable Long recordId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes) {

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found: " + recordId));

        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), record.getPatient().getId())) {
            throw new AccessDeniedException("Patients can only update their own records");
        }

        if (file != null && !file.isEmpty()) {
            String relativePath = storeFile(file, record.getPatient().getId());
            record.setFilePath(relativePath);
            record.setFilename(StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "record")));
        }

        if (notes != null && !notes.isEmpty()) {
            record.setContent(notes);
        }

        record.setUploadedAt(LocalDateTime.now());
        medicalRecordRepository.save(record);

        return ResponseEntity.ok(toClientRecord(record));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteMedicalRecord(@PathVariable Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found: " + recordId));

        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), record.getPatient().getId())) {
            throw new AccessDeniedException("Patients can only delete their own records");
        }

        try {
            Path filePath = Paths.get(record.getFilePath()).toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Warning: Could not delete file: " + e.getMessage());
        }

        medicalRecordRepository.deleteById(recordId);

        return ResponseEntity.ok(Map.of("message", "Medical record deleted successfully"));
    }

    private Map<String, Object> toClientRecord(MedicalRecord record) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", record.getId());
        data.put("fileName", record.getFilename());
        data.put("category", record.getDoctorName() == null ? "OTHER" : record.getDoctorName());
        data.put("notes", record.getContent());
        data.put("fileUrl", record.getFilePath());
        data.put("uploadDate", record.getUploadedAt());
        return data;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }

    @GetMapping("/download/{recordId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> downloadFile(@PathVariable Long recordId, @RequestParam(required = false) boolean preview) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found: " + recordId));

        User currentUser = getCurrentUser();
        String role = normalizeRole(currentUser.getRole());

        if ("PATIENT".equals(role) && !Objects.equals(currentUser.getId(), record.getPatient().getId())) {
            throw new AccessDeniedException("Patients can only download their own records");
        }

        try {
            Path filePath = Paths.get(record.getFilePath()).toAbsolutePath();
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] fileContent = Files.readAllBytes(filePath);
            
            // Determine content type based on file extension
            String contentType = getContentType(record.getFilename());
            
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                    .header("Content-Type", contentType);
            
            // Only add attachment disposition if not previewing
            if (!preview) {
                responseBuilder.header("Content-Disposition", "attachment; filename=\"" + record.getFilename() + "\"");
            } else {
                responseBuilder.header("Content-Disposition", "inline; filename=\"" + record.getFilename() + "\"");
            }
            
            return responseBuilder.body(fileContent);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not download file"));
        }
    }
    
    private String getContentType(String filename) {
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (ext) {
            case "pdf": return "application/pdf";
            case "txt": return "text/plain";
            case "csv": return "text/csv";
            case "json": return "application/json";
            case "xml": return "application/xml";
            case "log": return "text/plain";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "webp": return "image/webp";
            default: return "application/octet-stream";
        }
    }

    private String storeFile(MultipartFile file, Long patientId) {
        try {
            Path baseDir = Paths.get("uploads", "medical-records", String.valueOf(patientId)).toAbsolutePath().normalize();
            Files.createDirectories(baseDir);

            String originalName = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "record"));
            String filename = System.currentTimeMillis() + "_" + originalName;
            Path target = baseDir.resolve(filename).normalize();

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return "uploads/medical-records/" + patientId + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }
}
