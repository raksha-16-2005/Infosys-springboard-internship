package com.example.demo.model;

import jakarta.persistence.*;

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
}
