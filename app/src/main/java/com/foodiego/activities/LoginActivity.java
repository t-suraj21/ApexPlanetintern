package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.foodiego.R;
import com.foodiego.databinding.ActivityLoginBinding;
import com.foodiego.models.AuthResponse;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

/**
 * Login Screen Activity.
 * Handles form validation, interactive button touch animations, and slide screen transitions.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private android.app.AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        applyScaleAnimation(binding.btnLogin);
    }

    private void setupListeners() {
        // Clear errors automatically as user starts typing
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Trigger validations on Login Button click
        binding.btnLogin.setOnClickListener(v -> validateAndLogin());

        // Redirect to Register Screen with custom slide transitions
        binding.txtRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Forgot Password Mock Action
        binding.txtForgotPassword.setOnClickListener(v -> 
            Toast.makeText(this, "Reset Password link has been sent to your email!", Toast.LENGTH_SHORT).show()
        );
    }

    private void validateAndLogin() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        boolean isValid = true;

        // Email Validation
        if (email.isEmpty()) {
            binding.tilEmail.setError("Email address is required!");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Please enter a valid email address!");
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Password Validation
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required!");
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Password must contain at least 6 characters!");
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }

        // Action on Successful Validation
        if (isValid) {
            showLoading("Signing in...");
            Repository.getInstance().login(email, password, new Repository.ApiCallback<AuthResponse>() {
                @Override
                public void onSuccess(AuthResponse result) {
                    hideLoading();
                    if (result != null && result.getUser() != null) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        
                        // Save login session locally
                        SessionManager.getInstance(LoginActivity.this).createLoginSession(result.getUser());

                        // Navigate to Home dashboard
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        
                        // Clear current login from backstack so back press closes the app
                        finishAffinity();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed. User detail missing.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    hideLoading();
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showLoading(String message) {
        if (progressDialog == null) {
            android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
            progressBar.setPadding(40, 40, 40, 40);
            progressDialog = new android.app.AlertDialog.Builder(this)
                    .setView(progressBar)
                    .setMessage(message)
                    .setCancelable(false)
                    .create();
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Programmatic touch-scale micro-animation for buttons.
     */
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
