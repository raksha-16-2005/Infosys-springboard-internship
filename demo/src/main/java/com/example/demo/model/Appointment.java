package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_patient", columnList = "patient_id"),
    @Index(name = "idx_doctor", columnList = "doctor_id"),
    @Index(name = "idx_date", columnList = "appointment_date")
})
public class Appointment {
    public enum Status {SCHEDULED, COMPLETED, CANCELLED, NO_SHOW}
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private PatientProfile patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private DoctorProfile doctor;
    
    private LocalDateTime appointmentDate;
    
    @Enumerated(EnumType.STRING)
    private Status status = Status.SCHEDULED;
    
    @Column(length = 1000)
    private String symptoms;
    
    @Column(length = 2000)
    private String consultationNotes;
    
    @Column(length = 1000)
    private String notes;
    
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Prescription prescription;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Appointment(){}
    
    public Long getId(){return id;}
    public PatientProfile getPatient(){return patient;} 
    public void setPatient(PatientProfile p){this.patient=p;}
    public DoctorProfile getDoctor(){return doctor;} 
    public void setDoctor(DoctorProfile d){this.doctor=d;}
    public LocalDateTime getAppointmentDate(){return appointmentDate;} 
    public void setAppointmentDate(LocalDateTime ad){this.appointmentDate=ad;}
    public Status getStatus(){return status;} 
    public void setStatus(Status s){this.status=s;}
    public String getNotes(){return notes;} 
    public void setNotes(String n){this.notes=n;}
    public String getSymptoms(){return symptoms;}
    public void setSymptoms(String s){this.symptoms=s;}
    public String getConsultationNotes(){return consultationNotes;}
    public void setConsultationNotes(String cn){this.consultationNotes=cn;}
    public Prescription getPrescription(){return prescription;}
    public void setPrescription(Prescription p){this.prescription=p;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime c){this.createdAt=c;}
}
