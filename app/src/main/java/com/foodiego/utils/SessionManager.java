package com.foodiego.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.foodiego.models.User;

/**
 * Helper to manage persistent user login sessions via SharedPreferences.
 */
public class SessionManager {

    private static final String PREF_NAME = "FoodieGoPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_AVATAR = "userAvatar";

    private static SessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /**
     * Initializes user session parameters.
     */
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_AVATAR, user.getProfileImage());
        editor.commit();
    }

    /**
     * Verifies active session presence.
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Retrieves stored user session profile data.
     */
    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }
        return new User(
                pref.getString(KEY_USER_ID, ""),
                pref.getString(KEY_USER_NAME, ""),
                pref.getString(KEY_USER_EMAIL, ""),
                pref.getString(KEY_USER_AVATAR, "")
        );
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public void updateName(String newName) {
        editor.putString(KEY_USER_NAME, newName);
        editor.commit();
    }

    public void updateAvatar(String newAvatarUrl) {
        editor.putString(KEY_USER_AVATAR, newAvatarUrl);
        editor.commit();
    }

    /**
     * Clears all session parameters upon logout.
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
