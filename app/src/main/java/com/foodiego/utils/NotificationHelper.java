package com.foodiego.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.foodiego.models.NotificationItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility helper to cache and retrieve system and simulated push notifications locally.
 */
public class NotificationHelper {

    private static final String PREF_NAME = "FoodieGoNotifications";
    private static final String KEY_NOTIF_LIST = "notif_list_cache";
    private static final int MAX_NOTIFICATIONS = 30;

    /**
     * Records a new notification locally.
     */
    public static void saveNotification(Context context, String title, String body) {
        if (context == null || title == null || title.trim().isEmpty()) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        List<NotificationItem> list = getNotifications(context);
        
        // Add to front of list (newest first)
        list.add(0, new NotificationItem(title, body, System.currentTimeMillis()));

        // Limit list size to prevent bloating
        if (list.size() > MAX_NOTIFICATIONS) {
            list = list.subList(0, MAX_NOTIFICATIONS);
        }

        String json = new Gson().toJson(list);
        prefs.edit().putString(KEY_NOTIF_LIST, json).apply();
    }

    /**
     * Fetches all saved notifications from local cache.
     */
    public static List<NotificationItem> getNotifications(Context context) {
        if (context == null) return new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_NOTIF_LIST, null);

        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<ArrayList<NotificationItem>>() {}.getType();
            List<NotificationItem> list = new Gson().fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Clears all recorded notifications.
     */
    public static void clearAllNotifications(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_NOTIF_LIST).apply();
    }
}
