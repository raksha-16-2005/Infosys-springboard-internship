package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import com.medvault.exception.UnauthorizedAccessException;
import com.medvault.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Get all notifications for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(Long userId, int page, int size) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be > 0");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDTO);
    }

    /**
     * Get all unread notifications for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnreadNotifications(Long userId, int page, int size) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be > 0");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDTO);
    }

    /**
     * Get unread notification count for a user
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Get total notification count for a user
     */
    @Transactional(readOnly = true)
    public long getTotalCount(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        return notificationRepository.countByUserId(userId);
    }

    /**
     * Get notifications by type with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByType(Long userId, Notification.NotificationType type, int page, int size) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be > 0");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        return notifications.map(this::convertToDTO);
    }

    /**
     * Get notifications from a specific date range with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be > 0");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate, pageable);
        return notifications.map(this::convertToDTO);
    }

    /**
     * Mark a single notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("Invalid notificationId");
        }
        notificationRepository.markAsRead(notificationId);
    }

    /**
     * Mark all notifications for a user as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * Mark all notifications of a specific type as read
     */
    @Transactional
    public void markTypeAsRead(Long userId, Notification.NotificationType type) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        notificationRepository.markTypeAsRead(userId, type);
    }

    /**
     * Create a new notification (with idempotency check to avoid duplicates)
     */
    @Transactional
    public NotificationDTO createNotification(Long userId, Notification.NotificationType type, String message) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }

        // Idempotency check: avoid creating duplicate unread notifications with the same type and message
        boolean exists = notificationRepository.existsByUserIdAndTypeAndMessageAndIsReadFalse(userId, type, message);
        if (exists) {
            // Return the existing notification instead of creating a duplicate
            List<Notification> existingNotifs = notificationRepository.findByUserIdAndTypeAndIsReadFalseOrderByCreatedAtDesc(userId, type);
            for (Notification n : existingNotifs) {
                if (message.equals(n.getMessage())) {
                    return convertToDTO(n);
                }
            }
        }

        // Create new notification if no duplicate exists
        Notification notification = new Notification(userId, type, message);
        Notification saved = notificationRepository.save(notification);
        return convertToDTO(saved);
    }

    /**
     * Delete a notification by ID
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("Invalid notificationId");
        }
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Delete old notifications for a user (cleanup, e.g., older than 90 days)
     */
    @Transactional
    public int deleteOldNotifications(Long userId, int daysOld) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        if (daysOld <= 0) {
            throw new IllegalArgumentException("Days old must be > 0");
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteOldNotifications(userId, cutoffDate);
    }

    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long notificationId) {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("Invalid notificationId");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        return convertToDTO(notification);
    }

    /**
     * Get all notifications for a user (non-paginated, for admin use)
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotificationsNonPaginated(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Check if a user has unread notifications
     */
    @Transactional(readOnly = true)
    public boolean hasUnreadNotifications(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        return getUnreadCount(userId) > 0;
    }

    /**
     * Verify that a notification belongs to the user
     */
    @Transactional(readOnly = true)
    public void verifyNotificationOwnership(Long notificationId, Long userId) {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("Invalid notificationId");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have permission to access this notification");
        }
    }

    // ========== Private Helper Methods ==========

    private NotificationDTO convertToDTO(Notification notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
