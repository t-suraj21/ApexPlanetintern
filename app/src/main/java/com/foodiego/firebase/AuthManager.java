package com.foodiego.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private static AuthManager instance;
    private final FirebaseAuth auth;

    private AuthManager() {
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String message);
    }

    public void register(String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(authResult.getUser()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void logout() {
        auth.signOut();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}
