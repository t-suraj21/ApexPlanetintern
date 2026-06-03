package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.foodiego.R;
import com.foodiego.adapters.CartAdapter;
import com.foodiego.databinding.ActivityCartBinding;
import com.foodiego.models.CartItem;
import com.foodiego.models.GenericResponse;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Cart Activity Screen.
 * Displays selected cart items, calculates bill details dynamically, and completes checking out orders.
 */
public class CartActivity extends AppCompatActivity {

    private ActivityCartBinding binding;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartItems = new ArrayList<>();

        setupListeners();
        setupRecyclerView();
        loadCartFromREST();
        setupBottomNavigation();
        applyScaleAnimation(binding.btnCartCheckout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_cart);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_cart) {
                return true;
            }
            
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(CartActivity.this, HomeActivity.class);
            } else if (itemId == R.id.nav_orders) {
                intent = new Intent(CartActivity.this, OrdersActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(CartActivity.this, ProfileActivity.class);
            }
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {
        // Back Button
        binding.btnCartBack.setOnClickListener(v -> finishAndSlide());

        // Place Order button click
        binding.btnCartCheckout.setOnClickListener(v -> handleCheckout());
    }

    private void setupRecyclerView() {
        // Connect adapter with real-time bill calculation and REST sync callbacks
        cartAdapter = new CartAdapter(cartItems, updatedList -> {
            calculateBill();
            syncCartWithREST();
        });
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(cartAdapter);
    }

    private void loadCartFromREST() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please sign in to view your cart!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.scrollCart.setVisibility(View.GONE);
        binding.layoutEmptyCart.setVisibility(View.GONE);
        binding.btnCartCheckout.setEnabled(false);

        Repository.getInstance().getCart(userId, new Repository.ApiCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> result) {
                cartItems.clear();
                if (result != null) {
                    cartItems.addAll(result);
                }
                cartAdapter.notifyDataSetChanged();
                calculateBill();
            }

            @Override
            public void onFailure(String errorMessage) {
                // If cart is empty, let empty state render
                cartItems.clear();
                cartAdapter.notifyDataSetChanged();
                calculateBill();
            }
        });
    }

    private void syncCartWithREST() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        Repository.getInstance().syncCart(userId, cartItems, new Repository.ApiCallback<GenericResponse>() {
            @Override
            public void onSuccess(GenericResponse result) {
                // Synced
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CartActivity.this, "Cart sync failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your Cart Is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        binding.btnCartCheckout.setEnabled(false);
        binding.btnCartCheckout.setText("Placing Order...");

        // Calculate Totals
        int subtotal = 0;
        for (CartItem item : cartItems) {
            String priceStr = item.getFood().getPrice();
            int itemPrice = 0;
            try {
                itemPrice = Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            subtotal += (itemPrice * item.getQuantity());
        }
        int totalPayable = subtotal + 30 + 18;
        String totalPriceStr = "₹" + totalPayable;

        // POST request to PHP checkout transaction
        Repository.getInstance().placeOrder(userId, totalPriceStr, new Repository.ApiCallback<GenericResponse>() {
            @Override
            public void onSuccess(GenericResponse result) {
                binding.btnCartCheckout.setEnabled(true);
                binding.btnCartCheckout.setText("Place Order");

                Toast.makeText(CartActivity.this, "Order placed successfully! Thank you for ordering with FoodieGo.", Toast.LENGTH_LONG).show();
                cartItems.clear();
                cartAdapter.notifyDataSetChanged();
                calculateBill();

                // Launch order history screen
                Intent intent = new Intent(CartActivity.this, OrdersActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.btnCartCheckout.setEnabled(true);
                binding.btnCartCheckout.setText("Place Order");
                Toast.makeText(CartActivity.this, "Checkout Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Loops through cart items, extracts integer values from price strings (e.g. "₹149"),
     * and refreshes the Subtotal, Taxes, Delivery, and Total Amount fields in the UI.
     */
    private void calculateBill() {
        int subtotal = 0;

        for (CartItem item : cartItems) {
            String priceStr = item.getFood().getPrice();
            int itemPrice = 0;
            try {
                itemPrice = Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            subtotal += (itemPrice * item.getQuantity());
        }

        if (subtotal == 0) {
            // Cart is empty
            binding.scrollCart.setVisibility(View.GONE);
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.btnCartCheckout.setEnabled(false);
            binding.btnCartCheckout.setAlpha(0.6f);
            
            binding.txtBillSubtotal.setText("₹0");
            binding.txtBillDelivery.setText("₹0");
            binding.txtBillTaxes.setText("₹0");
            binding.txtBillTotal.setText("₹0");
        } else {
            // Cart has items
            binding.scrollCart.setVisibility(View.VISIBLE);
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.btnCartCheckout.setEnabled(true);
            binding.btnCartCheckout.setAlpha(1.0f);

            int deliveryFee = 30;
            int taxes = 18;
            int totalPayable = subtotal + deliveryFee + taxes;

            binding.txtBillSubtotal.setText("₹" + subtotal);
            binding.txtBillDelivery.setText("₹" + deliveryFee);
            binding.txtBillTaxes.setText("₹" + taxes);
            binding.txtBillTotal.setText("₹" + totalPayable);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void finishAndSlide() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Programmatic touch-scale micro-animation for buttons.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void applyScaleAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
            }
            return false;
        });
    }
}
