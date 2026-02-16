package com.example.demo.dto;

public class PatientProfileDTO {
    private Long id;
    private String fullName;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private String phone;
    private String address;
    private String emergencyContact;
    private String medicalHistory;

    public PatientProfileDTO(){}
    public Long getId(){return id;} public void setId(Long i){this.id=i;}
    public String getFullName(){return fullName;} public void setFullName(String fn){this.fullName=fn;}
    public Integer getAge(){return age;} public void setAge(Integer a){this.age=a;}
    public String getGender(){return gender;} public void setGender(String g){this.gender=g;}
    public String getBloodGroup(){return bloodGroup;} public void setBloodGroup(String bg){this.bloodGroup=bg;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
    public String getEmergencyContact(){return emergencyContact;} public void setEmergencyContact(String ec){this.emergencyContact=ec;}
    public String getMedicalHistory(){return medicalHistory;} public void setMedicalHistory(String mh){this.medicalHistory=mh;}
}
