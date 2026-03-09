package com.example.demo.controller;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all notifications for the current user with pagination
     * GET /api/notifications?page=0&size=10
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService.getNotifications(userId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications.getContent());
        response.put("currentPage", notifications.getNumber());
        response.put("totalItems", notifications.getTotalElements());
        response.put("totalPages", notifications.getTotalPages());
        response.put("isLast", notifications.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notifications for the current user with pagination
     * GET /api/notifications/unread?page=0&size=10
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications.getContent());
        response.put("currentPage", notifications.getNumber());
        response.put("totalItems", notifications.getTotalElements());
        response.put("totalPages", notifications.getTotalPages());
        response.put("isLast", notifications.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notification count
     * GET /api/notifications/unread-count
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Get total notification count
     * GET /api/notifications/count
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getTotalCount() {
        Long userId = getCurrentUserId();
        long count = notificationService.getTotalCount(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Get notifications by type
     * GET /api/notifications/by-type?type=APPOINTMENT_REMINDER&page=0&size=10
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/by-type")
    public ResponseEntity<Map<String, Object>> getNotificationsByType(
            @RequestParam Notification.NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService.getNotificationsByType(userId, type, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications.getContent());
        response.put("type", type);
        response.put("currentPage", notifications.getNumber());
        response.put("totalItems", notifications.getTotalElements());
        response.put("totalPages", notifications.getTotalPages());
        response.put("isLast", notifications.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Get notifications by date range
     * GET /api/notifications/by-date-range?startDate=2026-01-01T00:00:00&endDate=2026-03-06T23:59:59&page=0&size=10
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/by-date-range")
    public ResponseEntity<Map<String, Object>> getNotificationsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = getCurrentUserId();
        Page<NotificationDTO> notifications = notificationService.getNotificationsByDateRange(userId, startDate, endDate, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications.getContent());
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("currentPage", notifications.getNumber());
        response.put("totalItems", notifications.getTotalElements());
        response.put("totalPages", notifications.getTotalPages());
        response.put("isLast", notifications.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * Mark a single notification as read
     * PUT /api/notifications/{notificationId}/mark-read
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        
        // Verify ownership: User can only mark their own notifications as read
        notificationService.verifyNotificationOwnership(notificationId, userId);
        
        notificationService.markAsRead(notificationId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification marked as read successfully");
        response.put("notificationId", notificationId.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications as read
     * PUT /api/notifications/mark-all-read
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications of a specific type as read
     * PUT /api/notifications/mark-type-read?type=APPOINTMENT_REMINDER
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @PutMapping("/mark-type-read")
    public ResponseEntity<Map<String, String>> markTypeAsRead(
            @RequestParam Notification.NotificationType type) {

        Long userId = getCurrentUserId();
        notificationService.markTypeAsRead(userId, type);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notifications of type " + type + " marked as read successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single notification by ID
     * GET /api/notifications/{notificationId}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> getNotificationById(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        
        // Verify ownership: User can only view their own notifications
        notificationService.verifyNotificationOwnership(notificationId, userId);
        
        NotificationDTO notification = notificationService.getNotificationById(notificationId);

        Map<String, Object> response = new HashMap<>();
        response.put("notification", notification);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a notification by ID
     * DELETE /api/notifications/{notificationId}
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long notificationId) {
        Long userId = getCurrentUserId();
        
        // Verify ownership: User can only delete their own notifications
        notificationService.verifyNotificationOwnership(notificationId, userId);
        
        notificationService.deleteNotification(notificationId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification deleted successfully");
        response.put("notificationId", notificationId.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete old notifications for cleanup (Admin only)
     * DELETE /api/notifications/cleanup?daysOld=90
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> deleteOldNotifications(
            @RequestParam(defaultValue = "90") int daysOld) {

        Long userId = getCurrentUserId();
        int deletedCount = notificationService.deleteOldNotifications(userId, daysOld);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deleted " + deletedCount + " notifications older than " + daysOld + " days");
        response.put("deletedCount", deletedCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has unread notifications
     * GET /api/notifications/has-unread
     */
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @GetMapping("/has-unread")
    public ResponseEntity<Map<String, Object>> hasUnreadNotifications() {
        Long userId = getCurrentUserId();
        boolean hasUnread = notificationService.hasUnreadNotifications(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasUnread", hasUnread);

        return ResponseEntity.ok(response);
    }

    // ========== Private Helper Methods ==========

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found: " + username);
        }
        return user.get().getId();
    }
}
