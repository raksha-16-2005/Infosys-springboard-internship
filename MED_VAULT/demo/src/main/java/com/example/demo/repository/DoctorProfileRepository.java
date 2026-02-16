package com.example.demo.repository;

import com.example.demo.model.DoctorProfile;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    Optional<DoctorProfile> findByUser(User user);
}
