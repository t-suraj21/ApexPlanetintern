package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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
import com.foodiego.firebase.FirebaseHelper;
import com.foodiego.models.CartItem;
import com.foodiego.models.Category;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.models.Order;
import com.foodiego.models.User;
import com.foodiego.network.Repository;
import com.foodiego.utils.OfflineCacheManager;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main Dashboard Activity for FoodieGo.
 * Coordinates Home catalogs search & filter tags, Cart items listing & checkout,
 * Orders history, and Profile detail adjustments with offline support.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    // Home variables
    private List<Food> fullFoodList = new ArrayList<>();
    private List<Food> displayedFoodList = new ArrayList<>();
    private PopularFoodAdapter popularAdapter;
    private RecommendedFoodAdapter recommendedAdapter;
    private String selectedCategory = "";

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

    // --- Home Logic (Search & Filtering) ---
    private void setupHome() {
        binding.imgToolbarProfile.setOnClickListener(v -> switchTab(R.id.nav_profile));
        binding.imgToolbarNotification.setOnClickListener(v -> {
            NotificationsBottomSheet sheet = NotificationsBottomSheet.newInstance();
            sheet.show(getSupportFragmentManager(), "NotificationsBottomSheet");
        });

        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Pizza", R.drawable.ic_pizza));
        categories.add(new Category("Burger", R.drawable.ic_burger));
        categories.add(new Category("Pasta", R.drawable.ic_pasta));
        categories.add(new Category("Sandwich", R.drawable.ic_sandwich));
        categories.add(new Category("Drinks", R.drawable.ic_drinks));
        categories.add(new Category("Desserts", R.drawable.ic_dessert));

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(new CategoryAdapter(categories, cat -> {
            if (selectedCategory.equalsIgnoreCase(cat.getName())) {
                selectedCategory = ""; // toggle clear
            } else {
                selectedCategory = cat.getName();
            }
            applyFiltersAndSearch();
        }));

        popularAdapter = new PopularFoodAdapter(new ArrayList<>());
        binding.rvPopularFoods.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularFoods.setAdapter(popularAdapter);

        recommendedAdapter = new RecommendedFoodAdapter(displayedFoodList);
        binding.rvRecommendedFoods.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecommendedFoods.setAdapter(recommendedAdapter);

        // Bind chips listeners
        binding.chipVeg.setOnCheckedChangeListener((buttonView, isChecked) -> applyFiltersAndSearch());
        binding.chipPopular.setOnCheckedChangeListener((buttonView, isChecked) -> applyFiltersAndSearch());
        binding.chipFastDelivery.setOnCheckedChangeListener((buttonView, isChecked) -> applyFiltersAndSearch());
        binding.chipPriceLowHigh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) binding.chipPriceHighLow.setChecked(false);
            applyFiltersAndSearch();
        });
        binding.chipPriceHighLow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) binding.chipPriceLowHigh.setChecked(false);
            applyFiltersAndSearch();
        });
        binding.chipRating.setOnCheckedChangeListener((buttonView, isChecked) -> applyFiltersAndSearch());
        binding.chipNewest.setOnCheckedChangeListener((buttonView, isChecked) -> applyFiltersAndSearch());

        // Search text watcher
        binding.etSearchFoods.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

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
                        OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(user);
                        updateGreetingAndProfileIcon(user);
                    }
                }

                @Override
                public void onFailure(String message) {
                    User cached = OfflineCacheManager.getInstance(HomeActivity.this).getCachedProfile();
                    if (cached != null) {
                        updateGreetingAndProfileIcon(cached);
                    }
                }
            });
        }
    }

    private void updateGreetingAndProfileIcon(User user) {
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

    private void fetchFoodData() {
        binding.layoutHomeLoading.setVisibility(View.VISIBLE);

        if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
            binding.layoutHomeLoading.setVisibility(View.GONE);
            List<Food> cached = OfflineCacheManager.getInstance(this).getCachedFoods();
            if (cached != null && !cached.isEmpty()) {
                fullFoodList.clear();
                fullFoodList.addAll(cached);
                applyFiltersAndSearch();
                Toast.makeText(this, "Offline: loaded cached menu", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No internet and no menu cached", Toast.LENGTH_LONG).show();
            }
            return;
        }

        Repository.getInstance().getFoods(new Repository.ApiCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> result) {
                binding.layoutHomeLoading.setVisibility(View.GONE);
                if (result == null || result.isEmpty()) return;

                fullFoodList = result;
                // Cache locally
                OfflineCacheManager.getInstance(HomeActivity.this).cacheFoods(result);
                applyFiltersAndSearch();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.layoutHomeLoading.setVisibility(View.GONE);
                // Fallback to cache
                List<Food> cached = OfflineCacheManager.getInstance(HomeActivity.this).getCachedFoods();
                if (cached != null && !cached.isEmpty()) {
                    fullFoodList.clear();
                    fullFoodList.addAll(cached);
                    applyFiltersAndSearch();
                }
                Toast.makeText(HomeActivity.this, "Loaded from Offline Cache", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFiltersAndSearch() {
        if (fullFoodList == null || fullFoodList.isEmpty()) return;

        List<Food> filtered = new ArrayList<>();
        String query = binding.etSearchFoods.getText().toString().trim().toLowerCase();

        // 1. Text Search & Category Filter
        for (Food f : fullFoodList) {
            boolean matchesSearch = query.isEmpty() ||
                    f.getName().toLowerCase().contains(query) ||
                    f.getDescription().toLowerCase().contains(query) ||
                    f.getCategory().toLowerCase().contains(query) ||
                    f.getRestaurant().toLowerCase().contains(query);

            boolean matchesCategory = selectedCategory.isEmpty() ||
                    f.getCategory().equalsIgnoreCase(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filtered.add(f);
            }
        }

        // 2. Chip Filters
        // Veg Chip
        if (binding.chipVeg.isChecked()) {
            List<Food> vegList = new ArrayList<>();
            for (Food f : filtered) {
                if (f.isVeg()) vegList.add(f);
            }
            filtered = vegList;
        }

        // Popular Chip
        if (binding.chipPopular.isChecked()) {
            List<Food> popularList = new ArrayList<>();
            for (Food f : filtered) {
                if (f.isPopular()) popularList.add(f);
            }
            filtered = popularList;
        }

        // Fast Delivery Chip (< 25 min)
        if (binding.chipFastDelivery.isChecked()) {
            List<Food> fastList = new ArrayList<>();
            for (Food f : filtered) {
                int mins = 30;
                try {
                    mins = Integer.parseInt(f.getDeliveryTime().replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {}
                if (mins <= 20) fastList.add(f);
            }
            filtered = fastList;
        }

        // Rating Chip (>= 4.5)
        if (binding.chipRating.isChecked()) {
            List<Food> ratedList = new ArrayList<>();
            for (Food f : filtered) {
                double rating = 4.0;
                try {
                    rating = Double.parseDouble(f.getRating());
                } catch (Exception ignored) {}
                if (rating >= 4.5) ratedList.add(f);
            }
            filtered = ratedList;
        }

        // 3. Sorting Filters
        if (binding.chipPriceLowHigh.isChecked()) {
            Collections.sort(filtered, (o1, o2) -> {
                int p1 = Integer.parseInt(o1.getPrice().replaceAll("[^0-9]", ""));
                int p2 = Integer.parseInt(o2.getPrice().replaceAll("[^0-9]", ""));
                return Integer.compare(p1, p2);
            });
        } else if (binding.chipPriceHighLow.isChecked()) {
            Collections.sort(filtered, (o1, o2) -> {
                int p1 = Integer.parseInt(o1.getPrice().replaceAll("[^0-9]", ""));
                int p2 = Integer.parseInt(o2.getPrice().replaceAll("[^0-9]", ""));
                return Integer.compare(p2, p1);
            });
        }

        if (binding.chipNewest.isChecked()) {
            Collections.sort(filtered, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
        }

        // Update popular recycler items
        List<Food> popularItems = new ArrayList<>();
        for (Food f : filtered) {
            if (f.isPopular()) popularItems.add(f);
        }
        if (popularItems.isEmpty() && filtered.size() > 0) {
            popularItems.addAll(filtered.subList(0, Math.min(3, filtered.size())));
        }
        popularAdapter = new PopularFoodAdapter(popularItems);
        binding.rvPopularFoods.setAdapter(popularAdapter);

        // Update recommended list
        displayedFoodList.clear();
        displayedFoodList.addAll(filtered);
        recommendedAdapter.notifyDataSetChanged();

        // Handle Empty Search layout Lottie status
        if (displayedFoodList.isEmpty()) {
            binding.layoutEmptySearch.setVisibility(View.VISIBLE);
            binding.rvRecommendedFoods.setVisibility(View.GONE);
            binding.lottieEmptySearch.setAnimation("search_empty.json");
            binding.lottieEmptySearch.playAnimation();
        } else {
            binding.layoutEmptySearch.setVisibility(View.GONE);
            binding.rvRecommendedFoods.setVisibility(View.VISIBLE);
        }
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
                // Sync cart back to offline cache
                OfflineCacheManager.getInstance(this).cacheCart(updatedList);

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

        binding.layoutCart.lottieEmptyCart.setAnimation("empty_cart.json");
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
                    OfflineCacheManager.getInstance(HomeActivity.this).cacheCart(result);
                }
                cartAdapter.notifyDataSetChanged();
                calculateBill();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.layoutCart.progressCart.setVisibility(View.GONE);
                // Fallback to cache
                List<CartItem> cached = OfflineCacheManager.getInstance(HomeActivity.this).getCachedCart();
                cartItems.clear();
                if (cached != null) {
                    cartItems.addAll(cached);
                }
                cartAdapter.notifyDataSetChanged();
                calculateBill();
                Toast.makeText(HomeActivity.this, "Offline cart loaded", Toast.LENGTH_SHORT).show();
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
            binding.layoutCart.lottieEmptyCart.playAnimation();
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

        // Open checkout screen
        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }

    // --- Orders Logic ---
    private void setupOrders() {
        binding.layoutOrders.btnOrdersBack.setOnClickListener(v -> switchTab(R.id.nav_home));

        orderAdapter = new OrderAdapter(orderList);
        binding.layoutOrders.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.layoutOrders.rvOrders.setAdapter(orderAdapter);

        binding.layoutOrders.lottieEmptyOrders.setAnimation("no_orders.json");
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
                    binding.layoutOrders.lottieEmptyOrders.playAnimation();
                } else {
                    binding.layoutOrders.rvOrders.setVisibility(View.VISIBLE);
                    orderAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String message) {
                binding.layoutOrders.progressOrders.setVisibility(View.GONE);
                // Fallback to local offline orders
                SharedPreferences prefs = getSharedPreferences("FoodieGoOfflineOrders", MODE_PRIVATE);
                String prefsKey = "OfflineOrders_" + userId;
                String json = prefs.getString(prefsKey, null);
                orderList.clear();
                if (json != null) {
                    Type type = new TypeToken<ArrayList<Order>>() {}.getType();
                    List<Order> parsed = new com.google.gson.Gson().fromJson(json, type);
                    if (parsed != null) orderList.addAll(parsed);
                }

                if (orderList.isEmpty()) {
                    binding.layoutOrders.layoutEmptyOrders.setVisibility(View.VISIBLE);
                    binding.layoutOrders.lottieEmptyOrders.playAnimation();
                } else {
                    binding.layoutOrders.rvOrders.setVisibility(View.VISIBLE);
                    orderAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // --- Profile Logic ---
    private void setupProfile() {
        binding.layoutProfile.btnProfileBack.setOnClickListener(v -> switchTab(R.id.nav_home));
        binding.layoutProfile.btnEditPhoto.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
        binding.layoutProfile.btnSaveProfile.setOnClickListener(v -> saveProfileDetails());
        binding.layoutProfile.layoutLogout.setOnClickListener(v -> handleLogout());

        binding.layoutProfile.cardProfileAddresses.setOnClickListener(v -> startActivity(new Intent(this, AddressActivity.class)));
        binding.layoutProfile.cardProfileFavorites.setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        binding.layoutProfile.cardProfileSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
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
                User cached = OfflineCacheManager.getInstance(HomeActivity.this).getCachedProfile();
                if (cached != null) {
                    currentUser = cached;
                }
                bindUserProfile();
            }
        });
    }

    private void bindUserProfile() {
        if (currentUser == null) return;
        binding.layoutProfile.etProfileName.setText(currentUser.getName());
        binding.layoutProfile.etProfileEmail.setText(currentUser.getEmail());
        binding.layoutProfile.etProfilePhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");

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

        if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
            binding.layoutProfile.progressImageUpload.setVisibility(View.GONE);
            // Save locally
            currentUser.setProfileImage(fileUri.toString());
            SessionManager.getInstance(HomeActivity.this).createLoginSession(currentUser);
            OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(currentUser);
            bindUserProfile();
            Toast.makeText(this, "Profile image updated locally (Offline)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try Firebase Storage upload first
        FirebaseHelper.getInstance().uploadProfileImage(currentUser.getUserId(), fileUri, new FirebaseHelper.FirebaseCallback<String>() {
            @Override
            public void onSuccess(String downloadUrl) {
                // Save download URL to user profile
                currentUser.setProfileImage(downloadUrl);
                saveProfileDetails();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Fallback: upload to Node.js server
                Repository.getInstance().uploadProfileImage(currentUser.getUserId(), fileUri, HomeActivity.this, new Repository.ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        binding.layoutProfile.progressImageUpload.setVisibility(View.GONE);
                        currentUser = user;
                        SessionManager.getInstance(HomeActivity.this).createLoginSession(user);
                        OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(user);
                        bindUserProfile();
                        Toast.makeText(HomeActivity.this, "Profile picture updated via REST!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        binding.layoutProfile.progressImageUpload.setVisibility(View.GONE);
                        // Offline save fallback
                        currentUser.setProfileImage(fileUri.toString());
                        SessionManager.getInstance(HomeActivity.this).createLoginSession(currentUser);
                        OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(currentUser);
                        bindUserProfile();
                        Toast.makeText(HomeActivity.this, "Saved image offline: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void saveProfileDetails() {
        if (currentUser == null) return;

        String name = binding.layoutProfile.etProfileName.getText().toString().trim();
        String phone = binding.layoutProfile.etProfilePhone.getText().toString().trim();

        if (name.isEmpty()) {
            binding.layoutProfile.tilProfileName.setError("Name is required!");
            return;
        }

        binding.layoutProfile.layoutProfileLoading.setVisibility(View.VISIBLE);

        currentUser.setName(name);
        currentUser.setPhone(phone);

        // Update REST API & Firestore
        Repository.getInstance().updateUserProfile(currentUser.getUserId(), name, new Repository.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                user.setPhone(phone);
                FirebaseHelper.getInstance().saveUserProfile(user, new FirebaseHelper.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                        currentUser = user;
                        SessionManager.getInstance(HomeActivity.this).createLoginSession(user);
                        OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(user);
                        Toast.makeText(HomeActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                        currentUser = user;
                        SessionManager.getInstance(HomeActivity.this).createLoginSession(user);
                        OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(user);
                        Toast.makeText(HomeActivity.this, "Profile updated locally & REST!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                // Try Firestore direct
                FirebaseHelper.getInstance().saveUserProfile(currentUser, new FirebaseHelper.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                        SessionManager.getInstance(HomeActivity.this).createLoginSession(currentUser);
                        OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(currentUser);
                        Toast.makeText(HomeActivity.this, "Profile updated on Firestore!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        binding.layoutProfile.layoutProfileLoading.setVisibility(View.GONE);
                        SessionManager.getInstance(HomeActivity.this).createLoginSession(currentUser);
                        OfflineCacheManager.getInstance(HomeActivity.this).cacheProfile(currentUser);
                        Toast.makeText(HomeActivity.this, "Saved locally (Offline Mode)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void handleLogout() {
        FirebaseHelper.getInstance().logout();
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
