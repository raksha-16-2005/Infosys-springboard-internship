package com.example.demo.service;

import com.example.demo.dto.SignupRequest;
import com.example.demo.model.DoctorProfile;
import com.example.demo.model.PatientProfile;
import com.example.demo.model.User;
import com.example.demo.repository.DoctorProfileRepository;
import com.example.demo.repository.PatientProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;

    @Autowired
    PatientProfileRepository patientProfileRepository;

    @Autowired
    DoctorProfileRepository doctorProfileRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public String registerUser(SignupRequest signupRequest) {
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            return "Error: Username is already taken!";
        }

        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            return "Error: Email is already in use!";
        }

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()),
                signupRequest.getRole()
        );

        // Generate OTP as part of registration
        String otp = String.format("%06d", secureRandom.nextInt(900000) + 100000);
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        createDefaultProfileForRole(user);

        // Send OTP email to the registered address
        emailService.sendOtpEmail(user.getEmail(), otp);

        return "User registered successfully! OTP sent to email.";
    }

    private void createDefaultProfileForRole(User user) {
        if (user == null || user.getRole() == null) {
            return;
        }

        String normalizedRole = user.getRole().toUpperCase();

        if (normalizedRole.endsWith("PATIENT")) {
            if (patientProfileRepository.findByUser(user).isEmpty()) {
                PatientProfile profile = new PatientProfile();
                profile.setUser(user);
                profile.setFullName(user.getFullName() != null ? user.getFullName() : user.getName());
                profile.setPhone(user.getPhone());
                profile.setAddress(user.getAddress());
                patientProfileRepository.save(profile);
            }
            return;
        }

        if (normalizedRole.endsWith("DOCTOR")) {
            if (doctorProfileRepository.findByUser(user).isEmpty()) {
                DoctorProfile profile = new DoctorProfile();
                profile.setUser(user);
                profile.setFullName(user.getFullName() != null ? user.getFullName() : user.getName());
                profile.setPhone(user.getPhone());
                profile.setSpecialization(user.getSpecialization());
                doctorProfileRepository.save(profile);
            }
        }
    }
}
