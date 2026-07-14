package com.foodiego.models;

import java.io.Serializable;

/**
 * Data Model representing a single User Delivery Address.
 */
public class Address implements Serializable {
    private String id;
    private String title;
    private String detail;
    private String phone;
    private boolean isDefault;

    public Address() {
    }

    public Address(String id, String title, String detail, String phone, boolean isDefault) {
        this.id = id;
        this.title = title;
        this.detail = detail;
        this.phone = phone;
        this.isDefault = isDefault;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
