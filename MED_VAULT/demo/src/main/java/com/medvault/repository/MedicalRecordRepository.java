package com.medvault.repository;

import com.medvault.entity.MedicalRecord;
import com.medvault.entity.RecordCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// Note: Not registered as @Repository to avoid conflicts with com.example.demo.repository.MedicalRecordRepository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    
    // Find active, non-deleted records for a patient
        List<MedicalRecord> findByPatientIdAndIsDeletedFalseAndIsActiveTrue(Long patientId);
    
    // Find by patient and category, sorted by date descending
    List<MedicalRecord> findByPatientIdAndCategoryAndIsDeletedFalseAndIsActiveTrueOrderByUploadDateDesc(
            Long patientId, RecordCategory category);
    
    // Find by patient, sorted by date descending
        List<MedicalRecord> findByPatientIdAndIsDeletedFalseAndIsActiveTrueOrderByUploadDateDesc(Long patientId);
    
    // Find by patient and date range, sorted by date descending
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
           "AND mr.isDeleted = false AND mr.isActive = true " +
           "AND mr.uploadDate >= :startDate AND mr.uploadDate <= :endDate " +
           "ORDER BY mr.uploadDate DESC")
    List<MedicalRecord> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find by patient, category and date range, sorted by date descending
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
           "AND mr.category = :category AND mr.isDeleted = false AND mr.isActive = true " +
           "AND mr.uploadDate >= :startDate AND mr.uploadDate <= :endDate " +
           "ORDER BY mr.uploadDate DESC")
    List<MedicalRecord> findByPatientIdAndCategoryAndDateRange(
            @Param("patientId") Long patientId,
            @Param("category") RecordCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Find latest version of a record for a specific patient and file
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.patientId = :patientId " +
           "AND mr.fileName = :fileName AND mr.isDeleted = false " +
           "ORDER BY mr.versionNumber DESC")
    List<MedicalRecord> findLatestVersionByPatientIdAndFileName(
            @Param("patientId") Long patientId,
            @Param("fileName") String fileName);
}
