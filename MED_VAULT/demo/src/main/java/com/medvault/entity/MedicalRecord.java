package com.medvault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records",
       indexes = {
           @Index(name = "idx_patient_id", columnList = "patientId")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private Long uploadedBy;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordCategory category;

    @Column(nullable = false)
    private String fileUrl;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private Boolean isDeleted;

    private LocalDateTime uploadDate;
    private LocalDateTime lastModifiedDate;

    @PrePersist
    public void onCreate() {
        this.uploadDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
        this.isDeleted = false;
        this.isActive = true;
        this.versionNumber = 1;
    }

    @PreUpdate
    public void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }

    // Manual getters and setters for compatibility
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public RecordCategory getCategory() { return category; }
    public void setCategory(RecordCategory category) { this.category = category; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }
    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    // Builder pattern static method
    public static MedicalRecordBuilder builder() {
        return new MedicalRecordBuilder();
    }

    // Builder inner class
    public static class MedicalRecordBuilder {
        private Long id;
        private Long patientId;
        private Long uploadedBy;
        private String fileName;
        private String fileType;
        private RecordCategory category;
        private String fileUrl;
        private String notes;
        private Integer versionNumber;
        private Boolean isActive;
        private Boolean isDeleted;
        private LocalDateTime uploadDate;
        private LocalDateTime lastModifiedDate;

        public MedicalRecordBuilder id(Long id) { this.id = id; return this; }
        public MedicalRecordBuilder patientId(Long patientId) { this.patientId = patientId; return this; }
        public MedicalRecordBuilder uploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; return this; }
        public MedicalRecordBuilder fileName(String fileName) { this.fileName = fileName; return this; }
        public MedicalRecordBuilder fileType(String fileType) { this.fileType = fileType; return this; }
        public MedicalRecordBuilder category(RecordCategory category) { this.category = category; return this; }
        public MedicalRecordBuilder fileUrl(String fileUrl) { this.fileUrl = fileUrl; return this; }
        public MedicalRecordBuilder notes(String notes) { this.notes = notes; return this; }
        public MedicalRecordBuilder versionNumber(Integer versionNumber) { this.versionNumber = versionNumber; return this; }
        public MedicalRecordBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public MedicalRecordBuilder isDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; return this; }
        public MedicalRecordBuilder uploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; return this; }
        public MedicalRecordBuilder lastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; return this; }

        public MedicalRecord build() {
            MedicalRecord record = new MedicalRecord();
            record.id = this.id;
            record.patientId = this.patientId;
            record.uploadedBy = this.uploadedBy;
            record.fileName = this.fileName;
            record.fileType = this.fileType;
            record.category = this.category;
            record.fileUrl = this.fileUrl;
            record.notes = this.notes;
            record.versionNumber = this.versionNumber;
            record.isActive = this.isActive;
            record.isDeleted = this.isDeleted;
            record.uploadDate = this.uploadDate;
            record.lastModifiedDate = this.lastModifiedDate;
            return record;
        }
    }
}
