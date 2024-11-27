package com.example.myapplication;

public class ClaimedReward {
    private String name;
    private int pointsRequired;
    private String claimedDate; // Store the date when the reward was claimed

    // Constructor
    public ClaimedReward(String name, int pointsRequired, String claimedDate) {
        this.name = name;
        this.pointsRequired = pointsRequired;
        this.claimedDate = claimedDate;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public String getClaimedDate() {
        return claimedDate;
    }

    // Setters (optional, depending on if you want to update these fields later)
    public void setName(String name) {
        this.name = name;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public void setClaimedDate(String claimedDate) {
        this.claimedDate = claimedDate;
    }
}
