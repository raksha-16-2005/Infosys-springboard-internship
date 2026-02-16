package com.example.demo.service;

import com.example.demo.dto.SignupRequest;
import com.example.demo.model.User;
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

        // Send OTP email to the registered address
        emailService.sendOtpEmail(user.getEmail(), otp);

        return "User registered successfully! OTP sent to email.";
    }
}
