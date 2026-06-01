package com.foodiego.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.foodiego.R;
import com.foodiego.databinding.ActivitySplashBinding;

/**
 * Splash Screen Activity.
 * Displays application logo and tagline, then automatically transitions to LoginActivity after a 2-second delay.
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DELAY = 2000; // 2 seconds delay


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Dynamic visual premium look: load standard fade-in animation on logo and brand texts
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1200);
        binding.logoContainer.startAnimation(fadeIn);

        // Transition timer handler
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finish current activity so the user cannot navigate back to Splash
        }, SPLASH_DELAY);
    }
}
