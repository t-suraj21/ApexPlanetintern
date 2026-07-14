package com.foodiego.models;

import java.io.Serializable;

/**
 * Data Model representing a single user notification message.
 */
public class NotificationItem implements Serializable {
    private String title;
    private String body;
    private long timestamp;

    public NotificationItem() {
    }

    public NotificationItem(String title, String body, long timestamp) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
