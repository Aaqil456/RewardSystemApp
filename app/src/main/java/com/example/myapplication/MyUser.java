package com.example.myapplication;

public class MyUser {
    private String uid;
    private String displayName;
    private String email;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public MyUser() {
    }

    public MyUser(String uid, String displayName, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
    }

    // Getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

