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
import com.foodiego.models.User;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

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

        binding.btnRegister.setOnClickListener(v -> validateAndRegister());
        binding.txtLoginLink.setOnClickListener(v -> finishAndSlide());
    }

    private void validateAndRegister() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etRegisterEmail.getText().toString().trim();
        String password = binding.etRegisterPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            binding.tilFullName.setError("Full name is required!");
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegisterEmail.setError("Valid email is required!");
            return;
        }
        if (password.length() < 6) {
            binding.tilRegisterPassword.setError("Password must be at least 6 characters!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match!");
            return;
        }

        showLoading("Creating account...");
        Repository.getInstance().registerUser(fullName, email, password, new Repository.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                hideLoading();
                SessionManager.getInstance(RegisterActivity.this).createLoginSession(user);
                Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                finishAffinity();
            }

            @Override
            public void onFailure(String errorMessage) {
                hideLoading();
                Toast.makeText(RegisterActivity.this, "Registration failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
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

    private void finishAndSlide() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

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
