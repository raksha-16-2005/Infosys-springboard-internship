package com.example.demo.dto;

public class AppointmentRequest {
    private Long doctorId;
    private String appointmentDate;
    private String symptoms;
    private String notes;

    public Long getDoctorId(){return doctorId;} 
    public void setDoctorId(Long d){this.doctorId=d;}
    public String getAppointmentDate(){return appointmentDate;} 
    public void setAppointmentDate(String ad){this.appointmentDate=ad;}
    public String getSymptoms(){return symptoms;}
    public void setSymptoms(String s){this.symptoms=s;}
    public String getNotes(){return notes;} 
    public void setNotes(String n){this.notes=n;}
}
