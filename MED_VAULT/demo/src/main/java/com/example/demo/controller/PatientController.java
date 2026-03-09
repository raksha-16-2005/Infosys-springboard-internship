package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DoctorConsentService;
import com.example.demo.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final UserRepository userRepository;
    private final PatientService patientService;
    private final DoctorConsentService doctorConsentService;

    public PatientController(UserRepository userRepository,
                             PatientService patientService,
                             DoctorConsentService doctorConsentService) {
        this.userRepository = userRepository;
        this.patientService = patientService;
        this.doctorConsentService = doctorConsentService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getProfile() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        PatientProfileDTO profile = patientService.getProfile(user);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileDTO dto) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        PatientProfileDTO profile = patientService.updateProfile(user, dto);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> bookAppointment(@RequestBody AppointmentRequest req) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        AppointmentDTO appointment = patientService.bookAppointment(user, req.getDoctorId(), req);
        if (appointment == null) return ResponseEntity.badRequest().body("Failed to book appointment");
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getAppointments() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(patientService.myAppointments(user));
    }

    @GetMapping("/appointments/upcoming")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getUpcomingAppointments() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(patientService.getUpcomingAppointments(user));
    }

    @GetMapping("/appointments/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getAppointmentDetail(@PathVariable Long appointmentId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        AppointmentDTO appointment = patientService.getAppointmentDetail(appointmentId);
        if (appointment == null) return ResponseEntity.notFound().build();
        
        // Verify ownership: Patient can only view their own appointments
        if (!patientService.isPatientAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only view your own appointments");
        }
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getPrescriptions() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(patientService.myPrescriptions(user));
    }

    @GetMapping("/prescriptions/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership: Patient can only view prescriptions for their own appointments
        if (!patientService.isPatientAppointment(user, appointmentId)) {
            return ResponseEntity.status(403).body("Access denied: You can only view your own prescriptions");
        }
        
        PrescriptionResponseDTO prescription = patientService.getPrescription(appointmentId);
        if (prescription == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/follow-ups")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getFollowUpPrescriptions() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(patientService.getFollowUpPrescriptions(user));
    }

    @PutMapping("/appointments/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        boolean success = patientService.cancelAppointment(user, appointmentId);
        if (success) {
            return ResponseEntity.ok("Appointment cancelled successfully");
        }
        return ResponseEntity.badRequest().body("Failed to cancel appointment");
    }

    @GetMapping("/medical-records")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMedicalRecords() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(patientService.getMedicalRecords(user));
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> submitFeedback(@RequestBody FeedbackRequest request) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        
        // Verify ownership: Patient can only submit feedback for their own appointments
        if (request.getAppointmentId() != null && !patientService.isPatientAppointment(user, request.getAppointmentId())) {
            return ResponseEntity.status(403).body("Access denied: You can only submit feedback for your own appointments");
        }
        
        FeedbackDTO feedback = patientService.submitFeedback(user, request);
        if (feedback == null) {
            return ResponseEntity.badRequest().body("Failed to submit feedback. Ensure appointment is completed and belongs to you.");
        }
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/feedback")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMyFeedback() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(patientService.getMyFeedback(user));
    }

    @PostMapping("/profile/image")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        String imagePath = patientService.updateProfileImage(user, file);
        if (imagePath != null) {
            return ResponseEntity.ok(imagePath);
        }
        return ResponseEntity.badRequest().body("Failed to upload image");
    }

    @GetMapping("/profile/image")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getProfileImage() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        String imagePath = patientService.getProfileImagePath(user);
        return ResponseEntity.ok(imagePath != null ? imagePath : "");
    }

    @PostMapping("/consents")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> grantDoctorConsent(@RequestBody DoctorConsentRequest request) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        if (request == null || request.getDoctorId() == null) {
            return ResponseEntity.badRequest().body("doctorId is required");
        }
        return ResponseEntity.ok(
                doctorConsentService.grantConsent(user, request.getDoctorId(), request.getReason())
        );
    }

    @PutMapping("/consents/{doctorId}/revoke")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> revokeDoctorConsent(@PathVariable Long doctorId,
                                                 @RequestBody(required = false) DoctorConsentRequest request) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(doctorConsentService.revokeConsent(user, doctorId, reason));
    }

    @GetMapping("/consents")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> listDoctorConsents() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(doctorConsentService.listConsents(user));
    }
}

