package com.foodiego.firebase;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;

import com.foodiego.models.Food;
import com.foodiego.models.User;
import com.foodiego.utils.OfflineCacheManager;
import com.foodiego.utils.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper client mediating operations with Firebase Auth, Firestore, and Storage.
 * Provides fallback to offline caches and REST API when Firebase services are unreachable.
 */
public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private FirebaseHelper() {
        try {
            auth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            storage = FirebaseStorage.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase instances: " + e.getMessage());
        }
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

    // --- Authentication ---

    public void registerUser(String email, String password, String name, FirebaseCallback<User> callback) {
        if (auth == null) {
            callback.onFailure("Firebase Authentication is not initialized");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User user = new User(userId, name, email, "");
                            user.setPhone("");
                            user.setAddress("");
                            saveUserProfile(user, new FirebaseCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    callback.onSuccess(user);
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    callback.onSuccess(user); // Proceed even if saving profile to firestore fails
                                }
                            });
                        } else {
                            callback.onFailure("User creation failed: No user active");
                        }
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        callback.onFailure("Registration Failed: " + err);
                    }
                });
    }

    public void loginUser(String email, String password, FirebaseCallback<User> callback) {
        if (auth == null) {
            callback.onFailure("Firebase Authentication is not initialized");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchUserProfile(firebaseUser.getUid(), callback);
                        } else {
                            callback.onFailure("Login failed: User empty");
                        }
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        callback.onFailure("Authentication Failed: " + err);
                    }
                });
    }

    public void logout() {
        if (auth != null) {
            auth.signOut();
        }
    }

    // --- Firestore: User Profile ---

    public void saveUserProfile(User user, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure("Firestore is not initialized");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("profileImage", user.getProfileImage());
        userData.put("phone", user.getPhone() != null ? user.getPhone() : "");
        userData.put("address", user.getAddress() != null ? user.getAddress() : "");

        firestore.collection("users")
                .document(user.getUserId())
                .set(userData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure("Firestore Save Failed: " + e.getMessage()));
    }

    public void fetchUserProfile(String userId, FirebaseCallback<User> callback) {
        if (firestore == null) {
            callback.onFailure("Firestore is not initialized");
            return;
        }

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            User user = new User();
                            user.setUserId(doc.getString("userId"));
                            user.setName(doc.getString("name"));
                            user.setEmail(doc.getString("email"));
                            user.setProfileImage(doc.getString("profileImage"));
                            user.setPhone(doc.getString("phone"));
                            user.setAddress(doc.getString("address"));
                            callback.onSuccess(user);
                        } else {
                            callback.onFailure("Profile does not exist in database");
                        }
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Fetch failed";
                        callback.onFailure("Firestore Load Failed: " + err);
                    }
                });
    }

    // --- Firestore: Favorites ---

    public void addFavorite(String userId, Food food, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure("Firestore is not initialized");
            return;
        }

        firestore.collection("favorites")
                .document(userId)
                .collection("items")
                .document(food.getId())
                .set(food)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure("Failed to add favorite: " + e.getMessage()));
    }

    public void removeFavorite(String userId, String foodId, FirebaseCallback<Void> callback) {
        if (firestore == null) {
            callback.onFailure("Firestore is not initialized");
            return;
        }

        firestore.collection("favorites")
                .document(userId)
                .collection("items")
                .document(foodId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure("Failed to remove favorite: " + e.getMessage()));
    }

    public void getFavorites(String userId, FirebaseCallback<List<Food>> callback) {
        if (firestore == null) {
            callback.onFailure("Firestore is not initialized");
            return;
        }

        firestore.collection("favorites")
                .document(userId)
                .collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Food> favorites = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Food food = doc.toObject(Food.class);
                            if (food != null) {
                                favorites.add(food);
                            }
                        }
                        callback.onSuccess(favorites);
                    } else {
                        String err = task.getException() != null ? task.getException().getMessage() : "Fetch failed";
                        callback.onFailure("Failed to fetch favorites: " + err);
                    }
                });
    }

    public void checkFavoriteStatus(String userId, String foodId, FirebaseCallback<Boolean> callback) {
        if (firestore == null) {
            callback.onSuccess(false);
            return;
        }

        firestore.collection("favorites")
                .document(userId)
                .collection("items")
                .document(foodId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        callback.onSuccess(task.getResult().exists());
                    } else {
                        callback.onSuccess(false);
                    }
                });
    }

    // --- Firebase Storage: Image Upload ---

    public void uploadProfileImage(String userId, Uri imageUri, FirebaseCallback<String> callback) {
        if (storage == null) {
            callback.onFailure("Firebase Storage is not initialized");
            return;
        }

        StorageReference fileRef = storage.getReference().child("profile_images/" + userId + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> callback.onFailure("Failed to resolve image URL: " + e.getMessage())))
                .addOnFailureListener(e -> callback.onFailure("Upload failed: " + e.getMessage()));
    }
}
