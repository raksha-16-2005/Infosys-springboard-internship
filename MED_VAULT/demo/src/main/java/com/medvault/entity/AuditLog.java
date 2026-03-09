package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs",
       indexes = {
           @Index(name = "idx_record_id", columnList = "recordId"),
           @Index(name = "idx_patient_id", columnList = "patientId"),
           @Index(name = "idx_timestamp", columnList = "timestamp"),
           @Index(name = "idx_performed_by", columnList = "performedBy"),
           @Index(name = "idx_action_type", columnList = "actionType")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Long recordId;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long performedBy;

    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String details;

    @PrePersist
    public void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // Manual getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getPerformedBy() { return performedBy; }
    public void setPerformedBy(Long performedBy) { this.performedBy = performedBy; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    // Builder pattern static method
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    // Builder inner class
    public static class AuditLogBuilder {
        private Long id;
        private Long recordId;
        private Long patientId;
        private Long performedBy;
        private String role;
        private ActionType actionType;
        private LocalDateTime timestamp;
        private String details;

        public AuditLogBuilder id(Long id) { this.id = id; return this; }
        public AuditLogBuilder recordId(Long recordId) { this.recordId = recordId; return this; }
        public AuditLogBuilder patientId(Long patientId) { this.patientId = patientId; return this; }
        public AuditLogBuilder performedBy(Long performedBy) { this.performedBy = performedBy; return this; }
        public AuditLogBuilder role(String role) { this.role = role; return this; }
        public AuditLogBuilder actionType(ActionType actionType) { this.actionType = actionType; return this; }
        public AuditLogBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public AuditLogBuilder details(String details) { this.details = details; return this; }

        public AuditLog build() {
            AuditLog log = new AuditLog();
            log.id = this.id;
            log.recordId = this.recordId;
            log.patientId = this.patientId;
            log.performedBy = this.performedBy;
            log.role = this.role;
            log.actionType = this.actionType;
            log.timestamp = this.timestamp;
            log.details = this.details;
            return log;
        }
    }
}
