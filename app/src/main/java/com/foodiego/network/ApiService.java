package com.foodiego.network;

import com.foodiego.models.AuthResponse;
import com.foodiego.models.CartResponse;
import com.foodiego.models.GenericResponse;
import com.foodiego.models.OrderResponse;
import com.foodiego.models.ProductResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Retrofit Interface defining server-side PHP API Endpoints.
 */
public interface ApiService {

    @FormUrlEncoded
    @POST("register.php")
    Call<AuthResponse> register(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<AuthResponse> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @GET("get_products.php")
    Call<ProductResponse> getProducts();

    @GET("cart.php")
    Call<CartResponse> getCart(
            @Query("user_id") String userId
    );

    @POST("cart.php")
    Call<GenericResponse> syncCart(
            @Query("user_id") String userId,
            @Body Map<String, Object> body
    );

    @FormUrlEncoded
    @POST("place_order.php")
    Call<GenericResponse> placeOrder(
            @Field("user_id") String userId,
            @Field("total_price") String totalPrice
    );

    @GET("get_orders.php")
    Call<OrderResponse> getOrders(
            @Query("user_id") String userId
    );

    @FormUrlEncoded
    @POST("profile.php")
    Call<GenericResponse> updateProfileName(
            @Field("user_id") String userId,
            @Field("name") String name
    );

    @Multipart
    @POST("upload_avatar.php")
    Call<AuthResponse> uploadAvatar(
            @Part("user_id") RequestBody userId,
            @Part MultipartBody.Part image
    );
}
