package com.example.demo.repository;

import com.example.demo.model.PatientProfile;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    Optional<PatientProfile> findByUser(User user);
}
