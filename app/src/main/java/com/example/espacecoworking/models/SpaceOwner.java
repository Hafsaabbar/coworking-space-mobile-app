package com.example.espacecoworking.models;

public class SpaceOwner {
    private int spOwId;
    private int ownerId;
    private int spaceId;

    //Constructor
    public SpaceOwner(){
    }
    public SpaceOwner(int spOwId, int ownerId, int spaceId) {
        this.spOwId = spOwId;
        this.ownerId = ownerId;
        this.spaceId = spaceId;
    }

    //Getters
    public int getSpOwId() {
        return spOwId;
    }
    public int getOwnerId() {
        return ownerId;
    }
    public int getSpaceId() {
        return spaceId;
    }

    //Setters
    public void setSpOwId(int spOwId) {
        this.spOwId = spOwId;
    }
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    public void setSpaceId(int spaceId) {
        this.spaceId = spaceId;
    }
}
