package com.example.espacecoworking.models;

public class SpaceImages {
    private int imageId;
    private int spaceId;
    private byte[] image;
    private String createdAt;
    private String updatedAt;

    //Constructors
    public SpaceImages() {
    }
    public SpaceImages(int imageId, byte[] image, int spaceId,String createdAt, String updatedAt) {
        this.imageId = imageId;
        this.image = image;
        this.spaceId = spaceId;
        this.createdAt = createdAt;
        this.updatedAt = "";
    }

    //Getters
    public int getImageId() {
        return imageId;
    }
    public byte[] getImage() {
        return image;
    }
    public int getSpaceId() {
        return spaceId;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }

    //Setters
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    public void setSpaceId(int spaceId) {
        this.spaceId = spaceId;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
