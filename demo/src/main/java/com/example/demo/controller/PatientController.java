package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final UserRepository userRepository;
    private final PatientService patientService;

    public PatientController(UserRepository userRepository, PatientService patientService) {
        this.userRepository = userRepository;
        this.patientService = patientService;
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
        AppointmentDTO appointment = patientService.getAppointmentDetail(appointmentId);
        if (appointment == null) return ResponseEntity.notFound().build();
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
}

