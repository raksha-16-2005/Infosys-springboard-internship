package com.example.demo.dto;

import java.time.LocalDateTime;

public class PrescriptionResponseDTO {
    private Long id;
    private AppointmentDTO appointment;
    private String medicines;
    private String dosageInstructions;
    private LocalDateTime createdAt;

    public PrescriptionResponseDTO(){}
    public Long getId(){return id;} public void setId(Long i){this.id=i;}
    public AppointmentDTO getAppointment(){return appointment;} public void setAppointment(AppointmentDTO a){this.appointment=a;}
    public String getMedicines(){return medicines;} public void setMedicines(String m){this.medicines=m;}
    public String getDosageInstructions(){return dosageInstructions;} public void setDosageInstructions(String d){this.dosageInstructions=d;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime c){this.createdAt=c;}
}
