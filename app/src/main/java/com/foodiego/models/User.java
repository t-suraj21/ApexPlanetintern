package com.foodiego.models;

import java.io.Serializable;

/**
 * Data Model representing a single Registered User.
 */
public class User implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String profileImage;
    private String password;
    private String phone;
    private String address;

    public User() {
    }

    public User(String userId, String name, String email, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
    }

    public User(String userId, String name, String email, String profileImage, String password, String phone, String address) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
