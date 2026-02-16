package com.example.demo.repository;

import com.example.demo.model.Prescription;
import com.example.demo.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByAppointmentIn(List<Appointment> appointments);
    Optional<Prescription> findByAppointment(Appointment appointment);
    
    @Query("SELECT p FROM Prescription p Where p.appointment.patient = :appointment_patient AND p.followUpDate IS NOT NULL ORDER BY p.followUpDate ASC")
    List<Prescription> findFollowUpsByPatient(@Param("appointment_patient") com.example.demo.model.PatientProfile patient);
}
