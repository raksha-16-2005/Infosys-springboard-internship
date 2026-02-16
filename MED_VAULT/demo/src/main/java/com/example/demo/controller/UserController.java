package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the currently authenticated user's basic details, based on the JWT.
     * Requires a valid Bearer token in the Authorization header.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        body.put("email", user.getEmail());
        body.put("role", user.getRole());
        body.put("displayName", user.getFullName());
        body.put("profileImagePath", user.getProfileImagePath());

        return ResponseEntity.ok(body);
    }
}

