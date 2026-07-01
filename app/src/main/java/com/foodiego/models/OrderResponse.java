package com.foodiego.models;

import java.io.Serializable;

/**
 * Response model for order placement.
 */
public class OrderResponse implements Serializable {
    private String orderId;

    public OrderResponse() {
    }

    public OrderResponse(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
