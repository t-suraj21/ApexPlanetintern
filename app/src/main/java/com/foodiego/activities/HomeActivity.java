package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.adapters.CartAdapter;
import com.foodiego.adapters.CategoryAdapter;
import com.foodiego.adapters.OrderAdapter;
import com.foodiego.adapters.PopularFoodAdapter;
import com.foodiego.adapters.RecommendedFoodAdapter;
import com.foodiego.databinding.ActivityHomeBinding;
import com.foodiego.models.CartItem;
import com.foodiego.models.Category;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.models.Order;
import com.foodiego.models.User;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Single Activity for FoodieGo.
 * Contains and manages Home, Cart, Orders, and Profile page modules in a single-page structure.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    // Home variables
    private List<Food> fullFoodList = new ArrayList<>();
    private PopularFoodAdapter popularAdapter;
    private RecommendedFoodAdapter recommendedAdapter;

    // Cart variables
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    // Orders variables
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();

    // Profile variables
    private User currentUser;
    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToBackend(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize modules
        setupHome();
        setupCart();
        setupOrders();
        setupProfile();

        setupBottomNavigation();
        
        // Show Home by default
        switchTab(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
        // Refresh active tab contents
        int selectedTab = binding.bottomNavigation.getSelectedItemId();
        if (selectedTab == R.id.nav_cart) {
            loadCartFromBackend();
        } else if (selectedTab == R.id.nav_orders) {
            fetchOrderHistory();
        } else if (selectedTab == R.id.nav_profile) {
            fetchUserProfile();
        }
    }

    // --- Tab Switching Logic ---
    private void switchTab(int itemId) {
        switchTab(itemId, true);
    }

    private void switchTab(int itemId, boolean updateBottomNav) {
        // Toggle view visibility
        binding.toolbarHome.setVisibility(itemId == R.id.nav_home ? View.VISIBLE : View.GONE);
        binding.scrollHome.setVisibility(itemId == R.id.nav_home ? View.VISIBLE : View.GONE);

        binding.layoutCart.getRoot().setVisibility(itemId == R.id.nav_cart ? View.VISIBLE : View.GONE);
        binding.layoutOrders.getRoot().setVisibility(itemId == R.id.nav_orders ? View.VISIBLE : View.GONE);
        binding.layoutProfile.getRoot().setVisibility(itemId == R.id.nav_profile ? View.VISIBLE : View.GONE);

        // Update selected state of BottomNavigationView safely (no recursion)
        if (updateBottomNav && binding.bottomNavigation.getSelectedItemId() != itemId) {
            binding.bottomNavigation.setSelectedItemId(itemId);
        }

        // Load data on switch
        if (itemId == R.id.nav_home) {
            loadUserProfile();
        } else if (itemId == R.id.nav_cart) {
            loadCartFromBackend();
        } else if (itemId == R.id.nav_orders) {
            fetchOrderHistory();
        } else if (itemId == R.id.nav_profile) {
            fetchUserProfile();
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            switchTab(item.getItemId(), false);
            return true;
        });
    }

    // --- Home Logic ---
    private void setupHome() {
        binding.imgToolbarProfile.setOnClickListener(v -> switchTab(R.id.nav_profile));

        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Pizza", R.drawable.ic_pizza));
        categories.add(new Category("Burger", R.drawable.ic_burger));
        categories.add(new Category("Pasta", R.drawable.ic_pasta));
        categories.add(new Category("Sandwich", R.drawable.ic_sandwich));
        categories.add(new Category("Drinks", R.drawable.ic_drinks));
        categories.add(new Category("Desserts", R.drawable.ic_dessert));

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(new CategoryAdapter(categories, cat -> filterFoods(cat.getName())));

        popularAdapter = new PopularFoodAdapter(new ArrayList<>());
        binding.rvPopularFoods.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularFoods.setAdapter(popularAdapter);

        recommendedAdapter = new RecommendedFoodAdapter(new ArrayList<>());
        binding.rvRecommendedFoods.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecommendedFoods.setAdapter(recommendedAdapter);

        fetchFoodData();
    }

    private void loadUserProfile() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId != null) {
            Repository.getInstance().getUserProfile(userId, new Repository.ApiCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        SessionManager.getInstance(HomeActivity.this).createLoginSession(user);
                        String name = user.getName();
                        String firstName = (name != null && name.contains(" ")) ? name.split(" ")[0] : name;
                        binding.txtGreeting.setText(getString(R.string.greeting_format, firstName));

                        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                            Glide.with(HomeActivity.this)
                                    .load(user.getProfileImage())
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(binding.imgToolbarProfile);
                        }
                    }
                }

                @Override
                public void onFailure(String message) {
                    User localUser = SessionManager.getInstance(HomeActivity.this).getUserDetails();
                    if (localUser != null) {
                        String name = localUser.getName();
                        String firstName = (name != null && name.contains(" ")) ? name.split(" ")[0] : name;
                        binding.txtGreeting.setText(getString(R.string.greeting_format, firstName));

                        if (localUser.getProfileImage() != null && !localUser.getProfileImage().isEmpty()) {
                            Glide.with(HomeActivity.this)
                                    .load(localUser.getProfileImage())
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .circleCrop()
                                    .into(binding.imgToolbarProfile);
                        }
                    }
                }
            });
        }
    }

    private void fetchFoodData() {
        binding.layoutHomeLoading.setVisibility(View.VISIBLE);
        Repository.getInstance().getFoods(new Repository.ApiCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> result) {
                binding.layoutHomeLoading.setVisibility(View.GONE);
                if (result == null || result.isEmpty()) return;

                fullFoodList = result;
                List<Food> popularFoods = new ArrayList<>(result.subList(0, Math.min(5, result.size())));
                popularAdapter = new PopularFoodAdapter(popularFoods);
                binding.rvPopularFoods.setAdapter(popularAdapter);

                recommendedAdapter = new RecommendedFoodAdapter(result);
                binding.rvRecommendedFoods.setAdapter(recommendedAdapter);
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.layoutHomeLoading.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Failed To Load Foods: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterFoods(String categoryName) {
        if (fullFoodList.isEmpty()) return;
        List<Food> filtered = new ArrayList<>();
        for (Food f : fullFoodList) {
            if (f.getName().toLowerCase().contains(categoryName.toLowerCase())) filtered.add(f);
        }
        binding.rvRecommendedFoods.setAdapter(new RecommendedFoodAdapter(filtered.isEmpty() ? fullFoodList : filtered));
    }

    // --- Cart Logic ---
    private void setupCart() {
        binding.layoutCart.btnCartBack.setOnClickListener(v -> switchTab(R.id.nav_home));
        binding.layoutCart.btnCartCheckout.setOnClickListener(v -> handleCheckout());
        applyScaleAnimation(binding.layoutCart.btnCartCheckout);

        cartAdapter = new CartAdapter(cartItems, updatedList -> {
            calculateBill();
            String userId = SessionManager.getInstance(this).getUserId();
            if (userId != null) {
                Repository.getInstance().syncCart(userId, updatedList, new Repository.ApiCallback<GenericResponse>() {
                    @Override
                    public void onSuccess(GenericResponse result) {}
                    @Override
                    public void onFailure(String errorMessage) {}
                });
            }
        });
        binding.layoutCart.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.layoutCart.rvCartItems.setAdapter(cartAdapter);
    }

    private void loadCartFromBackend() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        binding.layoutCart.progressCart.setVisibility(View.VISIBLE);
        binding.layoutCart.scrollCart.setVisibility(View.GONE);
        binding.layoutCart.layoutEmptyCart.setVisibility(View.GONE);

        Repository.getInstance().getCart(userId, new Repository.ApiCallback<List<CartItem>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<CartItem> result) {
                binding.layoutCart.progressCart.setVisibility(View.GONE);
                cartItems.clear();
                if (result != null) {
                    cartItems.addAll(result);
                }
                cartAdapter.notifyDataSetChanged();
                calculateBill();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.layoutCart.progressCart.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Failed to load cart: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateBill() {
        int subtotal = 0;
        for (CartItem item : cartItems) {
            String priceStr = item.getFood().getPrice();
            int itemPrice = 0;
            try {
                itemPrice = java.lang.Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
            subtotal += (itemPrice * item.getQuantity());
        }

        if (subtotal == 0) {
            binding.layoutCart.scrollCart.setVisibility(View.GONE);
            binding.layoutCart.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutCart.btnCartCheckout.setEnabled(false);
        } else {
            binding.layoutCart.scrollCart.setVisibility(View.VISIBLE);
            binding.layoutCart.layoutEmptyCart.setVisibility(View.GONE);
            binding.layoutCart.btnCartCheckout.setEnabled(true);

            binding.layoutCart.txtBillSubtotal.setText("₹" + subtotal);
            binding.layoutCart.txtBillDelivery.setText("₹30");
            binding.layoutCart.txtBillTaxes.setText("₹18");
            binding.layoutCart.txtBillTotal.setText("₹" + (subtotal + 30 + 18));
        }
    }

    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your Cart Is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        binding.layoutCart.btnCartCheckout.setEnabled(false);
        binding.layoutCart.btnCartCheckout.setText("Placing Order...");

        int totalPayable = calculateTotal();

        Order order = new Order();
        order.setUserId(userId);
        order.setItems(cartItems);
        order.setTotalPrice("₹" + totalPayable);
        order.setTimestamp(System.currentTimeMillis());
        order.setStatus("Pending");

        Repository.getInstance().placeOrder(order, new Repository.ApiCallback<String>() {
            @Override
            public void onSuccess(String orderId) {
                binding.layoutCart.btnCartCheckout.setEnabled(true);
                binding.layoutCart.btnCartCheckout.setText("Place Order");
                Toast.makeText(HomeActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();

                // Clear local list and sync UI
                cartItems.clear();
                cartAdapter.notifyDataSetChanged();
                calculateBill();

                // Go to Orders tab on single page
                switchTab(R.id.nav_orders);
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.layoutCart.btnCartCheckout.setEnabled(true);
                binding.layoutCart.btnCartCheckout.setText("Place Order");
                Toast.makeText(HomeActivity.this, "Checkout Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int calculateTotal() {
        int subtotal = 0;
        for (CartItem item : cartItems) {
            String priceStr = item.getFood().getPrice();
            int itemPrice = java.lang.Integer.parseInt(priceStr.replaceAll("[^0-9]", ""));
            subtotal += (itemPrice * item.getQuantity());
        }
        return subtotal + 30 + 18;
    }

    // --- Orders Logic ---
    private void setupOrders() {
        binding.layoutOrders.btnOrdersBack.setOnClickListener(v -> switchTab(R.id.nav_home));

        orderAdapter = new OrderAdapter(orderList);
        binding.layoutOrders.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.layoutOrders.rvOrders.setAdapter(orderAdapter);
    }

    private void fetchOrderHistory() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        binding.layoutOrders.progressOrders.setVisibility(View.VISIBLE);
        binding.layoutOrders.rvOrders.setVisibility(View.GONE);
        binding.layoutOrders.layoutEmptyOrders.setVisibility(View.GONE);

        Repository.getInstance().getUserOrders(userId, new Repository.ApiCallback<List<Order>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Order> result) {
                binding.layoutOrders.progressOrders.setVisibility(View.GONE);
                orderList.clear();
                if (result != null) {
                    orderList.addAll(result);
                }

                if (orderList.isEmpty()) {
                    binding.layoutOrders.layoutEmptyOrders.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutOrders.rvOrders.setVisibility(View.VISIBLE);
                    orderAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String message) {
                binding.layoutOrders.progressOrders.setVisibility(View.GONE);
                binding.layoutOrders.layoutEmptyOrders.setVisibility(View.VISIBLE);
                Toast.makeText(HomeActivity.this, "Error loading orders: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Profile Logic ---
    private void setupProfile() {
        binding.layoutProfile.btnProfileBack.setOnClickListener(v -> switchTab(R.id.nav_home));
        binding.layoutProfile.btnEditPhoto.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
        binding.layoutProfile.btnSaveProfile.setOnClickListener(v -> saveProfileDetails());
        binding.layoutProfile.layoutLogout.setOnClickListener(v -> handleLogout());
    }

    private void fetchUserProfile() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) return;

        binding.layoutProfile.layoutProfileLoading.setVisibility(View.VISIBLE);
        Repository.getInstance().getUserProfile(userId, new Repository.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                currentUser = user;
                bindUserProfile();
            }

            @Override
            public void onFailure(String message) {
                binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                currentUser = SessionManager.getInstance(HomeActivity.this).getUserDetails();
                bindUserProfile();
            }
        });
    }

    private void bindUserProfile() {
        if (currentUser == null) return;
        binding.layoutProfile.etProfileName.setText(currentUser.getName());
        binding.layoutProfile.etProfileEmail.setText(currentUser.getEmail());

        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfileImage())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.layoutProfile.imgProfile);
        }
    }

    private void uploadImageToBackend(Uri fileUri) {
        if (currentUser == null) return;

        binding.layoutProfile.progressImageUpload.setVisibility(View.VISIBLE);
        Repository.getInstance().uploadProfileImage(currentUser.getUserId(), fileUri, this, new Repository.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                binding.layoutProfile.progressImageUpload.setVisibility(View.GONE);
                currentUser = user;
                SessionManager.getInstance(HomeActivity.this).createLoginSession(user);
                bindUserProfile();
                Toast.makeText(HomeActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                binding.layoutProfile.progressImageUpload.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Upload failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileDetails() {
        if (currentUser == null) return;

        String name = binding.layoutProfile.etProfileName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.layoutProfile.tilProfileName.setError("Name is required!");
            return;
        }

        binding.layoutProfile.layoutProfileLoading.setVisibility(View.VISIBLE);
        Repository.getInstance().updateUserProfile(currentUser.getUserId(), name, new Repository.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                currentUser = user;
                SessionManager.getInstance(HomeActivity.this).createLoginSession(user);
                Toast.makeText(HomeActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String message) {
                binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        SessionManager.getInstance(this).logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- Helper Utilities ---
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
