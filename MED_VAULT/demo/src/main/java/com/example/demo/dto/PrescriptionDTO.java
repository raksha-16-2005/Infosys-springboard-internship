package com.example.demo.dto;

import java.time.LocalDate;

public class PrescriptionDTO {
    private Long appointmentId;
    private String diagnosis;
    private String medicinesJson; // JSON array of medicine objects
    private String testsRecommended;
    private LocalDate followUpDate;
    private String notes;

    public PrescriptionDTO(){}
    public Long getAppointmentId(){return appointmentId;} 
    public void setAppointmentId(Long a){this.appointmentId=a;}
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
}
