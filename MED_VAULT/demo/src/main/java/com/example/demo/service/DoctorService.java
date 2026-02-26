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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final DoctorProfileRepository doctorProfileRepo;
    private final AppointmentRepository apptRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final UserRepository userRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentFeedbackRepository feedbackRepository;

    public DoctorService(DoctorProfileRepository doctorProfileRepo,
                         AppointmentRepository apptRepo,
                         PrescriptionRepository prescriptionRepo,
                         UserRepository userRepository,
                         MedicalRecordRepository medicalRecordRepository,
                         AppointmentFeedbackRepository feedbackRepository) {
        this.doctorProfileRepo = doctorProfileRepo;
        this.apptRepo = apptRepo;
        this.prescriptionRepo = prescriptionRepo;
        this.userRepository = userRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.feedbackRepository = feedbackRepository;
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
        dto.setConsultationFee(profile.getConsultationFee());
        dto.setAvailableSlots(profile.getAvailableSlots());
        dto.setProfileImagePath(profile.getProfileImagePath());
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
        profile.setConsultationFee(dto.getConsultationFee());
        profile.setAvailableSlots(dto.getAvailableSlots());
        profile = doctorProfileRepo.save(profile);

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
            userRepository.save(user);
        }
        
        DoctorProfileDTO res = new DoctorProfileDTO();
        res.setId(profile.getId());
        res.setFullName(profile.getFullName());
        res.setSpecialization(profile.getSpecialization());
        res.setQualification(profile.getQualification());
        res.setExperienceYears(profile.getExperienceYears());
        res.setHospitalName(profile.getHospitalName());
        res.setPhone(profile.getPhone());
        res.setBio(profile.getBio());
        res.setConsultationFee(profile.getConsultationFee());
        res.setAvailableSlots(profile.getAvailableSlots());
        res.setProfileImagePath(profile.getProfileImagePath());
        return res;
    }

    public DoctorProfileDTO updateProfileImage(User user, MultipartFile file) throws IOException {
        DoctorProfile profile = doctorProfileRepo.findByUser(user).orElse(new DoctorProfile());
        profile.setUser(user);
        String storedPath = storeProfileImage(file, user.getId());
        profile.setProfileImagePath(storedPath);
        doctorProfileRepo.save(profile);
        return getProfile(user);
    }

    public Path resolveProfileImage(User user) {
        DoctorProfile profile = doctorProfileRepo.findByUser(user).orElse(null);
        if (profile == null || profile.getProfileImagePath() == null || profile.getProfileImagePath().isBlank()) {
            return null;
        }
        return Paths.get(profile.getProfileImagePath());
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

    public AppointmentDTO acceptAppointment(Long appointmentId) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        a.setStatus(Appointment.Status.ACCEPTED);
        a = apptRepo.save(a);
        return convertToAppointmentDTO(a);
    }

    public AppointmentDTO rejectAppointment(Long appointmentId, String remarks) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        a.setStatus(Appointment.Status.REJECTED);
        a.setDoctorRemarks(remarks);
        a = apptRepo.save(a);
        return convertToAppointmentDTO(a);
    }

    public AppointmentDTO rescheduleAppointment(Long appointmentId, LocalDateTime newDate, String remarks) {
        Appointment a = apptRepo.findById(appointmentId).orElse(null);
        if (a == null) return null;
        a.setStatus(Appointment.Status.RESCHEDULED);
        a.setDoctorRemarks(remarks);
        if (newDate != null) {
            a.setAppointmentDate(newDate);
            a.setRescheduledDate(newDate);
        }
        a = apptRepo.save(a);
        return convertToAppointmentDTO(a);
    }

    public List<DoctorPatientDTO> getPatients(User doctor) {
        DoctorProfile dProfile = doctorProfileRepo.findByUser(doctor).orElse(null);
        if (dProfile == null) return List.of();
        return apptRepo.findByDoctor(dProfile).stream()
            .map(Appointment::getPatient)
            .filter(p -> p != null && p.getUser() != null)
            .collect(Collectors.toMap(
                p -> p.getUser().getId(),
                p -> p,
                (existing, replacement) -> existing
            ))
            .values()
            .stream()
            .map(this::toDoctorPatient)
            .collect(Collectors.toList());
    }

    public List<MedicalRecordDTO> getPatientRecords(Long patientUserId) {
        User patient = userRepository.findById(patientUserId).orElse(null);
        if (patient == null) return List.of();
        return medicalRecordRepository.findByPatient(patient).stream()
                .map(this::toMedicalRecord)
                .collect(Collectors.toList());
    }

    public MedicalRecordDTO addMedicalRecord(Long patientUserId, User doctor, String notes, MultipartFile file) throws IOException {
        User patient = userRepository.findById(patientUserId).orElse(null);
        if (patient == null) return null;
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctorName(doctor.getFullName() != null ? doctor.getFullName() : doctor.getUsername());
        if (notes != null && !notes.isBlank()) {
            record.setContent(notes);
        }
        if (file != null && !file.isEmpty()) {
            String storedPath = storeMedicalRecord(file, patient.getId());
            record.setFilePath(storedPath);
            record.setFilename(file.getOriginalFilename());
        }
        record = medicalRecordRepository.save(record);
        return toMedicalRecord(record);
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

    public List<FeedbackDTO> getMyFeedback(User doctor) {
        if (doctor == null) {
            return List.of();
        }
        return feedbackRepository.findByDoctorUserOrderByCreatedAtDesc(doctor)
                .stream()
                .map(this::toFeedbackDTO)
                .collect(Collectors.toList());
    }

    private AppointmentDTO convertToAppointmentDTO(Appointment a) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(a.getId());
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setStatus(a.getStatus().toString());
        dto.setNotes(a.getNotes());
        dto.setDoctorRemarks(a.getDoctorRemarks());
        dto.setRescheduledDate(a.getRescheduledDate());
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

    private DoctorPatientDTO toDoctorPatient(PatientProfile profile) {
        DoctorPatientDTO dto = new DoctorPatientDTO();
        dto.setProfileId(profile.getId());
        if (profile.getUser() != null) {
            dto.setUserId(profile.getUser().getId());
            dto.setEmail(profile.getUser().getEmail());
        }
        dto.setFullName(profile.getFullName());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setBloodGroup(profile.getBloodGroup());
        dto.setPhone(profile.getPhone());
        dto.setMedicalHistory(profile.getMedicalHistory());
        return dto;
    }

    private MedicalRecordDTO toMedicalRecord(MedicalRecord record) {
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setId(record.getId());
        dto.setFilename(record.getFilename());
        dto.setContent(record.getContent());
        dto.setFilePath(record.getFilePath());
        dto.setDoctorName(record.getDoctorName());
        dto.setUploadedAt(record.getUploadedAt());
        return dto;
    }

    private FeedbackDTO toFeedbackDTO(AppointmentFeedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setAppointmentId(feedback.getAppointment() != null ? feedback.getAppointment().getId() : null);
        dto.setRating(feedback.getRating());
        dto.setComments(feedback.getComments());
        dto.setCreatedAt(feedback.getCreatedAt());
        if (feedback.getPatientUser() != null) {
            dto.setPatientName(feedback.getPatientUser().getFullName() != null
                    ? feedback.getPatientUser().getFullName()
                    : feedback.getPatientUser().getUsername());
        }
        if (feedback.getDoctorUser() != null) {
            dto.setDoctorName(feedback.getDoctorUser().getFullName() != null
                    ? feedback.getDoctorUser().getFullName()
                    : feedback.getDoctorUser().getUsername());
        }
        return dto;
    }

    private String storeProfileImage(MultipartFile file, Long userId) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String fileName = "doctor-profile-" + UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "doctors", String.valueOf(userId));
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString().replace("\\", "/");
    }

    private String storeMedicalRecord(MultipartFile file, Long patientUserId) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String fileName = "record-" + UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "medical-records", String.valueOf(patientUserId));
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString().replace("\\", "/");
    }
}
