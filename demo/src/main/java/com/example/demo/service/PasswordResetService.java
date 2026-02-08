package com.example.demo.service;

import com.example.demo.model.PasswordResetToken;
import com.example.demo.model.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(PasswordResetTokenRepository tokenRepo, UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.tokenRepo = tokenRepo;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void createPasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(15));
        tokenRepo.save(prt);
        String link = "http://localhost:5173/reset-password?token=" + token;
        emailService.sendResetEmail(user.getEmail(), link);
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> opt = tokenRepo.findByToken(token);
        if (opt.isEmpty()) return false;
        PasswordResetToken prt = opt.get();
        if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepo.delete(prt);
            return false;
        }
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepo.delete(prt);
        return true;
    }
}
