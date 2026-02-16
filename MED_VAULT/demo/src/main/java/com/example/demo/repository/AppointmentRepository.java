package com.example.demo.repository;

import com.example.demo.model.Appointment;
import com.example.demo.model.PatientProfile;
import com.example.demo.model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(PatientProfile patient);
    List<Appointment> findByDoctor(DoctorProfile doctor);

    long countByStatus(Appointment.Status status);

    long countByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND DATE(a.appointmentDate) = :date ORDER BY a.appointmentDate ASC")
    List<Appointment> findByDoctorAndDate(@Param("doctor") DoctorProfile doctor, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient AND a.appointmentDate >= :from AND a.appointmentDate <= :to ORDER BY a.appointmentDate DESC")
    List<Appointment> findByPatientInDateRange(@Param("patient") PatientProfile patient, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.status = :status ORDER BY a.appointmentDate DESC")
    List<Appointment> findByDoctorAndStatus(@Param("doctor") DoctorProfile doctor, @Param("status") Appointment.Status status);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient AND a.appointmentDate >= :date ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingByPatient(@Param("patient") PatientProfile patient, @Param("date") LocalDateTime date);
}
