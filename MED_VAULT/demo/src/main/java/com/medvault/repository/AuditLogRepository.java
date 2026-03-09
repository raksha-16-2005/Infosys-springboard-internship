package com.medvault.repository;

import com.medvault.entity.ActionType;
import com.medvault.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find all audit logs for a patient with pagination
     */
    Page<AuditLog> findByPatientIdOrderByTimestampDesc(Long patientId, Pageable pageable);

    /**
     * Find all audit logs for a specific medical record with pagination
     */
    Page<AuditLog> findByRecordIdOrderByTimestampDesc(Long recordId, Pageable pageable);

    /**
     * Find audit logs for a patient filtered by action type with pagination
     */
    Page<AuditLog> findByPatientIdAndActionTypeOrderByTimestampDesc(Long patientId, ActionType actionType, Pageable pageable);

    /**
     * Find audit logs for a specific medical record by action type with pagination
     */
    Page<AuditLog> findByRecordIdAndActionTypeOrderByTimestampDesc(Long recordId, ActionType actionType, Pageable pageable);

    /**
     * Find audit logs for a patient within a time range with pagination
     */
    Page<AuditLog> findByPatientIdAndTimestampBetweenOrderByTimestampDesc(Long patientId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * Find audit logs for a specific medical record within a time range with pagination
     */
    Page<AuditLog> findByRecordIdAndTimestampBetweenOrderByTimestampDesc(Long recordId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * Find audit logs performed by a specific user with pagination
     */
    Page<AuditLog> findByPerformedByOrderByTimestampDesc(Long performedBy, Pageable pageable);

    /**
     * Find audit logs for a patient by performer and action type with pagination
     */
    Page<AuditLog> findByPatientIdAndPerformedByAndActionTypeOrderByTimestampDesc(Long patientId, Long performedBy, ActionType actionType, Pageable pageable);

    /**
     * Custom query to find audit logs with multiple filters
     */
    @Query("SELECT a FROM AuditLog a WHERE a.patientId = :patientId " +
           "AND (:recordId IS NULL OR a.recordId = :recordId) " +
           "AND (:actionType IS NULL OR a.actionType = :actionType) " +
           "AND a.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findAuditLogs(
            @Param("patientId") Long patientId,
            @Param("recordId") Long recordId,
            @Param("actionType") ActionType actionType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * Find all audit logs for a patient ordered by timestamp descending (non-paginated)
     */
    List<AuditLog> findByPatientIdOrderByTimestampDesc(Long patientId);

    /**
     * Find all audit logs for a specific medical record ordered by timestamp descending (non-paginated)
     */
    List<AuditLog> findByRecordIdOrderByTimestampDesc(Long recordId);

    /**
     * Count all audit logs for a patient
     */
    long countByPatientId(Long patientId);

    /**
     * Count all audit logs for a specific medical record
     */
    long countByRecordId(Long recordId);
}

