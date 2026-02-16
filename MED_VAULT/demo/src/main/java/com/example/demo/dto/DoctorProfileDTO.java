package com.example.demo.dto;

public class DoctorProfileDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String hospitalName;
    private String phone;
    private String bio;
    private java.math.BigDecimal consultationFee;
    private String availableSlots;
    private String profileImagePath;

    public DoctorProfileDTO(){}
    public Long getId(){return id;} public void setId(Long i){this.id=i;}
    public Long getUserId(){return userId;} public void setUserId(Long u){this.userId=u;}
    public String getFullName(){return fullName;} public void setFullName(String fn){this.fullName=fn;}
    public String getSpecialization(){return specialization;} public void setSpecialization(String s){this.specialization=s;}
    public String getQualification(){return qualification;} public void setQualification(String q){this.qualification=q;}
    public Integer getExperienceYears(){return experienceYears;} public void setExperienceYears(Integer e){this.experienceYears=e;}
    public String getHospitalName(){return hospitalName;} public void setHospitalName(String h){this.hospitalName=h;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getBio(){return bio;} public void setBio(String b){this.bio=b;}
    public java.math.BigDecimal getConsultationFee(){return consultationFee;} public void setConsultationFee(java.math.BigDecimal f){this.consultationFee=f;}
    public String getAvailableSlots(){return availableSlots;} public void setAvailableSlots(String s){this.availableSlots=s;}
    public String getProfileImagePath(){return profileImagePath;} public void setProfileImagePath(String p){this.profileImagePath=p;}
}
