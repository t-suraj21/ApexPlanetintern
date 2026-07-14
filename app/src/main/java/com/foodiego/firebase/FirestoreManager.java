package com.foodiego.firebase;

import com.foodiego.models.CartItem;
import com.foodiego.models.Order;
import com.foodiego.models.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class FirestoreManager {
    private static FirestoreManager instance;
    private final FirebaseFirestore db;

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onFailure(String message);
    }

    // --- User Profile ---
    public void saveUser(User user, FirestoreCallback<Void> callback) {
        db.collection("users").document(user.getUserId()).set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUser(String userId, FirestoreCallback<User> callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.toObject(User.class));
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // --- Cart Management ---
    public void addToCart(String userId, CartItem item, FirestoreCallback<Void> callback) {
        db.collection("cart").document(userId).collection("items").document(item.getFoodId()).set(item)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getCartItems(String userId, FirestoreCallback<List<CartItem>> callback) {
        db.collection("cart").document(userId).collection("items").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.toObjects(CartItem.class));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void removeFromCart(String userId, String foodId, FirestoreCallback<Void> callback) {
        db.collection("cart").document(userId).collection("items").document(foodId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void clearCart(String userId, FirestoreCallback<Void> callback) {
        db.collection("cart").document(userId).collection("items").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // --- Order Management ---
    public void placeOrder(Order order, FirestoreCallback<String> callback) {
        CollectionReference ordersRef = db.collection("orders");
        String orderId = ordersRef.document().getId();
        order.setOrderId(orderId);
        ordersRef.document(orderId).set(order)
                .addOnSuccessListener(aVoid -> callback.onSuccess(orderId))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUserOrders(String userId, FirestoreCallback<List<Order>> callback) {
        db.collection("orders").whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.toObjects(Order.class));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
