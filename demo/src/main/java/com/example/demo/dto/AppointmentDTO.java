package com.example.demo.dto;

import java.time.LocalDateTime;

public class AppointmentDTO {
    private Long id;
    private PatientProfileDTO patient;
    private DoctorProfileDTO doctor;
    private LocalDateTime appointmentDate;
    private String status;
    private String symptoms;
    private String consultationNotes;
    private String notes;
    private LocalDateTime createdAt;

    public AppointmentDTO(){}
    public Long getId(){return id;} 
    public void setId(Long i){this.id=i;}
    public PatientProfileDTO getPatient(){return patient;} 
    public void setPatient(PatientProfileDTO p){this.patient=p;}
    public DoctorProfileDTO getDoctor(){return doctor;} 
    public void setDoctor(DoctorProfileDTO d){this.doctor=d;}
    public LocalDateTime getAppointmentDate(){return appointmentDate;} 
    public void setAppointmentDate(LocalDateTime ad){this.appointmentDate=ad;}
    public String getStatus(){return status;} 
    public void setStatus(String s){this.status=s;}
    public String getNotes(){return notes;} 
    public void setNotes(String n){this.notes=n;}
    public String getSymptoms(){return symptoms;}
    public void setSymptoms(String s){this.symptoms=s;}
    public String getConsultationNotes(){return consultationNotes;}
    public void setConsultationNotes(String cn){this.consultationNotes=cn;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime c){this.createdAt=c;}
}
