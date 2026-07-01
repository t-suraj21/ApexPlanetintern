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
import com.foodiego.models.User;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private android.app.AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setupListeners();
        applyScaleAnimation(binding.btnLogin);
    }

    private void setupListeners() {
        binding.etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilEmail.setError(null);
            }
        });

        binding.etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
        });

        binding.btnLogin.setOnClickListener(v -> validateAndLogin());
        binding.txtRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void validateAndLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Valid email is required!");
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError("Password is required!");
            return;
        }

        showLoading("Signing in...");
        Repository.getInstance().loginUser(email, password, new Repository.ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                hideLoading();
                SessionManager.getInstance(LoginActivity.this).createLoginSession(user);
                Toast.makeText(LoginActivity.this, "Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finishAffinity();
            }

            @Override
            public void onFailure(String errorMessage) {
                hideLoading();
                Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
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
