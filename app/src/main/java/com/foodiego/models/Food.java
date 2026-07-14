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
    private String category;
    private String restaurant;
    private boolean isVeg;
    private boolean isPopular;
    private long timestamp;

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
        this.category = "General";
        this.restaurant = "FoodieGo Kitchen";
        this.isVeg = true;
        this.isPopular = false;
        this.timestamp = System.currentTimeMillis();
    }

    public Food(String id, String name, String description, String imageUrl, String price, String rating, String deliveryTime,
                String category, String restaurant, boolean isVeg, boolean isPopular, long timestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.category = category;
        this.restaurant = restaurant;
        this.isVeg = isVeg;
        this.isPopular = isPopular;
        this.timestamp = timestamp;
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

    public String getCategory() {
        return category != null ? category : "General";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRestaurant() {
        return restaurant != null ? restaurant : "FoodieGo Kitchen";
    }

    public void setRestaurant(String restaurant) {
        this.restaurant = restaurant;
    }

    public boolean isVeg() {
        return isVeg;
    }

    public void setVeg(boolean veg) {
        isVeg = veg;
    }

    public boolean isPopular() {
        return isPopular;
    }

    public void setPopular(boolean popular) {
        isPopular = popular;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
