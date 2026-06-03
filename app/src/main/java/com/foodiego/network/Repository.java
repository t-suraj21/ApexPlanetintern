package com.foodiego.network;

import android.util.Log;
import androidx.annotation.NonNull;
import com.foodiego.models.AuthResponse;
import com.foodiego.models.CartItem;
import com.foodiego.models.CartResponse;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.models.Order;
import com.foodiego.models.OrderResponse;
import com.foodiego.models.ProductDto;
import com.foodiego.models.ProductResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository pattern manager mediating REST API network queries.
 * Handles authentication, catalog, cart, and order operations with robust error handling.
 */
public class Repository {

    private static final String TAG = "Repository";
    private static Repository instance;
    private final ApiService apiService;

    private Repository() {
        apiService = RetrofitClient.getApiService();
    }

    public static synchronized Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    /**
     * Standardized callback interface for UI communication.
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

    /**
     * Common method to handle network-level failures.
     */
    private void handleNetworkFailure(Throwable t, ApiCallback<?> callback) {
        Log.e(TAG, "Network Error: " + t.getMessage(), t);
        callback.onFailure("No internet connection or server offline");
    }

    // --- Authentication ---

    public void register(String name, String email, String password, ApiCallback<AuthResponse> callback) {
        apiService.register(name, email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    if ("success".equalsIgnoreCase(auth.getStatus())) {
                        callback.onSuccess(auth);
                    } else {
                        callback.onFailure(auth.getMessage() != null ? auth.getMessage() : "Registration failed.");
                    }
                } else {
                    callback.onFailure("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void login(String email, String password, ApiCallback<AuthResponse> callback) {
        apiService.login(email, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    if ("success".equalsIgnoreCase(auth.getStatus())) {
                        callback.onSuccess(auth);
                    } else {
                        callback.onFailure(auth.getMessage() != null ? auth.getMessage() : "Login failed.");
                    }
                } else {
                    callback.onFailure("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    // --- Products catalog ---

    public void getFoods(ApiCallback<List<Food>> callback) {
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Food> foods = new ArrayList<>();
                    if (response.body().products != null) {
                        for (ProductDto dto : response.body().products) {
                            if (dto != null) {
                                foods.add(mapToFood(dto));
                            }
                        }
                    }
                    callback.onSuccess(foods);
                } else {
                    callback.onFailure("Failed to load catalog: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void getFoodById(String foodId, ApiCallback<Food> callback) {
        getFoods(new ApiCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> result) {
                if (result != null) {
                    for (Food food : result) {
                        if (food != null && food.getId() != null && food.getId().equalsIgnoreCase(foodId)) {
                            callback.onSuccess(food);
                            return;
                        }
                    }
                }
                callback.onFailure("Food details not found.");
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    // --- Cart operations ---

    public void getCart(String userId, ApiCallback<List<CartItem>> callback) {
        apiService.getCart(userId).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CartResponse cart = response.body();
                    if ("success".equalsIgnoreCase(cart.getStatus())) {
                        callback.onSuccess(cart.getItems() != null ? cart.getItems() : new ArrayList<>());
                    } else {
                        callback.onFailure(cart.getMessage() != null ? cart.getMessage() : "Failed to fetch cart.");
                    }
                } else {
                    callback.onFailure("Failed to fetch cart: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void syncCart(String userId, List<CartItem> items, ApiCallback<GenericResponse> callback) {
        Map<String, Object> body = new HashMap<>();
        body.put("items", items);

        apiService.syncCart(userId, body).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to sync cart: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    // --- Order checkout ---

    public void placeOrder(String userId, String totalPrice, ApiCallback<GenericResponse> callback) {
        apiService.placeOrder(userId, totalPrice).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse r = response.body();
                    if ("success".equalsIgnoreCase(r.getStatus())) {
                        callback.onSuccess(r);
                    } else {
                        callback.onFailure(r.getMessage() != null ? r.getMessage() : "Checkout failed.");
                    }
                } else {
                    callback.onFailure("Checkout failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void getOrders(String userId, ApiCallback<List<Order>> callback) {
        apiService.getOrders(userId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse r = response.body();
                    if ("success".equalsIgnoreCase(r.getStatus())) {
                        callback.onSuccess(r.getOrders() != null ? r.getOrders() : new ArrayList<>());
                    } else {
                        callback.onFailure(r.getMessage() != null ? r.getMessage() : "Failed to load orders.");
                    }
                } else {
                    callback.onFailure("Failed to load orders: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    // --- Profile management ---

    public void updateProfileName(String userId, String name, ApiCallback<GenericResponse> callback) {
        apiService.updateProfileName(userId, name).enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to update profile name: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void uploadAvatar(String userId, File imageFile, ApiCallback<AuthResponse> callback) {
        if (userId == null || imageFile == null) {
            callback.onFailure("Invalid parameters for upload.");
            return;
        }

        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        apiService.uploadAvatar(userIdBody, body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    if ("success".equalsIgnoreCase(auth.getStatus())) {
                        callback.onSuccess(auth);
                    } else {
                        callback.onFailure(auth.getMessage() != null ? auth.getMessage() : "Upload failed.");
                    }
                } else {
                    callback.onFailure("Upload failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    /**
     * Maps a ProductDto (raw API data) to a Food model (app UI data).
     */
    public Food mapToFood(ProductDto dto) {
        Food food = new Food();
        food.setId(String.valueOf(dto.id));
        food.setName(dto.title != null ? dto.title : "Unknown Food");
        food.setDescription(dto.description != null ? dto.description : "");
        food.setImageUrl(dto.thumbnail);
        
        // Price conversion logic
        int inrPrice = (int) Math.round(dto.price * 15);
        if (inrPrice < 80) inrPrice = 99;
        if (inrPrice > 499) inrPrice = 299;
        food.setPrice("₹" + inrPrice);
        
        double rating = dto.rating;
        if (rating > 5.0 || rating < 0) rating = 4.2;
        food.setRating(String.format(java.util.Locale.US, "%.1f", rating));
        
        int deliveryMinutes = 15 + (dto.id % 25);
        food.setDeliveryTime(deliveryMinutes + " min");
        
        return food;
    }
}
