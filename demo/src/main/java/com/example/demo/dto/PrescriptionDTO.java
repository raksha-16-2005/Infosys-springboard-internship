package com.example.demo.dto;

public class PrescriptionDTO {
    private Long appointmentId;
    private String medicines;
    private String dosageInstructions;

    public Long getAppointmentId(){return appointmentId;} public void setAppointmentId(Long a){this.appointmentId=a;}
    public String getMedicines(){return medicines;} public void setMedicines(String m){this.medicines=m;}
    public String getDosageInstructions(){return dosageInstructions;} public void setDosageInstructions(String d){this.dosageInstructions=d;}
}
