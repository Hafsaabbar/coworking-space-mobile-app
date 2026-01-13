package com.example.espacecoworking.models;

public class Booking {
    private int bookingId;
    private int clientId;
    private int spaceId;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
    private String createdAt;
    private String updatedAt;

    //Constructors
    public Booking() {
    }
    public Booking(int bookingId, int clientId, int spaceId, String date, String startTime, String endTime, String status, String createdAt){
        this.bookingId = bookingId;
        this.clientId = clientId;
        this.spaceId = spaceId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = "";
    }

    //Getters
    public int getBookingId() {
        return bookingId;
    }
    public int getClientId() {
        return clientId;
    }
    public int getSpaceId() {
        return spaceId;
    }
    public String getDate() {
        return date;
    }
    public String getStartTime() {
        return startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public String getStatus() {
        return status;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }

    //Setters
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    public void setSpaceId(int spaceId) {
        this.spaceId = spaceId;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
