package com.foodiego.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.foodiego.databinding.ActivityLoginBinding;

/**
 * Login Screen Activity.
 * Allows users to input their credentials, handles input validations, and navigates to Register or Home dashboards.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
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

        // Redirect to Register Screen
        binding.txtRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
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
            Toast.makeText(this, "Login Successful! Welcome, " + email.split("@")[0], Toast.LENGTH_SHORT).show();
            
            // Navigate to Home dashboard
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            
            // Clear current login from backstack so back press closes the app
            finishAffinity();
        }
    }
}
