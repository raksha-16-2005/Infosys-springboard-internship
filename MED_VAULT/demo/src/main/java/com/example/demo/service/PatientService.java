package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final PatientProfileRepository profileRepo;
    private final AppointmentRepository apptRepo;
    private final DoctorProfileRepository doctorProfileRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepo;

    public PatientService(PatientProfileRepository profileRepo,
                          AppointmentRepository apptRepo,
                          DoctorProfileRepository doctorProfileRepo,
                          PrescriptionRepository prescriptionRepo,
                          UserRepository userRepository,
                          MedicalRecordRepository medicalRecordRepo) {
        this.profileRepo = profileRepo;
        this.apptRepo = apptRepo;
        this.doctorProfileRepo = doctorProfileRepo;
        this.prescriptionRepo = prescriptionRepo;
        this.userRepository = userRepository;
        this.medicalRecordRepo = medicalRecordRepo;
    }

    public PatientProfileDTO getProfile(User user) {
        Optional<PatientProfile> p = profileRepo.findByUser(user);
        if (p.isEmpty()) return null;
        PatientProfile profile = p.get();
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setId(profile.getId());
        dto.setFullName(profile.getFullName());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setBloodGroup(profile.getBloodGroup());
        dto.setPhone(profile.getPhone());
        dto.setAddress(profile.getAddress());
        dto.setEmergencyContact(profile.getEmergencyContact());
        dto.setMedicalHistory(profile.getMedicalHistory());
        return dto;
    }

    public PatientProfileDTO updateProfile(User user, ProfileDTO dto) {
        PatientProfile profile = profileRepo.findByUser(user).orElse(new PatientProfile());
        profile.setUser(user);
        profile.setFullName(dto.getFullName());
        profile.setAge(dto.getAge());
        profile.setGender(dto.getGender());
        profile.setBloodGroup(dto.getBloodGroup());
        profile.setPhone(dto.getPhone());
        profile.setAddress(dto.getAddress());
        profile.setEmergencyContact(dto.getEmergencyContact());
        profile.setMedicalHistory(dto.getMedicalHistory());
        profile = profileRepo.save(profile);

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
            userRepository.save(user);
        }
        
        PatientProfileDTO res = new PatientProfileDTO();
        res.setId(profile.getId());
        res.setFullName(profile.getFullName());
        res.setAge(profile.getAge());
        res.setGender(profile.getGender());
        res.setBloodGroup(profile.getBloodGroup());
        res.setPhone(profile.getPhone());
        res.setAddress(profile.getAddress());
        res.setEmergencyContact(profile.getEmergencyContact());
        res.setMedicalHistory(profile.getMedicalHistory());
        return res;
    }

    public AppointmentDTO bookAppointment(User patient, Long doctorUserId, AppointmentRequest req) {
        System.out.println("=== BookAppointment called ===");
        System.out.println("Patient: " + (patient != null ? patient.getUsername() : "null"));
        System.out.println("Doctor User ID: " + doctorUserId);
        System.out.println("Appointment Date: " + req.getAppointmentDate());
        System.out.println("Symptoms: " + req.getSymptoms());
        System.out.println("Notes: " + req.getNotes());
        
        PatientProfile pProfile = profileRepo.findByUser(patient).orElse(null);
        if (pProfile == null) {
            System.out.println("ERROR: Patient profile not found for user: " + patient.getUsername());
            return null;
        }
        
        User doctorUser = userRepository.findById(doctorUserId).orElse(null);
        if (doctorUser == null) {
            System.out.println("ERROR: Doctor user not found with ID: " + doctorUserId);
            return null;
        }
        System.out.println("Found doctor user: " + doctorUser.getUsername());
        
        DoctorProfile dProfile = doctorProfileRepo.findByUser(doctorUser).orElse(null);
        if (dProfile == null) {
            System.out.println("ERROR: Doctor profile not found for user: " + doctorUser.getUsername());
            return null;
        }
        System.out.println("Found doctor profile: " + dProfile.getFullName());
        
        Appointment a = new Appointment();
        a.setPatient(pProfile);
        a.setDoctor(dProfile);
        try {
            a.setAppointmentDate(LocalDateTime.parse(req.getAppointmentDate()));
        } catch (Exception e) {
            System.out.println("ERROR: Failed to parse appointment date: " + req.getAppointmentDate());
            e.printStackTrace();
            return null;
        }
        a.setNotes(req.getNotes());
        a.setSymptoms(req.getSymptoms());
        a.setStatus(Appointment.Status.SCHEDULED);
        a = apptRepo.save(a);
        System.out.println("Appointment saved successfully with ID: " + a.getId());
        return convertToAppointmentDTO(a);
    }

    public List<AppointmentDTO> myAppointments(User patient) {
        PatientProfile pProfile = profileRepo.findByUser(patient).orElse(null);
        if (pProfile == null) return List.of();
        return apptRepo.findByPatient(pProfile).stream()
                .sorted((a, b) -> b.getAppointmentDate().compareTo(a.getAppointmentDate()))
                .map(this::convertToAppointmentDTO).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getUpcomingAppointments(User patient) {
        PatientProfile pProfile = profileRepo.findByUser(patient).orElse(null);
        if (pProfile == null) return List.of();
        return apptRepo.findUpcomingByPatient(pProfile, LocalDateTime.now()).stream()
                .map(this::convertToAppointmentDTO).collect(Collectors.toList());
    }

    public List<PrescriptionResponseDTO> myPrescriptions(User patient) {
        PatientProfile pProfile = profileRepo.findByUser(patient).orElse(null);
        if (pProfile == null) return List.of();
        List<Appointment> appts = apptRepo.findByPatient(pProfile);
        return prescriptionRepo.findByAppointmentIn(appts).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::convertToPrescriptionDTO).collect(Collectors.toList());
    }

    public PrescriptionResponseDTO getPrescription(Long appointmentId) {
        Appointment appt = apptRepo.findById(appointmentId).orElse(null);
        if (appt == null) return null;
        Prescription p = prescriptionRepo.findByAppointment(appt).orElse(null);
        if (p == null) return null;
        return convertToPrescriptionDTO(p);
    }

    public List<PrescriptionResponseDTO> getFollowUpPrescriptions(User patient) {
        PatientProfile pProfile = profileRepo.findByUser(patient).orElse(null);
        if (pProfile == null) return List.of();
        return prescriptionRepo.findFollowUpsByPatient(pProfile).stream()
                .map(this::convertToPrescriptionDTO).collect(Collectors.toList());
    }

    public AppointmentDTO getAppointmentDetail(Long appointmentId) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        return convertToAppointmentDetailDTO(a);
    }

    private AppointmentDTO convertToAppointmentDTO(Appointment a) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(a.getId());
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setStatus(a.getStatus().toString());
        dto.setNotes(a.getNotes());
        dto.setSymptoms(a.getSymptoms());
        dto.setDoctorRemarks(a.getDoctorRemarks());
        dto.setRescheduledDate(a.getRescheduledDate());
        dto.setCreatedAt(a.getCreatedAt());
        
        PatientProfileDTO pDto = new PatientProfileDTO();
        pDto.setId(a.getPatient().getId());
        pDto.setFullName(a.getPatient().getFullName());
        dto.setPatient(pDto);
        
        DoctorProfileDTO dDto = new DoctorProfileDTO();
        dDto.setId(a.getDoctor().getId());
        dDto.setFullName(a.getDoctor().getFullName());
        dDto.setSpecialization(a.getDoctor().getSpecialization());
        dto.setDoctor(dDto);
        
        return dto;
    }

    private AppointmentDTO convertToAppointmentDetailDTO(Appointment a) {
        AppointmentDTO dto = convertToAppointmentDTO(a);
        dto.setConsultationNotes(a.getConsultationNotes());
        return dto;
    }

    private PrescriptionResponseDTO convertToPrescriptionDTO(Prescription p) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(p.getId());
        dto.setDiagnosis(p.getDiagnosis());
        dto.setMedicinesJson(p.getMedicinesJson());
        dto.setTestsRecommended(p.getTestsRecommended());
        dto.setFollowUpDate(p.getFollowUpDate());
        dto.setNotes(p.getNotes());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setAppointment(convertToAppointmentDetailDTO(p.getAppointment()));
        return dto;
    }

    public boolean cancelAppointment(User patient, Long appointmentId) {
        Appointment appt = apptRepo.findById(appointmentId).orElse(null);
        if (appt == null) return false;
        PatientProfile pProfile = profileRepo.findByUser(patient).orElse(null);
        if (pProfile == null || !appt.getPatient().getId().equals(pProfile.getId())) {
            return false;
        }
        appt.setStatus(Appointment.Status.CANCELLED);
        apptRepo.save(appt);
        return true;
    }

    public List<MedicalRecordDTO> getMedicalRecords(User patient) {
        return medicalRecordRepo.findByPatient(patient).stream()
                .sorted((a, b) -> b.getUploadedAt().compareTo(a.getUploadedAt()))
                .map(record -> {
                    MedicalRecordDTO dto = new MedicalRecordDTO();
                    dto.setId(record.getId());
                    dto.setRecordDate(record.getUploadedAt());
                    dto.setRecordType(record.getFilename() != null ? record.getFilename() : "Medical Record");
                    dto.setNotes(record.getContent());
                    dto.setFilePath(record.getFilePath());
                    dto.setDoctorName(record.getDoctorName());
                    return dto;
                }).collect(Collectors.toList());
    }

    public String updateProfileImage(User patient, MultipartFile file) {
        if (file.isEmpty()) return null;
        
        try {
            String uploadDir = "uploads/patients/" + patient.getId();
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = "profile_" + System.currentTimeMillis() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String relativePath = uploadDir + "/" + fileName;
            patient.setProfileImagePath(relativePath);
            userRepository.save(patient);
            
            return relativePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getProfileImagePath(User patient) {
        return patient.getProfileImagePath();
    }
}
