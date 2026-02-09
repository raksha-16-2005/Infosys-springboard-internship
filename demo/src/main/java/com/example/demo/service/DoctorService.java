package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorProfileRepository doctorProfileRepo;
    private final AppointmentRepository apptRepo;
    private final PrescriptionRepository prescriptionRepo;

    public DoctorService(DoctorProfileRepository doctorProfileRepo, AppointmentRepository apptRepo, PrescriptionRepository prescriptionRepo) {
        this.doctorProfileRepo = doctorProfileRepo;
        this.apptRepo = apptRepo;
        this.prescriptionRepo = prescriptionRepo;
    }

    public DoctorProfileDTO getProfile(User user) {
        Optional<DoctorProfile> d = doctorProfileRepo.findByUser(user);
        if (d.isEmpty()) return null;
        DoctorProfile profile = d.get();
        DoctorProfileDTO dto = new DoctorProfileDTO();
        dto.setId(profile.getId());
        dto.setFullName(profile.getFullName());
        dto.setSpecialization(profile.getSpecialization());
        dto.setQualification(profile.getQualification());
        dto.setExperienceYears(profile.getExperienceYears());
        dto.setHospitalName(profile.getHospitalName());
        dto.setPhone(profile.getPhone());
        dto.setBio(profile.getBio());
        return dto;
    }

    public DoctorProfileDTO updateProfile(User user, ProfileDTO dto) {
        DoctorProfile profile = doctorProfileRepo.findByUser(user).orElse(new DoctorProfile());
        profile.setUser(user);
        profile.setFullName(dto.getFullName());
        profile.setSpecialization(dto.getSpecialization());
        profile.setQualification(dto.getQualification());
        profile.setExperienceYears(dto.getExperienceYears());
        profile.setHospitalName(dto.getHospitalName());
        profile.setPhone(dto.getPhone());
        profile.setBio(dto.getBio());
        profile = doctorProfileRepo.save(profile);
        
        DoctorProfileDTO res = new DoctorProfileDTO();
        res.setId(profile.getId());
        res.setFullName(profile.getFullName());
        res.setSpecialization(profile.getSpecialization());
        res.setQualification(profile.getQualification());
        res.setExperienceYears(profile.getExperienceYears());
        res.setHospitalName(profile.getHospitalName());
        res.setPhone(profile.getPhone());
        res.setBio(profile.getBio());
        return res;
    }

    public List<AppointmentDTO> myAppointments(User doctor) {
        DoctorProfile dProfile = doctorProfileRepo.findByUser(doctor).orElse(null);
        if (dProfile == null) return List.of();
        return apptRepo.findByDoctor(dProfile).stream().map(this::convertToAppointmentDTO).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getTodayAppointments(User doctor) {
        DoctorProfile dProfile = doctorProfileRepo.findByUser(doctor).orElse(null);
        if (dProfile == null) return List.of();
        LocalDate today = LocalDate.now();
        return apptRepo.findByDoctorAndDate(dProfile, today).stream()
                .map(this::convertToAppointmentDTO).collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByStatus(User doctor, String status) {
        DoctorProfile dProfile = doctorProfileRepo.findByUser(doctor).orElse(null);
        if (dProfile == null) return List.of();
        try {
            Appointment.Status enumStatus = Appointment.Status.valueOf(status.toUpperCase());
            return apptRepo.findByDoctorAndStatus(dProfile, enumStatus).stream()
                    .map(this::convertToAppointmentDTO).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public AppointmentDTO getAppointmentDetail(Long appointmentId) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        return convertToAppointmentDetailDTO(a);
    }

    public AppointmentDTO updateAppointmentStatus(Long appointmentId, String status) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        try {
            a.setStatus(Appointment.Status.valueOf(status.toUpperCase()));
            a = apptRepo.save(a);
            return convertToAppointmentDTO(a);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public AppointmentDTO updateAppointmentConsultationNotes(Long appointmentId, String notes, String symptoms, String consultationNotes) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        if (notes != null) a.setNotes(notes);
        if (symptoms != null) a.setSymptoms(symptoms);
        if (consultationNotes != null) a.setConsultationNotes(consultationNotes);
        a = apptRepo.save(a);
        return convertToAppointmentDetailDTO(a);
    }

    public PrescriptionResponseDTO addPrescription(Long appointmentId, PrescriptionDTO dto) {
        Appointment appt = apptRepo.findById(appointmentId).orElse(null);
        if (appt == null) return null;
        
        Prescription p = prescriptionRepo.findByAppointment(appt).orElse(new Prescription());
        p.setAppointment(appt);
        p.setDiagnosis(dto.getDiagnosis());
        p.setMedicinesJson(dto.getMedicinesJson());
        p.setTestsRecommended(dto.getTestsRecommended());
        p.setFollowUpDate(dto.getFollowUpDate());
        p.setNotes(dto.getNotes());
        p = prescriptionRepo.save(p);
        appt.setPrescription(p);
        apptRepo.save(appt);
        return convertToPrescriptionDTO(p);
    }

    public PrescriptionResponseDTO getPrescription(Long appointmentId) {
        Appointment appt = apptRepo.findById(appointmentId).orElse(null);
        if (appt == null) return null;
        Prescription p = prescriptionRepo.findByAppointment(appt).orElse(null);
        if (p == null) return null;
        return convertToPrescriptionDTO(p);
    }

    private AppointmentDTO convertToAppointmentDTO(Appointment a) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(a.getId());
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setStatus(a.getStatus().toString());
        dto.setNotes(a.getNotes());
        dto.setCreatedAt(a.getCreatedAt());
        
        PatientProfileDTO pDto = new PatientProfileDTO();
        pDto.setId(a.getPatient().getId());
        pDto.setFullName(a.getPatient().getFullName());
        pDto.setPhone(a.getPatient().getPhone());
        pDto.setAge(a.getPatient().getAge());
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
        dto.setSymptoms(a.getSymptoms());
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
