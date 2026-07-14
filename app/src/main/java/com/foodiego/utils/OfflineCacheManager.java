package com.foodiego.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.foodiego.models.Food;
import com.foodiego.models.Category;
import com.foodiego.models.CartItem;
import com.foodiego.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager responsible for offline caching of application datasets.
 * serializes objects/lists to SharedPreferences as JSON.
 */
public class OfflineCacheManager {

    private static final String PREF_NAME = "FoodieGoCachePrefs";
    private static final String KEY_FOOD_LIST = "cache_food_list";
    private static final String KEY_CATEGORIES = "cache_categories";
    private static final String KEY_CART = "cache_cart";
    private static final String KEY_USER_PROFILE = "cache_user_profile";

    private static OfflineCacheManager instance;
    private final Context context;
    private final Gson gson;

    private OfflineCacheManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new Gson();
    }

    public static synchronized OfflineCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineCacheManager(context);
        }
        return instance;
    }

    /**
     * Checks if the device has an active network connection.
     */
    public boolean isNetworkAvailable() {
        // Safe check for emulators: allow local network traffic to 10.0.2.2
        if (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")) {
            return true;
        }
        String fingerprint = android.os.Build.FINGERPRINT;
        if (fingerprint != null && (fingerprint.startsWith("unknown") || fingerprint.contains("generic") || fingerprint.contains("emulator"))) {
            return true;
        }
        
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return true; // Fallback to try API calls anyway
            android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) return true;
            return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork == null || activeNetwork.isConnectedOrConnecting();
        }
    }

    // --- Food Catalog Caching ---

    public void cacheFoods(List<Food> foods) {
        if (foods == null) return;
        String json = gson.toJson(foods);
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FOOD_LIST, json)
                .apply();
    }

    public List<Food> getCachedFoods() {
        String json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_FOOD_LIST, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Food>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // --- Category Caching ---

    public void cacheCategories(List<Category> categories) {
        if (categories == null) return;
        String json = gson.toJson(categories);
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CATEGORIES, json)
                .apply();
    }

    public List<Category> getCachedCategories() {
        String json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CATEGORIES, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<Category>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // --- Cart Caching ---

    public void cacheCart(List<CartItem> cartItems) {
        if (cartItems == null) return;
        String json = gson.toJson(cartItems);
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CART, json)
                .apply();
    }

    public List<CartItem> getCachedCart() {
        String json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CART, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<CartItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // --- Profile Caching ---

    public void cacheProfile(User user) {
        if (user == null) return;
        String json = gson.toJson(user);
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USER_PROFILE, json)
                .apply();
    }

    public User getCachedProfile() {
        String json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_PROFILE, null);
        if (json == null) return null;
        return gson.fromJson(json, User.class);
    }

    public void clearCache() {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }
}
