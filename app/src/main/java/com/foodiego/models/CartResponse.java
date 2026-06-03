package com.foodiego.models;

import java.util.List;

/**
 * REST API response mapping cart listings.
 */
public class CartResponse {
    private String status;
    private String message;
    private List<CartItem> items;

    public CartResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
