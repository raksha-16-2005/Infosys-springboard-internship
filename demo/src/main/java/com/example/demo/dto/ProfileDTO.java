package com.example.demo.dto;

public class ProfileDTO {
    private String fullName;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String address;
    private String emergencyContact;
    private String medicalHistory;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String hospitalName;
    private String bio;

    public String getFullName(){return fullName;} public void setFullName(String fn){this.fullName=fn;}
    public Integer getAge(){return age;} public void setAge(Integer a){this.age=a;}
    public String getGender(){return gender;} public void setGender(String g){this.gender=g;}
    public String getBloodGroup(){return bloodGroup;} public void setBloodGroup(String bg){this.bloodGroup=bg;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
    public String getEmergencyContact(){return emergencyContact;} public void setEmergencyContact(String ec){this.emergencyContact=ec;}
    public String getMedicalHistory(){return medicalHistory;} public void setMedicalHistory(String mh){this.medicalHistory=mh;}
    public String getSpecialization(){return specialization;} public void setSpecialization(String s){this.specialization=s;}
    public String getQualification(){return qualification;} public void setQualification(String q){this.qualification=q;}
    public Integer getExperienceYears(){return experienceYears;} public void setExperienceYears(Integer e){this.experienceYears=e;}
    public String getHospitalName(){return hospitalName;} public void setHospitalName(String h){this.hospitalName=h;}
    public String getBio(){return bio;} public void setBio(String b){this.bio=b;}
}
