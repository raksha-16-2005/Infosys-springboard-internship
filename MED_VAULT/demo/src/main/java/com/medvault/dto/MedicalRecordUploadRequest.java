package com.medvault.dto;

import com.medvault.entity.RecordCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordUploadRequest {
    private Long patientId;
    private RecordCategory category;
    private String notes;

    // Explicit getters and setters for compatibility
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public RecordCategory getCategory() { return category; }
    public void setCategory(RecordCategory category) { this.category = category; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
