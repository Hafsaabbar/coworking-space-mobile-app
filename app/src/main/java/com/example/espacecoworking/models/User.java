package com.example.espacecoworking.models;

import androidx.annotation.NonNull;

public class User {
    private int userId;
    private String name;
    private String email;
    private String password;
    private String role;
    private String phone;

    private String firebaseUid;
    private String createdAt;
    private String updatedAt;
    private byte[] image;

    //Constructors
    public User() {
    }

    public User(int userId, String name, String email, String password, String role, String phone, String createdAt, byte[] image) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = "";
        this.image = image;
    }

    //Getters
    public int getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getRole() {
        return role;
    }
    public String getPhone() {
        return phone;
    }
    public String getFirebaseUid() { return firebaseUid; }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }
    public byte[] getImage() {
        return image;
    }

    //Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{\n" +
                "userId=" + userId +
                ", name='" + name +
                ", email='" + email +
                ", password='" + password +
                ", role='" + role +
                ", phone='" + phone +
                ", firebaseUid='" + firebaseUid +
                ", createdAt='" + createdAt +
                ", updatedAt='" + updatedAt +
                ", image=" + "image=" + (image != null ? image.length + " bytes\n" : "null\n")+
                '}';
    }
}
