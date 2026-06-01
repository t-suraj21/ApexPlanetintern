package com.foodiego.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.foodiego.databinding.ActivityRegisterBinding;

/**
 * Register Screen Activity.
 * Handles account creation, conducts form validations, and redirects users to Home on successful validation.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        // Clear errors proactively as the user types
        binding.etFullName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilFullName.setError(null);
            }
        });

        binding.etRegisterEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilRegisterEmail.setError(null);
            }
        });

        binding.etRegisterPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilRegisterPassword.setError(null);
            }
        });

        binding.etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilConfirmPassword.setError(null);
            }
        });

        // Trigger Validation on Register click
        binding.btnRegister.setOnClickListener(v -> validateAndRegister());

        // Redirect back to Login Screen (simply closes the Register activity, returning to the existing Login activity below it)
        binding.txtLoginLink.setOnClickListener(v -> finish());
    }

    private void validateAndRegister() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etRegisterEmail.getText() != null ? binding.etRegisterEmail.getText().toString().trim() : "";
        String password = binding.etRegisterPassword.getText() != null ? binding.etRegisterPassword.getText().toString().trim() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        boolean isValid = true;

        // Full Name Validation
        if (fullName.isEmpty()) {
            binding.tilFullName.setError("Full name is required!");
            isValid = false;
        } else if (fullName.length() < 3) {
            binding.tilFullName.setError("Name must contain at least 3 characters!");
            isValid = false;
        } else {
            binding.tilFullName.setError(null);
        }

        // Email Validation
        if (email.isEmpty()) {
            binding.tilRegisterEmail.setError("Email address is required!");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegisterEmail.setError("Please enter a valid email address!");
            isValid = false;
        } else {
            binding.tilRegisterEmail.setError(null);
        }

        // Password Validation
        if (password.isEmpty()) {
            binding.tilRegisterPassword.setError("Password is required!");
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilRegisterPassword.setError("Password must contain at least 6 characters!");
            isValid = false;
        } else {
            binding.tilRegisterPassword.setError(null);
        }

        // Confirm Password Validation
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError("Please confirm your password!");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            binding.tilConfirmPassword.setError("Passwords do not match!");
            isValid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        // Action on Successful Validation
        if (isValid) {
            Toast.makeText(this, "Registration Successful! Welcome, " + fullName, Toast.LENGTH_SHORT).show();
            
            // Navigate directly to Home dashboard
            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
            startActivity(intent);
            
            // Clear current stack so that back press exits the app
            finishAffinity();
        }
    }

    /**
     * Helper TextWatcher to shorten boilerplate
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public abstract void onTextChanged(CharSequence s, int start, int before, int count);

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
