package com.foodiego.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.foodiego.databinding.ActivitySplashBinding;
import com.foodiego.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1200);
        binding.logoContainer.startAnimation(fadeIn);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (SessionManager.getInstance(SplashActivity.this).isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}
