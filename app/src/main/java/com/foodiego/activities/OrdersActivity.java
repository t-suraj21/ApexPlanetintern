package com.foodiego.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.foodiego.R;
import com.foodiego.adapters.OrderAdapter;
import com.foodiego.databinding.ActivityOrdersBinding;
import com.foodiego.models.Order;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Screen displaying User's Order History records dynamically.
 */
public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderList = new ArrayList<>();

        setupListeners();
        setupRecyclerView();
        fetchOrderHistory();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_orders);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_orders) {
                return true;
            }
            
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(OrdersActivity.this, HomeActivity.class);
            } else if (itemId == R.id.nav_cart) {
                intent = new Intent(OrdersActivity.this, CartActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(OrdersActivity.this, ProfileActivity.class);
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
        // Back chevron action
        binding.btnOrdersBack.setOnClickListener(v -> finishAndSlide());
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(orderList);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(orderAdapter);
    }

    private void fetchOrderHistory() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) {
            binding.progressOrders.setVisibility(View.GONE);
            Toast.makeText(this, "Please sign in to view orders!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressOrders.setVisibility(View.VISIBLE);
        binding.rvOrders.setVisibility(View.GONE);
        binding.layoutEmptyOrders.setVisibility(View.GONE);

        Repository.getInstance().getOrders(userId, new Repository.ApiCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> result) {
                binding.progressOrders.setVisibility(View.GONE);
                orderList.clear();
                if (result != null) {
                    orderList.addAll(result);
                }

                // Sort orders locally in memory: newer orders on top
                Collections.sort(orderList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                if (orderList.isEmpty()) {
                    binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
                    binding.rvOrders.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyOrders.setVisibility(View.GONE);
                    binding.rvOrders.setVisibility(View.VISIBLE);
                    orderAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.progressOrders.setVisibility(View.GONE);
                binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
                Toast.makeText(OrdersActivity.this, "Failed to load orders: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
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
