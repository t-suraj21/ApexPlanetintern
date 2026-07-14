package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.foodiego.R;
import com.foodiego.databinding.ActivityCheckoutBinding;
import com.foodiego.models.Address;
import com.foodiego.models.CartItem;
import com.foodiego.models.Order;
import com.foodiego.network.Repository;
import com.foodiego.services.MyFirebaseMessagingService;
import com.foodiego.utils.OfflineCacheManager;
import com.foodiego.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity handling checkout processing, billing summary calculations,
 * delivery address retrieval, payment form inputs validation, and order placement.
 */
public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private String userId;
    private Address selectedAddress;
    private List<CartItem> checkoutCartItems = new ArrayList<>();
    private int subtotalPrice = 0;
    private int totalPrice = 0;

    private static final String PREFS_NAME = "FoodieGoAddresses";
    private static final String KEY_ADDR_CACHE = "address_list_cache";

    // Re-load default address if AddressActivity changes address details
    private final ActivityResultLauncher<Intent> addressLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> loadDefaultAddress()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) {
            Toast.makeText(this, "Session invalid!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        loadCartDetails();
        loadDefaultAddress();
    }

    private void setupUI() {
        binding.btnCheckoutBack.setOnClickListener(v -> finish());

        // Address selection trigger
        binding.cardCheckoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, AddressActivity.class);
            addressLauncher.launch(intent);
        });

        // Toggle payment fields visibility
        binding.rgPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCreditCard || checkedId == R.id.rbDebitCard) {
                binding.layoutCardForm.setVisibility(View.VISIBLE);
                binding.layoutUPIForm.setVisibility(View.GONE);
            } else if (checkedId == R.id.rbUPI) {
                binding.layoutCardForm.setVisibility(View.GONE);
                binding.layoutUPIForm.setVisibility(View.VISIBLE);
            } else {
                binding.layoutCardForm.setVisibility(View.GONE);
                binding.layoutUPIForm.setVisibility(View.GONE);
            }
        });

        binding.btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
    }

    private void loadCartDetails() {
        Repository.getInstance().getCart(userId, new Repository.ApiCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> result) {
                if (result != null && !result.isEmpty()) {
                    checkoutCartItems.clear();
                    checkoutCartItems.addAll(result);
                    calculateBillDetails();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Cart empty!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Try cache
                List<CartItem> cached = OfflineCacheManager.getInstance(CheckoutActivity.this).getCachedCart();
                if (cached != null && !cached.isEmpty()) {
                    checkoutCartItems.clear();
                    checkoutCartItems.addAll(cached);
                    calculateBillDetails();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to load cart: " + errorMessage, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void calculateBillDetails() {
        subtotalPrice = 0;
        for (CartItem item : checkoutCartItems) {
            String priceStr = item.getFood().getPrice();
            int itemPrice = 0;
            try {
                itemPrice = Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            subtotalPrice += (itemPrice * item.getQuantity());
        }

        totalPrice = subtotalPrice + 30 + 18;

        binding.txtCheckoutSubtotal.setText("₹" + subtotalPrice);
        binding.txtCheckoutTotal.setText("₹" + totalPrice);
        binding.txtCheckoutPanelTotal.setText("₹" + totalPrice);
    }

    private void loadDefaultAddress() {
        if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
            loadAddressFromCache();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (firestore == null) {
            loadAddressFromCache();
            return;
        }

        firestore.collection("addresses")
                .document(userId)
                .collection("list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        selectedAddress = null;
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Address addr = doc.toObject(Address.class);
                            if (addr != null && addr.isDefault()) {
                                selectedAddress = addr;
                                break;
                            }
                        }
                        // Fallback to first if no default marked
                        if (selectedAddress == null && !task.getResult().isEmpty()) {
                            selectedAddress = task.getResult().getDocuments().get(0).toObject(Address.class);
                        }
                        bindAddress();
                    } else {
                        loadAddressFromCache();
                    }
                });
    }

    private void loadAddressFromCache() {
        String json = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ADDR_CACHE + "_" + userId, null);
        selectedAddress = null;
        if (json != null) {
            Type type = new TypeToken<ArrayList<Address>>() {}.getType();
            List<Address> cached = new Gson().fromJson(json, type);
            if (cached != null && !cached.isEmpty()) {
                for (Address addr : cached) {
                    if (addr.isDefault()) {
                        selectedAddress = addr;
                        break;
                    }
                }
                if (selectedAddress == null) {
                    selectedAddress = cached.get(0);
                }
            }
        }
        bindAddress();
    }

    private void bindAddress() {
        if (selectedAddress != null) {
            binding.txtCheckoutAddressTitle.setText(selectedAddress.getTitle() + " 📍");
            binding.txtCheckoutAddressDetail.setText(selectedAddress.getDetail() + "\nPhone: " + selectedAddress.getPhone());
        } else {
            binding.txtCheckoutAddressTitle.setText("Select Address");
            binding.txtCheckoutAddressDetail.setText("No address selected. Tap here to select or add one.");
        }
    }

    private void validateAndPlaceOrder() {
        if (selectedAddress == null) {
            Toast.makeText(this, "Please select a delivery address!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate payment fields if cards/UPI selected
        int checkedPaymentId = binding.rgPaymentMethods.getCheckedRadioButtonId();
        if (checkedPaymentId == R.id.rbCreditCard || checkedPaymentId == R.id.rbDebitCard) {
            String cardNo = binding.etCardNumber.getText().toString().trim();
            String cardExp = binding.etCardExpiry.getText().toString().trim();
            String cvv = binding.etCardCVV.getText().toString().trim();

            if (cardNo.length() != 16) {
                binding.etCardNumber.setError("Card number must be 16 digits!");
                return;
            }
            if (cardExp.isEmpty() || !cardExp.contains("/")) {
                binding.etCardExpiry.setError("Expiry required (MM/YY)!");
                return;
            }
            if (cvv.length() != 3) {
                binding.etCardCVV.setError("CVV must be 3 digits!");
                return;
            }
        } else if (checkedPaymentId == R.id.rbUPI) {
            String upiId = binding.etUPIId.getText().toString().trim();
            if (upiId.isEmpty() || !upiId.contains("@")) {
                binding.etUPIId.setError("Enter a valid UPI ID (e.g. user@upi)!");
                return;
            }
        }

        // Setup Order Model
        binding.btnPlaceOrder.setEnabled(false);
        binding.btnPlaceOrder.setText("Placing Order...");

        Order order = new Order();
        order.setUserId(userId);
        order.setItems(checkoutCartItems);
        order.setTotalPrice("₹" + totalPrice);
        order.setTimestamp(System.currentTimeMillis());
        order.setStatus("Placed");

        // Set address context onto order
        order.setOrderId(java.util.UUID.randomUUID().toString().substring(0, 8)); // Mock default order ID if backend returns empty

        Repository.getInstance().placeOrder(order, new Repository.ApiCallback<String>() {
            @Override
            public void onSuccess(String orderId) {
                binding.btnPlaceOrder.setEnabled(true);
                binding.btnPlaceOrder.setText("Place Order");

                // Clear offline cart cache
                OfflineCacheManager.getInstance(CheckoutActivity.this).cacheCart(new ArrayList<>());

                // Trigger FCM Push notification locally
                MyFirebaseMessagingService.sendLocalNotification(
                        CheckoutActivity.this,
                        "Order Confirmed 🎉",
                        "Your order has been placed successfully."
                );

                // Open Order Tracking Screen
                Intent intent = new Intent(CheckoutActivity.this, OrderTrackingActivity.class);
                intent.putExtra("order_id", orderId != null ? orderId : order.getOrderId());
                intent.putExtra("order_total", order.getTotalPrice());
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.btnPlaceOrder.setEnabled(true);
                binding.btnPlaceOrder.setText("Place Order");
                // Save Order locally if Node.js server is offline! This prevents user order placement from failing completely!
                saveOrderLocally(order);
            }
        });
    }

    private void saveOrderLocally(Order order) {
        // Fallback: save order to a local offline orders cache
        String prefsKey = "OfflineOrders_" + userId;
        SharedPreferences prefs = getSharedPreferences("FoodieGoOfflineOrders", MODE_PRIVATE);
        String json = prefs.getString(prefsKey, null);
        List<Order> list = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<ArrayList<Order>>() {}.getType();
            List<Order> parsed = new Gson().fromJson(json, type);
            if (parsed != null) list.addAll(parsed);
        }
        list.add(order);
        prefs.edit().putString(prefsKey, new Gson().toJson(list)).apply();

        // Clear offline cart
        OfflineCacheManager.getInstance(this).cacheCart(new ArrayList<>());

        // Notification
        MyFirebaseMessagingService.sendLocalNotification(
                this,
                "Order Confirmed 🎉",
                "Your order has been placed successfully (Saved Offline)."
        );

        Toast.makeText(this, "Order Placed Offline!", Toast.LENGTH_SHORT).show();

        // Open Tracking Activity
        Intent intent = new Intent(CheckoutActivity.this, OrderTrackingActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        intent.putExtra("order_total", order.getTotalPrice());
        startActivity(intent);
        finish();
    }
}
