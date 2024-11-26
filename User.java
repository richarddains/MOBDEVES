package com.example.jotty;

public class User {
    private String userId;
    private String username;
    private String email;
    private String password;
    private String bio;  // New field for bio

    public User() {}

    public User(String userId, String username, String email, String password, String bio) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.bio = bio;  // Set the bio
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
