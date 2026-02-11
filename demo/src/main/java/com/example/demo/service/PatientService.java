package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
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
    private final EmailService emailService;

    public PatientService(PatientProfileRepository profileRepo,
                          AppointmentRepository apptRepo,
                          DoctorProfileRepository doctorProfileRepo,
                          PrescriptionRepository prescriptionRepo,
                          EmailService emailService) {
        this.profileRepo = profileRepo;
        this.apptRepo = apptRepo;
        this.doctorProfileRepo = doctorProfileRepo;
        this.prescriptionRepo = prescriptionRepo;
        this.emailService = emailService;
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

    public AppointmentDTO bookAppointment(User patient, Long doctorId, AppointmentRequest req) {
        PatientProfile pProfile = profileRepo.findByUser(patient).orElse(null);
        if (pProfile == null) return null;
        DoctorProfile dProfile = doctorProfileRepo.findById(doctorId).orElse(null);
        if (dProfile == null) return null;

        LocalDateTime slot = LocalDateTime.parse(req.getAppointmentDate());
        if (apptRepo.existsByDoctorAndAppointmentDate(dProfile, slot)) {
            return null;
        }

        Appointment a = new Appointment();
        a.setPatient(pProfile);
        a.setDoctor(dProfile);
        a.setAppointmentDate(slot);
        a.setNotes(req.getNotes());
        a.setSymptoms(req.getSymptoms());
        a.setStatus(Appointment.Status.SCHEDULED);
        a = apptRepo.save(a);

        if (patient.getEmail() != null) {
            emailService.sendAppointmentBookedEmail(
                    patient.getEmail(),
                    pProfile.getFullName(),
                    dProfile.getFullName(),
                    a.getAppointmentDate()
            );
        }

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
}
