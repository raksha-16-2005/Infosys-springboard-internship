package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "prescriptions")
public class Prescription {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true)
    @JsonIgnore
    private Appointment appointment;
    
    @Column(length = 2000)
    private String diagnosis;
    
    @Column(length = 3000, columnDefinition = "TEXT")
    private String medicinesJson; // JSON array of medicines
    
    @Column(length = 2000)
    private String testsRecommended;
    
    private LocalDate followUpDate;
    
    @Column(length = 2000)
    private String notes;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Prescription(){}
    
    public Long getId(){return id;}
    public Appointment getAppointment(){return appointment;} 
    public void setAppointment(Appointment a){this.appointment=a;}
    public String getDiagnosis(){return diagnosis;}
    public void setDiagnosis(String d){this.diagnosis=d;}
    public String getMedicinesJson(){return medicinesJson;}
    public void setMedicinesJson(String m){this.medicinesJson=m;}
    public String getTestsRecommended(){return testsRecommended;}
    public void setTestsRecommended(String t){this.testsRecommended=t;}
    public LocalDate getFollowUpDate(){return followUpDate;}
    public void setFollowUpDate(LocalDate f){this.followUpDate=f;}
    public String getNotes(){return notes;}
    public void setNotes(String n){this.notes=n;}
    public LocalDateTime getCreatedAt(){return createdAt;} 
    public void setCreatedAt(LocalDateTime c){this.createdAt=c;}
}
