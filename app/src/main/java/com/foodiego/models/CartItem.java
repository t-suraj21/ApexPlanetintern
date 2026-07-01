package com.foodiego.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private Food food;
    private int quantity;

    public CartItem() { }

    public CartItem(Food food, int quantity) {
        this.food = food;
        this.quantity = quantity;
    }

    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Helper for Firestore document ID
    public String getFoodId() {
        return food != null ? food.getId() : null;
    }
}
