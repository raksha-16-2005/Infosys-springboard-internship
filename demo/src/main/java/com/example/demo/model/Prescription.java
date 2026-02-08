package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Prescription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Appointment appointment;
    @Column(length=2000)
    private String medicines;
    @Column(length=2000)
    private String dosageInstructions;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Prescription(){}
    public Long getId(){return id;}
    public Appointment getAppointment(){return appointment;} public void setAppointment(Appointment a){this.appointment=a;}
    public String getMedicines(){return medicines;} public void setMedicines(String m){this.medicines=m;}
    public String getDosageInstructions(){return dosageInstructions;} public void setDosageInstructions(String d){this.dosageInstructions=d;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime c){this.createdAt=c;}
}
