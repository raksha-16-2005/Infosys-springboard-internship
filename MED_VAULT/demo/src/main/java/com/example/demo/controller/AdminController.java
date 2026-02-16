package com.example.demo.controller;

import com.example.demo.dto.AdminAppointmentDTO;
import com.example.demo.dto.AdminDoctorDTO;
import com.example.demo.dto.AdminPatientDTO;
import com.example.demo.dto.AdminProfileDTO;
import com.example.demo.dto.AdminStatsDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminService adminService;

    public AdminController(UserRepository userRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/stats")
    public AdminStatsDTO getStats() {
        return adminService.getStats();
    }

    @GetMapping("/doctors")
    public List<AdminDoctorDTO> getDoctors() {
        return adminService.getDoctors();
    }

    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<?> getDoctor(@PathVariable Long doctorId) {
        AdminDoctorDTO doctor = adminService.getDoctor(doctorId);
        if (doctor == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(doctor);
    }

    @PutMapping("/doctors/{doctorId}/status")
    public ResponseEntity<?> updateDoctorStatus(@PathVariable Long doctorId, @RequestBody Map<String, Boolean> body) {
        boolean active = body.getOrDefault("active", true);
        AdminDoctorDTO doctor = adminService.updateDoctorStatus(doctorId, active);
        if (doctor == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/patients")
    public List<AdminPatientDTO> getPatients() {
        return adminService.getPatients();
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<?> getPatient(@PathVariable Long patientId) {
        AdminPatientDTO patient = adminService.getPatient(patientId);
        if (patient == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/appointments")
    public List<AdminAppointmentDTO> getAppointments() {
        return adminService.getAppointments();
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        AdminProfileDTO profile = adminService.getProfile(user);
        return ResponseEntity.ok(profile);
    }

    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestPart(required = false) MultipartFile profileImage) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        try {
            AdminProfileDTO updated = adminService.updateProfile(user, displayName, email, password, profileImage);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("Failed to update profile");
        }
    }

    @GetMapping("/profile/image")
    public ResponseEntity<?> getProfileImage() throws MalformedURLException {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        Path path = adminService.resolveProfileImage(user);
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=profile")
                .contentType(contentType)
                .body(resource);
    }
}

