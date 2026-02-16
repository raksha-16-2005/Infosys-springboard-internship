package com.example.demo.service;

import com.example.demo.dto.AdminAppointmentDTO;
import com.example.demo.dto.AdminDoctorDTO;
import com.example.demo.dto.AdminPatientDTO;
import com.example.demo.dto.AdminProfileDTO;
import com.example.demo.dto.AdminStatsDTO;
import com.example.demo.model.Appointment;
import com.example.demo.model.DoctorProfile;
import com.example.demo.model.PatientProfile;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.DoctorProfileRepository;
import com.example.demo.repository.PatientProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository,
                        DoctorProfileRepository doctorProfileRepository,
                        PatientProfileRepository patientProfileRepository,
                        AppointmentRepository appointmentRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminStatsDTO getStats() {
        AdminStatsDTO dto = new AdminStatsDTO();
        dto.setTotalDoctors(doctorProfileRepository.count());
        dto.setTotalPatients(patientProfileRepository.count());
        dto.setTotalAppointments(appointmentRepository.count());

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);
        dto.setTodaysAppointments(appointmentRepository.countByAppointmentDateBetween(start, end));

        dto.setApprovedAppointments(appointmentRepository.countByStatus(Appointment.Status.COMPLETED));
        dto.setPendingAppointments(appointmentRepository.countByStatus(Appointment.Status.SCHEDULED));
        return dto;
    }

    public List<AdminDoctorDTO> getDoctors() {
        return doctorProfileRepository.findAll().stream()
                .map(this::toAdminDoctor)
                .collect(Collectors.toList());
    }

    public AdminDoctorDTO getDoctor(Long doctorId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId).orElse(null);
        if (profile == null) return null;
        return toAdminDoctor(profile);
    }

    public AdminDoctorDTO updateDoctorStatus(Long doctorId, boolean active) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorId).orElse(null);
        if (profile == null || profile.getUser() == null) return null;
        User user = profile.getUser();
        user.setActive(active);
        userRepository.save(user);
        return toAdminDoctor(profile);
    }

    public List<AdminPatientDTO> getPatients() {
        return patientProfileRepository.findAll().stream()
                .map(this::toAdminPatient)
                .collect(Collectors.toList());
    }

    public AdminPatientDTO getPatient(Long patientId) {
        PatientProfile profile = patientProfileRepository.findById(patientId).orElse(null);
        if (profile == null) return null;
        return toAdminPatient(profile);
    }

    public List<AdminAppointmentDTO> getAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toAdminAppointment)
                .collect(Collectors.toList());
    }

    public AdminProfileDTO getProfile(User user) {
        AdminProfileDTO dto = new AdminProfileDTO();
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setProfileImagePath(user.getProfileImagePath());
        return dto;
    }

    public AdminProfileDTO updateProfile(User user,
                                         String displayName,
                                         String email,
                                         String password,
                                         MultipartFile profileImage) throws IOException {
        if (displayName != null && !displayName.trim().isEmpty()) {
            user.setFullName(displayName.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            Optional<User> existing = userRepository.findByEmail(email.trim());
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(email.trim());
        }

        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password.trim()));
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            String savedPath = storeProfileImage(profileImage, user.getId());
            user.setProfileImagePath(savedPath);
        }

        userRepository.save(user);
        return getProfile(user);
    }

    public Path resolveProfileImage(User user) {
        if (user.getProfileImagePath() == null || user.getProfileImagePath().isBlank()) {
            return null;
        }
        return Paths.get(user.getProfileImagePath());
    }

    private String storeProfileImage(MultipartFile file, Long userId) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }

        String fileName = "profile-" + UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "admins", String.valueOf(userId));
        Files.createDirectories(uploadDir);

        Path target = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString().replace("\\", "/");
    }

    private AdminDoctorDTO toAdminDoctor(DoctorProfile profile) {
        AdminDoctorDTO dto = new AdminDoctorDTO();
        dto.setId(profile.getId());
        dto.setFullName(profile.getFullName());
        dto.setSpecialization(profile.getSpecialization());
        dto.setQualification(profile.getQualification());
        dto.setExperienceYears(profile.getExperienceYears());
        if (profile.getUser() != null) {
            dto.setEmail(profile.getUser().getEmail());
            dto.setActive(profile.getUser().getActive() == null || profile.getUser().getActive());
        } else {
            dto.setActive(true);
        }
        return dto;
    }

    private AdminPatientDTO toAdminPatient(PatientProfile profile) {
        AdminPatientDTO dto = new AdminPatientDTO();
        dto.setId(profile.getId());
        dto.setFullName(profile.getFullName());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setBloodGroup(profile.getBloodGroup());
        dto.setPhone(profile.getPhone());
        dto.setAddress(profile.getAddress());
        dto.setEmergencyContact(profile.getEmergencyContact());
        dto.setMedicalHistory(profile.getMedicalHistory());
        if (profile.getUser() != null) {
            dto.setEmail(profile.getUser().getEmail());
        }
        return dto;
    }

    private AdminAppointmentDTO toAdminAppointment(Appointment appointment) {
        AdminAppointmentDTO dto = new AdminAppointmentDTO();
        dto.setId(appointment.getId());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setStatus(appointment.getStatus().toString());
        if (appointment.getDoctor() != null) {
            dto.setDoctorName(appointment.getDoctor().getFullName());
            dto.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
        }
        if (appointment.getPatient() != null) {
            dto.setPatientName(appointment.getPatient().getFullName());
            dto.setPatientPhone(appointment.getPatient().getPhone());
        }
        return dto;
    }
}
