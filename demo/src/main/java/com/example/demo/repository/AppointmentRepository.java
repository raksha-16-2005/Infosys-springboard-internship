package com.example.demo.repository;

import com.example.demo.model.Appointment;
import com.example.demo.model.PatientProfile;
import com.example.demo.model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(PatientProfile patient);
    List<Appointment> findByDoctor(DoctorProfile doctor);
}
