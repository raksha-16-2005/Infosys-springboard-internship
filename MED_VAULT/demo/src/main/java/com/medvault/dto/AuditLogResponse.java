package com.medvault.dto;

import com.medvault.entity.ActionType;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private Long recordId;
    private Long patientId;
    private Long performedBy;
    private String role;
    private ActionType actionType;
    private LocalDateTime timestamp;
    private String details;

    // Manual getters and setters for compatibility
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
    public static AuditLogResponseBuilder builder() {
        return new AuditLogResponseBuilder();
    }

    // Builder inner class
    public static class AuditLogResponseBuilder {
        private Long id;
        private Long recordId;
        private Long patientId;
        private Long performedBy;
        private String role;
        private ActionType actionType;
        private LocalDateTime timestamp;
        private String details;

        public AuditLogResponseBuilder id(Long id) { this.id = id; return this; }
        public AuditLogResponseBuilder recordId(Long recordId) { this.recordId = recordId; return this; }
        public AuditLogResponseBuilder patientId(Long patientId) { this.patientId = patientId; return this; }
        public AuditLogResponseBuilder performedBy(Long performedBy) { this.performedBy = performedBy; return this; }
        public AuditLogResponseBuilder role(String role) { this.role = role; return this; }
        public AuditLogResponseBuilder actionType(ActionType actionType) { this.actionType = actionType; return this; }
        public AuditLogResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public AuditLogResponseBuilder details(String details) { this.details = details; return this; }

        public AuditLogResponse build() {
            AuditLogResponse response = new AuditLogResponse();
            response.id = this.id;
            response.recordId = this.recordId;
            response.patientId = this.patientId;
            response.performedBy = this.performedBy;
            response.role = this.role;
            response.actionType = this.actionType;
            response.timestamp = this.timestamp;
            response.details = this.details;
            return response;
        }
    }
}
