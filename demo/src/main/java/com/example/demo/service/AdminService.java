package com.example.demo.service;

import com.example.demo.dto.SystemStatsDTO;
import com.example.demo.dto.UserSummaryDTO;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    public AdminService(UserRepository userRepository,
                        AppointmentRepository appointmentRepository,
                        EmailService emailService) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
    }

    public List<UserSummaryDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserSummaryDTO::from)
                .collect(Collectors.toList());
    }

    public List<UserSummaryDTO> getAllDoctors() {
        return userRepository.findByRole(Role.ROLE_DOCTOR).stream()
                .map(UserSummaryDTO::from)
                .collect(Collectors.toList());
    }

    public UserSummaryDTO approveDoctor(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        if (saved.getEmail() != null) {
            emailService.sendDoctorApprovalEmail(saved.getEmail(), saved.getName());
        }
        return UserSummaryDTO.from(saved);
    }

    public UserSummaryDTO suspendUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setStatus(UserStatus.SUSPENDED);
        return UserSummaryDTO.from(userRepository.save(user));
    }

    public UserSummaryDTO activateUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setStatus(UserStatus.ACTIVE);
        return UserSummaryDTO.from(userRepository.save(user));
    }

    public void deleteDoctor(Long userId) {
        userRepository.deleteById(userId);
    }

    public SystemStatsDTO getSystemStats() {
        long totalUsers = userRepository.count();
        long totalDoctors = userRepository.countByRole(Role.ROLE_DOCTOR);
        long totalPatients = userRepository.countByRole(Role.ROLE_PATIENT);
        long totalAppointments = appointmentRepository.count();

        return new SystemStatsDTO(totalUsers, totalDoctors, totalPatients, totalAppointments);
    }
}

