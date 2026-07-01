package com.foodiego.network;

import com.foodiego.models.CartItem;
import java.util.List;

public class CartSyncBody {
    private List<CartItem> items;

    public CartSyncBody(List<CartItem> items) {
        this.items = items;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
