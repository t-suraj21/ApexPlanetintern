package com.foodiego.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.foodiego.models.CartItem;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.models.Order;
import com.foodiego.models.OrderResponse;
import com.foodiego.models.User;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository pattern manager mediating REST API network queries.
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

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

    private void handleNetworkFailure(Throwable t, ApiCallback<?> callback) {
        Log.e(TAG, "Network Error: " + t.getMessage(), t);
        callback.onFailure("No internet connection or server offline");
    }

    // --- Products catalog ---

    public void getFoods(final ApiCallback<List<Food>> callback) {
        apiService.getProducts().enqueue(new Callback<List<Food>>() {
            @Override
            public void onResponse(@NonNull Call<List<Food>> call, @NonNull Response<List<Food>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to load catalog: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Food>> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void getFoodById(String foodId, final ApiCallback<Food> callback) {
        apiService.getProductById(foodId).enqueue(new Callback<Food>() {
            @Override
            public void onResponse(@NonNull Call<Food> call, @NonNull Response<Food> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Food details not found.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Food> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    // --- User Auth ---

    public void registerUser(String name, String email, String password, final ApiCallback<User> callback) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        apiService.registerUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Registration failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void loginUser(String email, String password, final ApiCallback<User> callback) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        apiService.loginUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Login failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    // --- User Profile ---

    public void getUserProfile(String userId, final ApiCallback<User> callback) {
        apiService.getUserProfile(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to load profile: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void updateUserProfile(String userId, String name, final ApiCallback<User> callback) {
        User user = new User();
        user.setName(name);

        apiService.updateUserProfile(userId, user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to update profile: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void uploadProfileImage(String userId, Uri fileUri, Context context, final ApiCallback<User> callback) {
        File file = getFileFromUri(context, fileUri);
        if (file == null) {
            callback.onFailure("Failed to resolve image file path");
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        apiService.uploadProfileImage(userId, body).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                try { file.delete(); } catch (Exception ignored) {}
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to upload image: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                try { file.delete(); } catch (Exception ignored) {}
                handleNetworkFailure(t, callback);
            }
        });
    }

    // --- Cart ---

    public void getCart(String userId, final ApiCallback<List<CartItem>> callback) {
        apiService.getCartItems(userId).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<CartItem>> call, @NonNull Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch cart: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CartItem>> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void syncCart(String userId, List<CartItem> cartItems, final ApiCallback<GenericResponse> callback) {
        apiService.syncCart(userId, new CartSyncBody(cartItems)).enqueue(new Callback<GenericResponse>() {
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

    // --- Orders ---

    public void placeOrder(Order order, final ApiCallback<String> callback) {
        apiService.placeOrder(order).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderResponse> call, @NonNull Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getOrderId());
                } else {
                    callback.onFailure("Failed to place order: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    public void getUserOrders(String userId, final ApiCallback<List<Order>> callback) {
        apiService.getUserOrders(userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to load orders: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                handleNetworkFailure(t, callback);
            }
        });
    }

    // --- File Utility ---

    private File getFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            File tempFile = new File(context.getCacheDir(), "temp_avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Error getting file from Uri: " + e.getMessage(), e);
            return null;
        }
    }
}
