package com.medvault.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordUpdateRequest {
    private String notes;

    // Explicit getters and setters for compatibility
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
