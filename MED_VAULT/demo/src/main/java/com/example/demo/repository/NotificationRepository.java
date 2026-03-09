package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find all notifications for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Find all notifications for a user with pagination, ordered by creation date (newest first)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find all unread notifications for a user
     */
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    
    /**
     * Find all unread notifications for a user with pagination
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find all unread notifications for a user (basic)
     */
    List<Notification> findByUserId(Long userId);
    
    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndIsReadFalse(Long userId);
    
    /**
     * Count total notifications for a user
     */
    long countByUserId(Long userId);
    
    /**
     * Find notifications by type for a user
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, Notification.NotificationType type);
    
    /**
     * Find notifications by type with pagination
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, Notification.NotificationType type, Pageable pageable);
    
    /**
     * Find unread notifications of a specific type for a user
     */
    List<Notification> findByUserIdAndTypeAndIsReadFalseOrderByCreatedAtDesc(Long userId, Notification.NotificationType type);
    
    /**
     * Find notifications created after a certain date
     */
    List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime createdAfter);
    
    /**
     * Find notifications created between two dates
     */
    List<Notification> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find notifications created between two dates with pagination
     */
    Page<Notification> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Mark a single notification as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId);
    
    /**
     * Mark all notifications for a user as read
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
    
    /**
     * Mark notifications by type as read (useful for appointment reminders, etc.)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.type = :type AND n.isRead = false")
    void markTypeAsRead(@Param("userId") Long userId, @Param("type") Notification.NotificationType type);
    
    /**
     * Check if a notification exists (for idempotency - to prevent duplicates)
     */
    boolean existsByUserIdAndTypeAndMessageAndIsReadFalse(Long userId, Notification.NotificationType type, String message);
    
    /**
     * Delete old notifications (older than a certain date) for cleanup
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("userId") Long userId, @Param("cutoffDate") LocalDateTime cutoffDate);
}
