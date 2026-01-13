package com.example.espacecoworking.models;

public class Space {
    private double price;
    private int spaceId;
    private String name;
    private String location;
    private int capacity;
    private String description;
    private String createdAt;
    private String updatedAt;

    //Constructors
    public Space() {
    }
    public Space(int spaceId, String name, String location, int capacity, String description,double price, String createdAt, String updatedAt) {
        this.spaceId = spaceId;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //Getters
    public int getSpaceId() {
        return spaceId;
    }
    public String getName() {
        return name;
    }
    public String getLocation() {
        return location;
    }
    public int getCapacity() {
        return capacity;
    }
    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }

    //Setters
    public void setSpaceId(int spaceId) {
        this.spaceId = spaceId;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
