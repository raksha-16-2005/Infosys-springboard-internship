package com.example.demo.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class PrescriptionResponseDTO {
    private Long id;
    private AppointmentDTO appointment;
    private String diagnosis;
    private String medicinesJson;
    private String testsRecommended;
    private LocalDate followUpDate;
    private String notes;
    private LocalDateTime createdAt;

    public PrescriptionResponseDTO(){}
    public Long getId(){return id;} 
    public void setId(Long i){this.id=i;}
    public AppointmentDTO getAppointment(){return appointment;} 
    public void setAppointment(AppointmentDTO a){this.appointment=a;}
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
