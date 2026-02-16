package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class DoctorProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String fullName;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String hospitalName;
    private String phone;
    @Column(length=2000)
    private String bio;
    @Column(precision = 10, scale = 2)
    private BigDecimal consultationFee;
    @Column(length = 2000)
    private String availableSlots;
    private String profileImagePath;

    public DoctorProfile(){}
    public Long getId(){return id;}
    public User getUser(){return user;} public void setUser(User u){this.user=u;}
    public String getFullName(){return fullName;} public void setFullName(String fn){this.fullName=fn;}
    public String getSpecialization(){return specialization;} public void setSpecialization(String s){this.specialization=s;}
    public String getQualification(){return qualification;} public void setQualification(String q){this.qualification=q;}
    public Integer getExperienceYears(){return experienceYears;} public void setExperienceYears(Integer e){this.experienceYears=e;}
    public String getHospitalName(){return hospitalName;} public void setHospitalName(String h){this.hospitalName=h;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getBio(){return bio;} public void setBio(String b){this.bio=b;}
    public BigDecimal getConsultationFee(){return consultationFee;} public void setConsultationFee(BigDecimal f){this.consultationFee=f;}
    public String getAvailableSlots(){return availableSlots;} public void setAvailableSlots(String s){this.availableSlots=s;}
    public String getProfileImagePath(){return profileImagePath;} public void setProfileImagePath(String p){this.profileImagePath=p;}
}
