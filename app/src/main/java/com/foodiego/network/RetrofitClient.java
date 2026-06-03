package com.foodiego.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Singleton Retrofit Client initializer pointing to PHP localhost server.
 */
public class RetrofitClient {

    // 10.0.2.2 points to 127.0.0.1 on host machine from inside the Android Emulator
    private static final String BASE_URL = "http://10.0.2.2/foodiego_api/";
    private static Retrofit retrofit = null;

    public static synchronized Retrofit getClient() {
        if (retrofit == null) {
            // Setup logging for debugging network calls
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
