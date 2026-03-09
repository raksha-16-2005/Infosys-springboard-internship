package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_record")
public class MedicalRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User patient;
    private String filename;
    private String content;
    private String filePath;
    private String doctorName;
    private LocalDateTime uploadedAt = LocalDateTime.now();
    public MedicalRecord(){}
    public Long getId(){return id;}
    public User getPatient(){return patient;} public void setPatient(User p){this.patient=p;}
    public String getFilename(){return filename;} public void setFilename(String f){this.filename=f;}
    public String getContent(){return content;} public void setContent(String c){this.content=c;}
    public String getFilePath(){return filePath;} public void setFilePath(String p){this.filePath=p;}
    public String getDoctorName(){return doctorName;} public void setDoctorName(String d){this.doctorName=d;}
    public LocalDateTime getUploadedAt(){return uploadedAt;} public void setUploadedAt(LocalDateTime u){this.uploadedAt=u;}
}
