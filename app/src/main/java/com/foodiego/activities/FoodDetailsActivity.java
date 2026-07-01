package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.databinding.ActivityFoodDetailsBinding;
import com.foodiego.models.CartItem;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

/**
 * Food Details Screen Activity.
 * Integrated with REST backend for Cart management.
 */
public class FoodDetailsActivity extends AppCompatActivity {

    private ActivityFoodDetailsBinding binding;
    private Food food;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoodDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        food = (Food) getIntent().getSerializableExtra("food_item");

        if (food != null) {
            bindFoodDetails();
            setupListeners();
            applyScaleAnimation(binding.btnDetailsAddToCart);
        } else {
            Toast.makeText(this, "Food details not found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindFoodDetails() {
        binding.txtDetailsName.setText(food.getName());
        binding.txtDetailsPrice.setText(food.getPrice());
        binding.txtDetailsRating.setText("★ " + food.getRating());
        binding.txtDetailsDeliveryTime.setText("⏱ " + food.getDeliveryTime());
        binding.txtDetailsDescription.setText(food.getDescription());

        Glide.with(this)
                .load(food.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_foodiego_logo)
                .error(R.drawable.ic_foodiego_logo)
                .centerCrop()
                .into(binding.imgDetailsFood);
    }

    private void setupListeners() {
        binding.btnDetailsBack.setOnClickListener(v -> finishAndSlide());

        binding.btnDetailsPlus.setOnClickListener(v -> {
            quantity++;
            binding.txtDetailsQuantity.setText(String.valueOf(quantity));
        });

        binding.btnDetailsMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.txtDetailsQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.btnDetailsAddToCart.setOnClickListener(v -> addToCartFirestore());
    }

    private void addToCartFirestore() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please sign in to add items to cart!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnDetailsAddToCart.setEnabled(false);

        Repository.getInstance().getCart(userId, new Repository.ApiCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> result) {
                boolean exists = false;
                List<CartItem> cartList = result != null ? result : new ArrayList<>();
                for (CartItem item : cartList) {
                    if (item.getFood() != null && item.getFood().getId() != null && item.getFood().getId().equalsIgnoreCase(food.getId())) {
                        item.setQuantity(item.getQuantity() + quantity);
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    cartList.add(new CartItem(food, quantity));
                }

                Repository.getInstance().syncCart(userId, cartList, new Repository.ApiCallback<GenericResponse>() {
                    @Override
                    public void onSuccess(GenericResponse syncResult) {
                        binding.btnDetailsAddToCart.setEnabled(true);
                        Toast.makeText(FoodDetailsActivity.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                        finishAndSlide();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        binding.btnDetailsAddToCart.setEnabled(true);
                        Toast.makeText(FoodDetailsActivity.this, "Error adding to cart: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.btnDetailsAddToCart.setEnabled(true);
                Toast.makeText(FoodDetailsActivity.this, "Error fetching cart: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishAndSlide() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void applyScaleAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
            }
            return false;
        });
    }
}
