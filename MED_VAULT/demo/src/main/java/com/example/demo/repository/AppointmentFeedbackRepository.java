package com.example.demo.repository;

import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentFeedback;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentFeedbackRepository extends JpaRepository<AppointmentFeedback, Long> {
    Optional<AppointmentFeedback> findByAppointment(Appointment appointment);
    List<AppointmentFeedback> findByPatientUserOrderByCreatedAtDesc(User patientUser);
    List<AppointmentFeedback> findByDoctorUserOrderByCreatedAtDesc(User doctorUser);
}
