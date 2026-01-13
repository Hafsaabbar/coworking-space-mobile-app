package com.example.espacecoworking.models;

import androidx.annotation.NonNull;

public class Client {
    private int clientId;
    private String preferences;
    private String createdAt;
    private String updatedAt;

    //Constructors
    public Client() {
    }
    public Client(int clientId, String preferences, String createdAt, String updatedAt) {
        this.clientId = clientId;
        this.preferences = preferences;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //Getters
    public int getClientId() {
        return clientId;
    }
    public String getPreferences() {
        return preferences;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }

    //Setters
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @NonNull
    @Override
    public String toString() {
        return "Client{" +
                "clientId=" + clientId +
                ", preferences='" + preferences + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
