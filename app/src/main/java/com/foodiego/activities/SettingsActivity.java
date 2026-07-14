package com.foodiego.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.foodiego.databinding.ActivitySettingsBinding;
import com.foodiego.firebase.FirebaseHelper;
import com.foodiego.utils.SessionManager;

import java.util.Locale;

/**
 * Settings Activity managing app configuration preferences, locale adjustments,
 * dark theme toggles, push toggles, and user log out.
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences settingsPrefs;

    private static final String PREFS_NAME = "FoodieGoSettings";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_LANGUAGE = "app_language";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settingsPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupUI();
        setupListeners();
    }

    private void setupUI() {
        // Bind state from preferences
        boolean isDarkMode = settingsPrefs.getBoolean(KEY_DARK_MODE, false);
        binding.switchDarkMode.setChecked(isDarkMode);

        boolean isNotifications = settingsPrefs.getBoolean(KEY_NOTIFICATIONS, true);
        binding.switchNotifications.setChecked(isNotifications);

        String lang = settingsPrefs.getString(KEY_LANGUAGE, "en");
        binding.txtLanguageSelected.setText(lang.equalsIgnoreCase("hi") ? "Hindi" : "English");
    }

    private void setupListeners() {
        binding.btnSettingsBack.setOnClickListener(v -> finish());

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications silenced", Toast.LENGTH_SHORT).show();
        });

        binding.btnLanguage.setOnClickListener(v -> showLanguageDialog());

        binding.btnPrivacyPolicy.setOnClickListener(v -> showPrivacyPolicyDialog());

        binding.btnAboutApp.setOnClickListener(v -> showAboutAppDialog());

        binding.btnSettingsLogout.setOnClickListener(v -> handleLogout());
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Hindi"};
        int checkedItem = settingsPrefs.getString(KEY_LANGUAGE, "en").equalsIgnoreCase("hi") ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLang = (which == 1) ? "hi" : "en";
                    settingsPrefs.edit().putString(KEY_LANGUAGE, selectedLang).apply();
                    setLocale(selectedLang);
                    binding.txtLanguageSelected.setText(languages[which]);
                    dialog.dismiss();
                    // Restart HomeActivity to apply language changes
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void showPrivacyPolicyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("We value your privacy. FoodieGo collects details like your name, phone number, address, and profile photo to fulfill orders and provide notifications. All details are kept securely in Firestore and Firebase Storage.\n\nYour data is never shared with third parties.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutAppDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About FoodieGo")
                .setMessage("FoodieGo v1.0.0\n\nA premium food delivery Android application built during Android Development Internship.\n\nFeatures 100% Java + XML structure, Firebase Authentication, Firestore databases, FCM notifications, and offline caching.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void handleLogout() {
        FirebaseHelper.getInstance().logout();
        SessionManager.getInstance(this).logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
