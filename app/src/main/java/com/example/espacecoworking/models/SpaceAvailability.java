package com.example.espacecoworking.models;

public class SpaceAvailability {
    private int availabilityId;
    private int spaceId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean isAvailable;

    //Constructors
    public SpaceAvailability() {
    }
    public SpaceAvailability(int availabilityId, int spaceId, String dayOfWeek, String startTime, String endTime, boolean isAvailable) {
        this.availabilityId = availabilityId;
        this.spaceId = spaceId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
    }

    //Getters
    public int getAvailabilityId() {
        return availabilityId;
    }
    public int getSpaceId() {
        return spaceId;
    }
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    public String getStartTime() {
        return startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public boolean isAvailable() {
        return isAvailable;
    }

    //Setters
    public void setAvailabilityId(int availabilityId) {
        this.availabilityId = availabilityId;
    }
    public void setSpaceId(int spaceId) {
        this.spaceId = spaceId;
    }
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}
