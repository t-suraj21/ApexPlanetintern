package com.foodiego.utils;

import com.foodiego.models.CartItem;
import com.foodiego.models.Food;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton manager to track items added to the Shopping Cart in real time.
 */
public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    /**
     * Adds an item to the cart or increments its count if already present.
     */
    public void addFoodToCart(Food food, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getFood().getName().equalsIgnoreCase(food.getName())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        cartItems.add(new CartItem(food, quantity));
    }

    /**
     * Clears all items in the cart.
     */
    public void clearCart() {
        cartItems.clear();
    }
}
