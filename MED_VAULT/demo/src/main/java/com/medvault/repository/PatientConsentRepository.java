package com.medvault.repository;

import com.medvault.entity.PatientConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Note: Not registered as @Repository to avoid conflicts with demo repositories
public interface PatientConsentRepository extends JpaRepository<PatientConsent, Long> {
    Optional<PatientConsent> findByPatientIdAndDoctorId(Long patientId, Long doctorId);
}
