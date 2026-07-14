package com.foodiego;

import org.junit.Test;
import static org.junit.Assert.*;

import com.foodiego.models.CartItem;
import com.foodiego.models.Food;
import com.foodiego.models.Order;
import com.foodiego.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Unit tests validating credential inputs syntax, cart total calculations,
 * and data models integrity.
 */
public class LoginRegisterValidationTest {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
    );

    // --- Validation Helpers ---

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidRegister(String name, String email, String password, String confirmPassword) {
        if (name == null || name.trim().length() < 3) return false;
        if (!isValidEmail(email)) return false;
        if (password == null || password.length() < 6) return false;
        return password.equals(confirmPassword);
    }

    // --- Tests ---

    @Test
    public void testEmailValidation() {
        assertTrue(isValidEmail("user@example.com"));
        assertTrue(isValidEmail("john.doe@sub.domain.co.in"));
        assertFalse(isValidEmail("user"));
        assertFalse(isValidEmail("user@"));
        assertFalse(isValidEmail("user@domain"));
        assertFalse(isValidEmail(null));
    }

    @Test
    public void testRegisterValidation() {
        assertTrue(isValidRegister("Rahul Sharma", "rahul@gmail.com", "secure123", "secure123"));
        // Name too short
        assertFalse(isValidRegister("Ab", "rahul@gmail.com", "secure123", "secure123"));
        // Invalid email
        assertFalse(isValidRegister("Rahul Sharma", "rahul@domain", "secure123", "secure123"));
        // Password too short
        assertFalse(isValidRegister("Rahul Sharma", "rahul@gmail.com", "123", "123"));
        // Passwords mismatch
        assertFalse(isValidRegister("Rahul Sharma", "rahul@gmail.com", "secure123", "wrongpass"));
    }

    @Test
    public void testCartTotalCalculation() {
        // Setup cart items
        List<CartItem> items = new ArrayList<>();
        
        Food pizza = new Food("1", "Pizza", "Delicious", "", "₹350", "4.5", "20 min");
        Food burger = new Food("2", "Burger", "Juicy", "", "₹150", "4.3", "15 min");
        
        items.add(new CartItem(pizza, 2)); // 2 * 350 = 700
        items.add(new CartItem(burger, 1)); // 1 * 150 = 150
        
        // Sum subtotal
        int subtotal = 0;
        for (CartItem item : items) {
            int price = Integer.parseInt(item.getFood().getPrice().replaceAll("[^0-9]", ""));
            subtotal += (price * item.getQuantity());
        }
        
        assertEquals(850, subtotal);
        
        // Add tax (18) and delivery (30)
        int delivery = 30;
        int tax = 18;
        int grandTotal = subtotal + delivery + tax;
        
        assertEquals(898, grandTotal);
    }

    @Test
    public void testFoodModel() {
        Food food = new Food("3", "Pasta", "Creamy", "http://image.url", "₹200", "4.2", "25 min", "Pasta", "Italian Cafe", true, true, 1000L);
        assertEquals("3", food.getId());
        assertEquals("Pasta", food.getName());
        assertEquals("Creamy", food.getDescription());
        assertEquals("http://image.url", food.getImageUrl());
        assertEquals("₹200", food.getPrice());
        assertEquals("4.2", food.getRating());
        assertEquals("25 min", food.getDeliveryTime());
        assertEquals("Pasta", food.getCategory());
        assertEquals("Italian Cafe", food.getRestaurant());
        assertTrue(food.isVeg());
        assertTrue(food.isPopular());
        assertEquals(1000L, food.getTimestamp());
    }

    @Test
    public void testOrderModel() {
        Order order = new Order();
        order.setOrderId("ord_123");
        order.setUserId("user_456");
        order.setTotalPrice("₹500");
        order.setStatus("Preparing");
        order.setTimestamp(5000L);
        
        assertEquals("ord_123", order.getOrderId());
        assertEquals("user_456", order.getUserId());
        assertEquals("₹500", order.getTotalPrice());
        assertEquals("Preparing", order.getStatus());
        assertEquals(5000L, order.getTimestamp());
    }
}
