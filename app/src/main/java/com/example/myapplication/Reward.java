package com.example.myapplication;

public class Reward {
    private String name;
    private int pointsRequired;

    public Reward(String name, int pointsRequired) {
        this.name = name;
        this.pointsRequired = pointsRequired;
    }

    public String getName() {
        return name;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }
}

