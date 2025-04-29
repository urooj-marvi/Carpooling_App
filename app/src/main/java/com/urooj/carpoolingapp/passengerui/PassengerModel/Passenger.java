package com.urooj.carpoolingapp.passengerui.PassengerModel;

import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
@Keep
public class Passenger{
    private String userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String userType; // "driver" or "passenger"

    // Common fields for both users
    private String caste;
    private String cnic;
    private String gender; // "male" or "female"

    // Additional fields for drivers
    private String religion;
    private String nationality;
    private String carNumber;
    private String homeAddress;
    private String driverPhotoUrl;
    private String licensePhotoUrl;

    // Profile fields
    private String profileImageUrl;
    private String address;

    // Default constructor required for Firebase
    public Passenger() {
        // Default empty constructor required for Firebase
        this.profileImageUrl = "";
        this.address = "";
    }

    // Constructor for Passenger
    public Passenger(String userId, String email, String fullName, String phoneNumber,
                String caste, String cnic, String gender) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.userType = "passenger";
        this.caste = caste;
        this.cnic = cnic;
        this.gender = gender;
        this.profileImageUrl = "";
        this.address = "";
    }

    // Constructor for Driver
    public Passenger(String userId, String email, String fullName, String phoneNumber,
                String caste, String cnic, String gender, String religion,
                String nationality, String carNumber, String homeAddress) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.userType = "driver";
        this.caste = caste;
        this.cnic = cnic;
        this.gender = gender;
        this.religion = religion;
        this.nationality = nationality;
        this.carNumber = carNumber;
        this.homeAddress = homeAddress;
        this.profileImageUrl = "";
        this.address = homeAddress;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfileImageUrl() {
        return profileImageUrl != null ? profileImageUrl : "";
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getAddress() {
        return address != null ? address : "";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCaste() {
        return caste != null ? caste : "";
    }

    public void setCaste(String caste) {
        this.caste = caste;
    }

    public String getCnic() {
        return cnic != null ? cnic : "";
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getGender() {
        return gender != null ? gender : "";
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getReligion() {
        return religion != null ? religion : "";
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getNationality() {
        return nationality != null ? nationality : "";
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getCarNumber() {
        return carNumber != null ? carNumber : "";
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getHomeAddress() {
        return homeAddress != null ? homeAddress : "";
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getDriverPhotoUrl() {
        return driverPhotoUrl != null ? driverPhotoUrl : "";
    }

    public void setDriverPhotoUrl(String driverPhotoUrl) {
        this.driverPhotoUrl = driverPhotoUrl;
    }

    public String getLicensePhotoUrl() {
        return licensePhotoUrl != null ? licensePhotoUrl : "";
    }

    public void setLicensePhotoUrl(String licensePhotoUrl) {
        this.licensePhotoUrl = licensePhotoUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", userType='" + userType + '\'' +
                ", caste='" + caste + '\'' +
                ", cnic='" + cnic + '\'' +
                ", gender='" + gender + '\'' +
                (userType != null && userType.equals("driver") ?
                        ", religion='" + religion + '\'' +
                                ", nationality='" + nationality + '\'' +
                                ", carNumber='" + carNumber + '\'' +
                                ", homeAddress='" + homeAddress + '\'' +
                                ", driverPhotoUrl='" + driverPhotoUrl + '\'' +
                                ", licensePhotoUrl='" + licensePhotoUrl + '\'' : "") +
                '}';
    }
}