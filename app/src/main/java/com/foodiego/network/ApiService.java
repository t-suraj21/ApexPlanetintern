package com.foodiego.network;

import com.foodiego.models.CartItem;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.models.Order;
import com.foodiego.models.OrderResponse;
import com.foodiego.models.User;

import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Retrofit Interface for FoodieGo custom Node.js/MongoDB API.
 */
public interface ApiService {

    @GET("api/products")
    Call<List<Food>> getProducts();

    @GET("api/products/{id}")
    Call<Food> getProductById(@Path("id") String id);

    @POST("api/auth/register")
    Call<User> registerUser(@Body User user);

    @POST("api/auth/login")
    Call<User> loginUser(@Body User user);

    @GET("api/users/{userId}")
    Call<User> getUserProfile(@Path("userId") String userId);

    @PUT("api/users/{userId}")
    Call<User> updateUserProfile(@Path("userId") String userId, @Body User user);

    @Multipart
    @POST("api/users/{userId}/avatar")
    Call<User> uploadProfileImage(@Path("userId") String userId, @Part MultipartBody.Part image);

    @GET("api/cart/{userId}")
    Call<List<CartItem>> getCartItems(@Path("userId") String userId);

    @POST("api/cart/{userId}")
    Call<GenericResponse> syncCart(@Path("userId") String userId, @Body CartSyncBody body);

    @POST("api/orders")
    Call<OrderResponse> placeOrder(@Body Order order);

    @GET("api/orders/{userId}")
    Call<List<Order>> getUserOrders(@Path("userId") String userId);
}
