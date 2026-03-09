package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_consent",
       indexes = {
           @Index(name = "idx_patient_doctor", columnList = "patientId,doctorId"),
           @Index(name = "idx_patient_id", columnList = "patientId")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private Boolean consentGranted;

    @Column(nullable = false)
    private LocalDateTime grantedAt;

    private LocalDateTime revokedAt;

    @Column(length = 500)
    private String consentReason;

    @PrePersist
    public void onCreate() {
        if (this.consentGranted == null) {
            this.consentGranted = true;
        }
        if (this.grantedAt == null) {
            this.grantedAt = LocalDateTime.now();
        }
    }

    // Manual getters and setters for compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Boolean getConsentGranted() { return consentGranted; }
    public void setConsentGranted(Boolean consentGranted) { this.consentGranted = consentGranted; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
    public void setGrantedAt(LocalDateTime grantedAt) { this.grantedAt = grantedAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public String getConsentReason() { return consentReason; }
    public void setConsentReason(String consentReason) { this.consentReason = consentReason; }
}
