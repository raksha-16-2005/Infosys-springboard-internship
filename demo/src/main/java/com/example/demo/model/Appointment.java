package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Appointment {
    public enum Status {PENDING, APPROVED, COMPLETED, CANCELLED}
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private PatientProfile patient;
    @ManyToOne
    private DoctorProfile doctor;
    private LocalDateTime appointmentDate;
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;
    @Column(length=1000)
    private String notes;

    public Appointment(){}
    public Long getId(){return id;}
    public PatientProfile getPatient(){return patient;} public void setPatient(PatientProfile p){this.patient=p;}
    public DoctorProfile getDoctor(){return doctor;} public void setDoctor(DoctorProfile d){this.doctor=d;}
    public LocalDateTime getAppointmentDate(){return appointmentDate;} public void setAppointmentDate(LocalDateTime ad){this.appointmentDate=ad;}
    public Status getStatus(){return status;} public void setStatus(Status s){this.status=s;}
    public String getNotes(){return notes;} public void setNotes(String n){this.notes=n;}
}
