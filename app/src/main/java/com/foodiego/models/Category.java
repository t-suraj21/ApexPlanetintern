package com.foodiego.models;

import java.io.Serializable;

/**
 * Data Model representing a single Food Category.
 */
public class Category implements Serializable {
    private String name;
    private int iconResId;

    public Category() {
    }

    public Category(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}
