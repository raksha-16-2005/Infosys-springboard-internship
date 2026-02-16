package com.example.demo.controller;

import com.example.demo.dto.DoctorProfileDTO;
import com.example.demo.repository.DoctorProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
public class DoctorsListController {

    private final DoctorProfileRepository doctorProfileRepository;

    public DoctorsListController(DoctorProfileRepository doctorProfileRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllDoctors() {
        List<DoctorProfileDTO> doctors = doctorProfileRepository.findAll().stream().map(d -> {
            DoctorProfileDTO dto = new DoctorProfileDTO();
            dto.setId(d.getId());
            dto.setUserId(d.getUser() != null ? d.getUser().getId() : null);
            dto.setFullName(d.getFullName());
            dto.setSpecialization(d.getSpecialization());
            dto.setQualification(d.getQualification());
            dto.setExperienceYears(d.getExperienceYears());
            dto.setHospitalName(d.getHospitalName());
            dto.setPhone(d.getPhone());
            dto.setBio(d.getBio());
            dto.setConsultationFee(d.getConsultationFee());
            dto.setAvailableSlots(d.getAvailableSlots());
            dto.setProfileImagePath(d.getProfileImagePath());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(doctors);
    }
}
