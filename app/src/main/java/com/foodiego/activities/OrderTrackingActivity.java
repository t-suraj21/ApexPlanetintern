package com.foodiego.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.foodiego.R;
import com.foodiego.databinding.ActivityOrderTrackingBinding;
import com.foodiego.services.MyFirebaseMessagingService;
import com.foodiego.utils.OfflineCacheManager;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity for displaying real-time Order Tracking timeline.
 * Stepper stages: Placed -> Preparing -> Out for Delivery -> Delivered.
 * Includes status simulator firing corresponding FCM mock system notifications.
 */
public class OrderTrackingActivity extends AppCompatActivity {

    private ActivityOrderTrackingBinding binding;
    private String orderId;
    private String orderTotal;
    private int currentStatusIndex = 0; // 0: Placed, 1: Preparing, 2: Delivery, 3: Delivered

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("order_id");
        orderTotal = getIntent().getStringExtra("order_total");

        setupUI();
        updateStatusUI();
    }

    private void setupUI() {
        binding.btnTrackingBack.setOnClickListener(v -> finish());

        if (orderId != null) {
            binding.txtTrackingOrderId.setText("Order ID: #" + orderId);
        }
        if (orderTotal != null) {
            binding.txtEstimatedTime.setText("Total Payable: " + orderTotal + " | Delivery: 25 mins");
        }

        binding.btnSimulateNext.setOnClickListener(v -> advanceStatus());

        // Default local Lottie search animation fallback (represented by Lottie loading JSON placeholder)
        binding.lottieTracking.setAnimation("loading.json");
    }

    private void advanceStatus() {
        if (currentStatusIndex >= 3) {
            Toast.makeText(this, "Order already delivered!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentStatusIndex++;
        updateStatusUI();

        // Trigger Firebase FCM mock notifications corresponding to the new status
        String title = "";
        String body = "";

        switch (currentStatusIndex) {
            case 1:
                title = "Order Accepted";
                body = "Restaurant accepted your order.";
                updateDatabaseStatus("Preparing");
                break;
            case 2:
                title = "Order Out for Delivery";
                body = "Your order is on the way 🚴";
                updateDatabaseStatus("Out for Delivery");
                break;
            case 3:
                title = "Order Delivered";
                body = "Enjoy your meal 🍕. Thank you for ordering.";
                updateDatabaseStatus("Delivered");
                binding.btnSimulateNext.setEnabled(false);
                binding.btnSimulateNext.setText("Delivered!");
                break;
        }

        if (!title.isEmpty()) {
            MyFirebaseMessagingService.sendLocalNotification(this, title, body);
        }
    }

    private void updateStatusUI() {
        Context context = this;

        // Reset all to default pending
        binding.indicatorPlaced.setBackgroundResource(R.drawable.bg_step_pending);
        binding.indicatorPlaced.setText("1");
        binding.indicatorPlaced.setTextColor(ContextCompat.getColor(context, R.color.textGray));
        binding.txtStepPlaced.setTextColor(ContextCompat.getColor(context, R.color.textGray));

        binding.indicatorPrep.setBackgroundResource(R.drawable.bg_step_pending);
        binding.indicatorPrep.setText("2");
        binding.indicatorPrep.setTextColor(ContextCompat.getColor(context, R.color.textGray));
        binding.txtStepPrep.setTextColor(ContextCompat.getColor(context, R.color.textGray));
        binding.linePlacedToPrep.setBackgroundColor(ContextCompat.getColor(context, R.color.grayBorder));

        binding.indicatorDelivery.setBackgroundResource(R.drawable.bg_step_pending);
        binding.indicatorDelivery.setText("3");
        binding.indicatorDelivery.setTextColor(ContextCompat.getColor(context, R.color.textGray));
        binding.txtStepDelivery.setTextColor(ContextCompat.getColor(context, R.color.textGray));
        binding.linePrepToDelivery.setBackgroundColor(ContextCompat.getColor(context, R.color.grayBorder));

        binding.indicatorDelivered.setBackgroundResource(R.drawable.bg_step_pending);
        binding.indicatorDelivered.setText("4");
        binding.indicatorDelivered.setTextColor(ContextCompat.getColor(context, R.color.textGray));
        binding.txtStepDelivered.setTextColor(ContextCompat.getColor(context, R.color.textGray));
        binding.lineDeliveryToDelivered.setBackgroundColor(ContextCompat.getColor(context, R.color.grayBorder));

        // Mark based on currentStatusIndex
        if (currentStatusIndex >= 0) {
            binding.indicatorPlaced.setBackgroundResource(R.drawable.bg_step_completed);
            binding.indicatorPlaced.setText("✓");
            binding.indicatorPlaced.setTextColor(ContextCompat.getColor(context, R.color.white));
            binding.txtStepPlaced.setTextColor(ContextCompat.getColor(context, R.color.primaryColor));
        }

        if (currentStatusIndex >= 1) {
            binding.linePlacedToPrep.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor));
            binding.indicatorPrep.setBackgroundResource(R.drawable.bg_step_completed);
            binding.indicatorPrep.setText("✓");
            binding.indicatorPrep.setTextColor(ContextCompat.getColor(context, R.color.white));
            binding.txtStepPrep.setTextColor(ContextCompat.getColor(context, R.color.primaryColor));
        }

        if (currentStatusIndex >= 2) {
            binding.linePrepToDelivery.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor));
            binding.indicatorDelivery.setBackgroundResource(R.drawable.bg_step_completed);
            binding.indicatorDelivery.setText("✓");
            binding.indicatorDelivery.setTextColor(ContextCompat.getColor(context, R.color.white));
            binding.txtStepDelivery.setTextColor(ContextCompat.getColor(context, R.color.primaryColor));
        }

        if (currentStatusIndex >= 3) {
            binding.lineDeliveryToDelivered.setBackgroundColor(ContextCompat.getColor(context, R.color.primaryColor));
            binding.indicatorDelivered.setBackgroundResource(R.drawable.bg_step_completed);
            binding.indicatorDelivered.setText("✓");
            binding.indicatorDelivered.setTextColor(ContextCompat.getColor(context, R.color.white));
            binding.txtStepDelivered.setTextColor(ContextCompat.getColor(context, R.color.primaryColor));
        }
    }

    private void updateDatabaseStatus(String status) {
        if (orderId == null || !OfflineCacheManager.getInstance(this).isNetworkAvailable()) return;

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (firestore == null) return;

        firestore.collection("orders")
                .document(orderId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> Logd("Order status synced: " + status))
                .addOnFailureListener(e -> Logd("Order status sync failed: " + e.getMessage()));
    }

    private void Logd(String msg) {
        android.util.Log.d("OrderTracking", msg);
    }
}
