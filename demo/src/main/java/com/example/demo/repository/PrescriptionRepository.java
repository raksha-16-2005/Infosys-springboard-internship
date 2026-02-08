package com.example.demo.repository;

import com.example.demo.model.Prescription;
import com.example.demo.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByAppointmentIn(List<Appointment> appointments);
}
