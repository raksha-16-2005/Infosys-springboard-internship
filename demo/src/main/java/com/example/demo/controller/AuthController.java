package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.User;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtils;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    AuditLogService auditLogService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@RequestBody LoginRequest loginRequest) {

        String identifier = loginRequest.getUsername();
        if (identifier != null && identifier.contains("@")) {
            Optional<User> u = userRepository.findByEmail(identifier);
            if (u.isPresent()) identifier = u.get().getUsername();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Account is not active. Current status: " + user.getStatus());
        }

        String jwt = jwtUtils.generateJwtToken(authentication);

        String role = "";
        if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
            role = userDetails.getAuthorities().iterator().next().getAuthority();
            if (role.startsWith("ROLE_")) role = role.substring(5);
        }

        auditLogService.log(user.getUsername(), "LOGIN", "User logged in");

        JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getUsername(), role);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", jwtResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@RequestBody SignupRequest signUpRequest) {
        String result = userService.registerUser(signUpRequest);
        if (result.startsWith("Error")) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(result));
        }
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", result));
    }

    /**
     * Request an email OTP for passwordless authentication.
     */
    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<String>> requestOtp(@RequestBody OtpRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("User with this email does not exist"));
        }

        User user = userOpt.get();

        String otp = String.format("%06d", secureRandom.nextInt(900000) + 100000);
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok(ApiResponse.ok("OTP sent to registered email address", null));
    }

    /**
     * Verify a previously sent email OTP and issue a JWT token.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<JwtResponse>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("User with this email does not exist"));
        }

        User user = userOpt.get();

        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("No OTP requested for this user"));
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("OTP has expired"));
        }

        if (!user.getOtpCode().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("Invalid OTP"));
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("Account is not active. Current status: " + user.getStatus()));
        }

        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        String jwt = jwtUtils.generateTokenForUser(user.getUsername(), user.getRole().name());
        String role = user.getRole().name().replace("ROLE_", "");

        auditLogService.log(user.getUsername(), "LOGIN_OTP", "User logged in via OTP");

        JwtResponse jwtResponse = new JwtResponse(jwt, user.getUsername(), role);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified", jwtResponse));
    }
}

