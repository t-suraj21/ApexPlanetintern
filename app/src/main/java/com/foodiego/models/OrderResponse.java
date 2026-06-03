package com.foodiego.models;

import java.util.List;

/**
 * REST API response mapping historical order listings.
 */
public class OrderResponse {
    private String status;
    private String message;
    private List<Order> orders;

    public OrderResponse() {
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

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
