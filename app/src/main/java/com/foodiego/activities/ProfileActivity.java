package com.foodiego.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.databinding.ActivityProfileBinding;
import com.foodiego.models.AuthResponse;
import com.foodiego.models.GenericResponse;
import com.foodiego.models.User;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Screen displaying User Profile settings, supporting photo picking, uploading, and logging out.
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private User currentUser;

    // Modern ActivityResult API for launching system image gallery picking
    private final ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToStorage(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        fetchUserProfile();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                return true;
            }
            
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(ProfileActivity.this, HomeActivity.class);
            } else if (itemId == R.id.nav_cart) {
                intent = new Intent(ProfileActivity.this, CartActivity.class);
            } else if (itemId == R.id.nav_orders) {
                intent = new Intent(ProfileActivity.this, OrdersActivity.class);
            }
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupListeners() {
        // Back Navigation
        binding.btnProfileBack.setOnClickListener(v -> finishAndSlide());

        // Select Photo click
        binding.btnEditPhoto.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

        // Save Details Action
        binding.btnSaveProfile.setOnClickListener(v -> saveProfileDetails());

        // Logout Section
        binding.layoutLogout.setOnClickListener(v -> handleLogout());
    }

    private void fetchUserProfile() {
        // Load details instantly from local SharedPreferences session manager
        currentUser = SessionManager.getInstance(this).getUserDetails();
        if (currentUser == null) {
            Toast.makeText(this, "Sign in to edit your profile!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindUserProfile();
    }

    private void bindUserProfile() {
        binding.etProfileName.setText(currentUser.getName());
        binding.etProfileEmail.setText(currentUser.getEmail());

        if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfileImage())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_foodiego_logo)
                    .error(R.drawable.ic_foodiego_logo)
                    .centerCrop()
                    .into(binding.imgProfile);
        } else {
            binding.imgProfile.setImageResource(R.drawable.ic_profile);
        }
    }

    private void uploadImageToStorage(Uri fileUri) {
        if (currentUser == null) return;

        binding.progressImageUpload.setVisibility(View.VISIBLE);
        binding.btnEditPhoto.setEnabled(false);

        // Copy selected gallery content URI to local cache directory for multipart file packaging
        File tempFile = copyUriToTempFile(fileUri);
        if (tempFile == null) {
            binding.progressImageUpload.setVisibility(View.GONE);
            binding.btnEditPhoto.setEnabled(true);
            Toast.makeText(this, "Failed to resolve image path.", Toast.LENGTH_SHORT).show();
            return;
        }

        Repository.getInstance().uploadAvatar(currentUser.getUserId(), tempFile, new Repository.ApiCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse result) {
                binding.progressImageUpload.setVisibility(View.GONE);
                binding.btnEditPhoto.setEnabled(true);
                if (result != null && result.getProfileImage() != null) {
                    String newAvatarUrl = result.getProfileImage();
                    
                    // Sync avatar url locally in SharedPreferences session
                    SessionManager.getInstance(ProfileActivity.this).updateAvatar(newAvatarUrl);
                    currentUser.setProfileImage(newAvatarUrl);

                    Glide.with(ProfileActivity.this)
                            .load(newAvatarUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(R.drawable.ic_foodiego_logo)
                            .error(R.drawable.ic_foodiego_logo)
                            .centerCrop()
                            .into(binding.imgProfile);

                    Toast.makeText(ProfileActivity.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile record.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.progressImageUpload.setVisibility(View.GONE);
                binding.btnEditPhoto.setEnabled(true);
                Toast.makeText(ProfileActivity.this, "Photo upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Copy Android gallery Content URI to local Cache directory for OkHttp REST package uploads.
     */
    private File copyUriToTempFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            File tempFile = new File(getCacheDir(), "temp_avatar.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveProfileDetails() {
        if (currentUser == null) return;

        String name = binding.etProfileName.getText() != null ? binding.etProfileName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            binding.tilProfileName.setError("Name is required!");
            return;
        }
        if (name.length() < 3) {
            binding.tilProfileName.setError("Name must contain at least 3 characters!");
            return;
        }

        binding.tilProfileName.setError(null);
        binding.layoutProfileLoading.setVisibility(View.VISIBLE);

        Repository.getInstance().updateProfileName(currentUser.getUserId(), name, new Repository.ApiCallback<GenericResponse>() {
            @Override
            public void onSuccess(GenericResponse result) {
                binding.layoutProfileLoading.setVisibility(View.GONE);
                
                // Sync name locally inside SharedPreferences session
                SessionManager.getInstance(ProfileActivity.this).updateName(name);
                currentUser.setName(name);

                Toast.makeText(ProfileActivity.this, "Profile details updated successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.layoutProfileLoading.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Failed to update profile: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        // Clear local SharedPreferences session parameters
        SessionManager.getInstance(this).logoutUser();
        Toast.makeText(this, "Sign Out Successful!", Toast.LENGTH_SHORT).show();

        // Redirect back to Login Gate, clearing current backstack completely
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
}
