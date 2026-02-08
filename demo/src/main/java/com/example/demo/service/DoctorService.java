package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final UserRepository userRepo;
    private final DoctorProfileRepository doctorProfileRepo;
    private final AppointmentRepository apptRepo;
    private final PrescriptionRepository prescriptionRepo;

    public DoctorService(UserRepository userRepo, DoctorProfileRepository doctorProfileRepo, AppointmentRepository apptRepo, PrescriptionRepository prescriptionRepo) {
        this.userRepo = userRepo;
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

    public AppointmentDTO updateAppointmentStatus(Long appointmentId, String status) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        a.setStatus(Appointment.Status.valueOf(status));
        a = apptRepo.save(a);
        return convertToAppointmentDTO(a);
    }

    public PrescriptionResponseDTO addPrescription(Long appointmentId, PrescriptionDTO dto) {
        Appointment appt = apptRepo.findById(appointmentId).orElse(null);
        if (appt == null) return null;
        
        Prescription p = new Prescription();
        p.setAppointment(appt);
        p.setMedicines(dto.getMedicines());
        p.setDosageInstructions(dto.getDosageInstructions());
        p = prescriptionRepo.save(p);
        return convertToPrescriptionDTO(p);
    }

    private AppointmentDTO convertToAppointmentDTO(Appointment a) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(a.getId());
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setStatus(a.getStatus().toString());
        dto.setNotes(a.getNotes());
        
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

    private PrescriptionResponseDTO convertToPrescriptionDTO(Prescription p) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(p.getId());
        dto.setMedicines(p.getMedicines());
        dto.setDosageInstructions(p.getDosageInstructions());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setAppointment(convertToAppointmentDTO(p.getAppointment()));
        return dto;
    }
}
