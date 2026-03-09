package com.medvault.util;

import com.medvault.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for security-related operations
 */
@Component
public class SecurityUtils {

    /**
     * Get the currently authenticated user's ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }

        String username = authentication.getName();
        return convertToLong(username);
    }

    /**
     * Get the currently authenticated user's role
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }

        String role = extractUserRole(authentication);
        return normalizeRole(role);
    }

    /**
     * Check if the current user is an admin
     */
    public boolean isAdmin() {
        try {
            return "ADMIN".equals(getCurrentUserRole());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the current user is a patient
     */
    public boolean isPatient() {
        try {
            return "PATIENT".equals(getCurrentUserRole());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the current user is a doctor
     */
    public boolean isDoctor() {
        try {
            return "DOCTOR".equals(getCurrentUserRole());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verify that the current user owns the resource (by ID)
     */
    public void verifyOwnership(Long resourceOwnerId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(resourceOwnerId)) {
            throw new UnauthorizedAccessException("You do not have permission to access this resource");
        }
    }

    /**
     * Verify that the current user owns the resource or is an admin
     */
    public void verifyOwnershipOrAdmin(Long resourceOwnerId) {
        if (isAdmin()) {
            return; // Admins can access any resource
        }
        verifyOwnership(resourceOwnerId);
    }

    /**
     * Verify that the current user is the patient who owns the data
     */
    public void verifyPatientOwnership(Long patientId) {
        if (isAdmin()) {
            return; // Admins can access any patient data
        }
        
        if (!isPatient()) {
            throw new UnauthorizedAccessException("Only patients can access patient data");
        }
        
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(patientId)) {
            throw new UnauthorizedAccessException("Patients can only access their own data");
        }
    }

    /**
     * Get authentication context with userId and role
     */
    public AuthContext getAuthContext() {
        return new AuthContext(getCurrentUserId(), getCurrentUserRole());
    }

    // Private helper methods

    private Long convertToLong(String value) {
        if (value == null || value.isBlank()) {
            throw new UnauthorizedAccessException("Unable to extract user ID from authentication");
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new UnauthorizedAccessException("Unable to convert user identifier to Long: " + value);
        }
    }

    private String extractUserRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return null;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    private String normalizeRole(String authority) {
        if (authority == null || authority.isBlank()) {
            throw new UnauthorizedAccessException("User role not found");
        }
        if (authority.startsWith("ROLE_")) {
            return authority.substring(5);
        }
        return authority;
    }

    /**
     * Record class for authentication context
     */
    public record AuthContext(Long userId, String role) {}
}
