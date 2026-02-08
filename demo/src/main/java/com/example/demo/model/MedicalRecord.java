package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class MedicalRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User patient;
    private String filename;
    private String content;
    private LocalDateTime uploadedAt = LocalDateTime.now();
    public MedicalRecord(){}
    public Long getId(){return id;}
    public User getPatient(){return patient;} public void setPatient(User p){this.patient=p;}
    public String getFilename(){return filename;} public void setFilename(String f){this.filename=f;}
    public String getContent(){return content;} public void setContent(String c){this.content=c;}
    public LocalDateTime getUploadedAt(){return uploadedAt;}
}
