package com.foodiego.models;

import java.io.Serializable;

/**
 * Data Model representing a single Food Item.
 */
public class Food implements Serializable {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String price;
    private String rating;
    private String deliveryTime;

    public Food() {
    }

    public Food(String id, String name, String description, String imageUrl, String price, String rating, String deliveryTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}
