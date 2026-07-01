package com.foodiego.models;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private String totalPrice;
    private long timestamp;
    private String status;

    public Order() {
    }

    public Order(String orderId, String userId, List<CartItem> items, String totalPrice, long timestamp, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> items() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CartItem> getItems() {
        return items;
    }
}
