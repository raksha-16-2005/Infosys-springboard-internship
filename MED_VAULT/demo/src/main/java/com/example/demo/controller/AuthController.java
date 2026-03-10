package com.example.demo.controller;

import com.example.demo.dto.JwtResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.OtpRequest;
import com.example.demo.dto.OtpVerifyRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtils;
import com.example.demo.service.EmailService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailService emailService;

    @Autowired
    NotificationService notificationService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        String identifier = loginRequest.getUsername();
        // allow users to login with email as well as username
        if (identifier != null && identifier.contains("@")) {
            java.util.Optional<com.example.demo.model.User> u = userRepository.findByEmail(identifier);
            if (u.isPresent()) identifier = u.get().getUsername();
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Invalid username/email or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = "";
        if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
            role = userDetails.getAuthorities().iterator().next().getAuthority();
            if (role.startsWith("ROLE_")) role = role.substring(5);
        }

        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        notifyAdminsOnLogin(user);
        Long userId = user != null ? user.getId() : null;
        String displayName = user != null ? user.getFullName() : null;
        String email = user != null ? user.getEmail() : null;
        String profileImagePath = user != null ? user.getProfileImagePath() : null;

        return ResponseEntity.ok(new JwtResponse(userId,
                                                 jwt,
                                                 userDetails.getUsername(),
                                                 role,
                                                 displayName,
                                                 email,
                                                 profileImagePath));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        String result = userService.registerUser(signUpRequest);
        if (result.startsWith("Error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Request an email OTP for passwordless authentication.
     */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody OtpRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: User with this email does not exist");
        }

        User user = userOpt.get();

        String otp = String.format("%06d", secureRandom.nextInt(900000) + 100000);
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok("OTP sent to registered email address");
    }

    /**
     * Verify a previously sent email OTP and issue a JWT token.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: User with this email does not exist");
        }

        User user = userOpt.get();

        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            return ResponseEntity.badRequest().body("Error: No OTP requested for this user");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Error: OTP has expired");
        }

        if (!user.getOtpCode().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body("Error: Invalid OTP");
        }

        // Clear OTP after successful verification
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        notifyAdminsOnLogin(user);

        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
        String role = user.getRole();

        return ResponseEntity.ok(new JwtResponse(
            user.getId(),
            jwt,
            user.getUsername(),
            role,
            user.getFullName(),
            user.getEmail(),
            user.getProfileImagePath()
        ));
    }

    private void notifyAdminsOnLogin(User loggedInUser) {
        if (loggedInUser == null || loggedInUser.getRole() == null) {
            return;
        }

        String role = loggedInUser.getRole().toUpperCase();
        if (role.endsWith("ADMIN")) {
            return;
        }

        String actorName = loggedInUser.getFullName() != null && !loggedInUser.getFullName().isBlank()
                ? loggedInUser.getFullName()
                : loggedInUser.getUsername();
        String message = role + " login: " + actorName + " (" + loggedInUser.getEmail() + ")";

        for (User user : userRepository.findAll()) {
            String userRole = user.getRole() != null ? user.getRole().toUpperCase() : "";
            if (userRole.endsWith("ADMIN")) {
                try {
                    notificationService.createNotification(
                            user.getId(),
                            com.example.demo.model.Notification.NotificationType.SYSTEM_NOTIFICATION,
                            message
                    );
                } catch (Exception ignored) {
                }
            }
        }
    }
}
