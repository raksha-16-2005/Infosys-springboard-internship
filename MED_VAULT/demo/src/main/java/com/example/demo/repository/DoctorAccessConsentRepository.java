package com.example.demo.repository;

import com.example.demo.model.DoctorAccessConsent;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorAccessConsentRepository extends JpaRepository<DoctorAccessConsent, Long> {
    Optional<DoctorAccessConsent> findByPatientAndDoctor(User patient, User doctor);

    boolean existsByPatientAndDoctorAndGrantedTrueAndRevokedAtIsNull(User patient, User doctor);

    List<DoctorAccessConsent> findByPatientOrderByUpdatedAtDesc(User patient);
}