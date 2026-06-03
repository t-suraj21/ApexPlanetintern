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
import com.foodiego.databinding.ActivityRegisterBinding;
import com.foodiego.models.AuthResponse;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

/**
 * Register Screen Activity.
 * Handles account registration validation, button touch animations, and slide transition behaviors.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private android.app.AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        applyScaleAnimation(binding.btnRegister);
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

        // Redirect back to Login Screen with slide animations
        binding.txtLoginLink.setOnClickListener(v -> finishAndSlide());
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
            showLoading("Creating account...");
            Repository.getInstance().register(fullName, email, password, new Repository.ApiCallback<AuthResponse>() {
                @Override
                public void onSuccess(AuthResponse result) {
                    hideLoading();
                    if (result != null && result.getUser() != null) {
                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        
                        // Save login session locally
                        SessionManager.getInstance(RegisterActivity.this).createLoginSession(result.getUser());

                        // Navigate directly to Home dashboard
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        startActivity(intent);
                        
                        // Clear current stack so that back press exits the app
                        finishAffinity();
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed. Stored profile mismatch.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    hideLoading();
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void finishAndSlide() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public abstract void onTextChanged(CharSequence s, int start, int before, int count);

        @Override
        public void afterTextChanged(Editable s) {}
    }
}
