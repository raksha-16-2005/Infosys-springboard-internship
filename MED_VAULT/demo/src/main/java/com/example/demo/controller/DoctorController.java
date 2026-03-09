package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DoctorService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    private final DoctorService doctorService;
    private final UserRepository userRepository;

    public DoctorController(DoctorService doctorService, UserRepository userRepository) {
        this.doctorService = doctorService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getProfile() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        DoctorProfileDTO profile = doctorService.getProfile(user);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileDTO dto) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        logger.info("Doctor profile update requested by user: {} role: {}", user.getUsername(), user.getRole());
        DoctorProfileDTO profile = doctorService.updateProfile(user, dto);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getAppointments() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(doctorService.myAppointments(user));
    }

    @GetMapping("/appointments/today")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getTodayAppointments() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(doctorService.getTodayAppointments(user));
    }

    @GetMapping("/appointments/status/{status}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getAppointmentsByStatus(@PathVariable String status) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(doctorService.getAppointmentsByStatus(user, status));
    }

    @GetMapping("/appointments/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getAppointmentDetail(@PathVariable Long appointmentId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        AppointmentDTO appointment = doctorService.getAppointmentDetail(appointmentId);
        if (appointment == null) return ResponseEntity.notFound().build();
        
        // Verify ownership: Doctor can only view their own appointments
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only view your own appointments");
        }
        
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long appointmentId, @RequestBody Map<String, String> body) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership: Doctor can only update their own appointments
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only update your own appointments");
        }
        
        String status = body.get("status");
        AppointmentDTO appointment = doctorService.updateAppointmentStatus(appointmentId, status);
        if (appointment == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/accept")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> acceptAppointment(@PathVariable Long appointmentId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only accept your own appointments");
        }
        
        AppointmentDTO appointment = doctorService.acceptAppointment(appointmentId);
        if (appointment == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/reject")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> rejectAppointment(@PathVariable Long appointmentId, @RequestBody AppointmentDecisionRequest body) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only reject your own appointments");
        }
        
        AppointmentDTO appointment = doctorService.rejectAppointment(appointmentId, body.getRemarks());
        if (appointment == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/reschedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> rescheduleAppointment(@PathVariable Long appointmentId, @RequestBody AppointmentRescheduleRequest body) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only reschedule your own appointments");
        }
        
        LocalDateTime newDate = null;
        if (body.getAppointmentDate() != null && !body.getAppointmentDate().isBlank()) {
            newDate = LocalDateTime.parse(body.getAppointmentDate());
        }
        AppointmentDTO appointment = doctorService.rescheduleAppointment(appointmentId, newDate, body.getRemarks());
        if (appointment == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/consultation")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateConsultationNotes(@PathVariable Long appointmentId, @RequestBody Map<String, String> body) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only update consultation notes for your own appointments");
        }
        
        String notes = body.get("notes");
        String symptoms = body.get("symptoms");
        String consultationNotes = body.get("consultationNotes");
        AppointmentDTO appointment = doctorService.updateAppointmentConsultationNotes(appointmentId, notes, symptoms, consultationNotes);
        if (appointment == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(appointment);
    }

    @PostMapping("/appointments/{appointmentId}/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> addPrescription(@PathVariable Long appointmentId, @RequestBody PrescriptionDTO dto) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only add prescriptions for your own appointments");
        }
        
        PrescriptionResponseDTO prescription = doctorService.addPrescription(appointmentId, dto);
        if (prescription == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/appointments/{appointmentId}/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership
        if (!doctorService.isDoctorAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only view prescriptions for your own appointments");
        }
        
        PrescriptionResponseDTO prescription = doctorService.getPrescription(appointmentId);
        if (prescription == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/patients")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getPatients() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(doctorService.getPatients(user));
    }

    @GetMapping("/feedback")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getFeedback() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        List<FeedbackDTO> feedback = doctorService.getMyFeedback(user);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/patients/{patientUserId}/records")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getPatientRecords(@PathVariable Long patientUserId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(doctorService.getPatientRecords(user, patientUserId));
    }

    @PostMapping(value = "/patients/{patientUserId}/records", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> addPatientRecord(
            @PathVariable Long patientUserId,
            @RequestParam(required = false) String notes,
            @RequestPart(required = false) MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        try {
            MedicalRecordDTO record = doctorService.addMedicalRecord(patientUserId, user, notes, file);
            if (record == null) return ResponseEntity.badRequest().body("Patient not found");
            return ResponseEntity.ok(record);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Failed to save record");
        }
    }

    @PostMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> uploadProfileImage(@RequestPart MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        try {
            DoctorProfileDTO profile = doctorService.updateProfileImage(user, file);
            return ResponseEntity.ok(profile);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Failed to upload image");
        }
    }

    @GetMapping("/profile/image")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getProfileImage() throws MalformedURLException {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        Path path = doctorService.resolveProfileImage(user);
        if (path == null) return ResponseEntity.notFound().build();
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) return ResponseEntity.notFound().build();
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String detected = Files.probeContentType(path);
            if (detected != null) {
                contentType = MediaType.parseMediaType(detected);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=doctor-profile")
                .contentType(contentType)
                .body(resource);
    }
}

