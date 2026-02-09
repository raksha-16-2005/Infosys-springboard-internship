package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
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
        AppointmentDTO appointment = doctorService.getAppointmentDetail(appointmentId);
        if (appointment == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateAppointmentStatus(@PathVariable Long appointmentId, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        AppointmentDTO appointment = doctorService.updateAppointmentStatus(appointmentId, status);
        if (appointment == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(appointment);
    }

    @PutMapping("/appointments/{appointmentId}/consultation")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateConsultationNotes(@PathVariable Long appointmentId, @RequestBody Map<String, String> body) {
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
        PrescriptionResponseDTO prescription = doctorService.addPrescription(appointmentId, dto);
        if (prescription == null) return ResponseEntity.badRequest().body("Appointment not found");
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/appointments/{appointmentId}/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId) {
        PrescriptionResponseDTO prescription = doctorService.getPrescription(appointmentId);
        if (prescription == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(prescription);
    }
}

