package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.SystemStatsDTO;
import com.example.demo.dto.UserSummaryDTO;
import com.example.demo.service.AdminService;
import com.example.demo.service.AuditLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService, AuditLogService auditLogService) {
        this.adminService = adminService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserSummaryDTO>> getAllUsers() {
        return ApiResponse.ok("Users fetched", adminService.getAllUsers());
    }

    @GetMapping("/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserSummaryDTO>> getAllDoctors() {
        return ApiResponse.ok("Doctors fetched", adminService.getAllDoctors());
    }

    @PostMapping("/doctors/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserSummaryDTO> approveDoctor(@PathVariable Long id, Principal principal) {
        UserSummaryDTO dto = adminService.approveDoctor(id);
        auditLogService.log(principal.getName(), "APPROVE_DOCTOR", "Approved doctor with id " + id);
        return ApiResponse.ok("Doctor approved", dto);
    }

    @PostMapping("/users/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserSummaryDTO> suspendUser(@PathVariable Long id, Principal principal) {
        UserSummaryDTO dto = adminService.suspendUser(id);
        auditLogService.log(principal.getName(), "SUSPEND_USER", "Suspended user with id " + id);
        return ApiResponse.ok("User suspended", dto);
    }

    @PostMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserSummaryDTO> activateUser(@PathVariable Long id, Principal principal) {
        UserSummaryDTO dto = adminService.activateUser(id);
        auditLogService.log(principal.getName(), "ACTIVATE_USER", "Activated user with id " + id);
        return ApiResponse.ok("User activated", dto);
    }

    @DeleteMapping("/doctors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteDoctor(@PathVariable Long id, Principal principal) {
        adminService.deleteDoctor(id);
        auditLogService.log(principal.getName(), "DELETE_DOCTOR", "Deleted doctor with id " + id);
        return ApiResponse.ok("Doctor deleted", null);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<SystemStatsDTO> getStats() {
        return ApiResponse.ok("Stats fetched", adminService.getSystemStats());
    }
}


