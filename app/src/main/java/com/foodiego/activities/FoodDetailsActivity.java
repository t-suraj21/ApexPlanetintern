package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.databinding.ActivityFoodDetailsBinding;
import com.foodiego.models.CartItem;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Food Details Screen Activity.
 * Displays large image, details descriptions, pricing, handles a quantity picker, and integrates adding to the cart list.
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

        // Retrieve Serializable Food entity from Intent parameters
        food = (Food) getIntent().getSerializableExtra("food_item");

        if (food != null) {
            bindFoodDetails();
            setupListeners();
            applyScaleAnimation(binding.btnDetailsAddToCart);
            fetchFreshDetails();
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

        // Glide Image Loader
        Glide.with(this)
                .load(food.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_foodiego_logo)
                .error(R.drawable.ic_foodiego_logo)
                .centerCrop()
                .into(binding.imgDetailsFood);
    }

    private void fetchFreshDetails() {
        Repository.getInstance().getFoodById(food.getId(), new Repository.ApiCallback<Food>() {
            @Override
            public void onSuccess(Food result) {
                if (result != null) {
                    food = result;
                    bindFoodDetails();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                // Keep displaying pre-loaded details if API fails
            }
        });
    }

    private void setupListeners() {
        // Back Button
        binding.btnDetailsBack.setOnClickListener(v -> finishAndSlide());

        // Increment Quantity
        binding.btnDetailsPlus.setOnClickListener(v -> {
            quantity++;
            binding.txtDetailsQuantity.setText(String.valueOf(quantity));
        });

        // Decrement Quantity
        binding.btnDetailsMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.txtDetailsQuantity.setText(String.valueOf(quantity));
            }
        });

        // Add to Cart in MySQL via REST API
        binding.btnDetailsAddToCart.setOnClickListener(v -> addToCartREST());
    }

    private void addToCartREST() {
        String userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please sign in to add items to cart!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnDetailsAddToCart.setEnabled(false);

        // 1. Fetch current cart from MySQL
        Repository.getInstance().getCart(userId, new Repository.ApiCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> result) {
                // 2. Append or increment local quantities
                boolean exists = false;
                for (CartItem item : result) {
                    if (item.getFood().getId() != null && item.getFood().getId().equalsIgnoreCase(food.getId())) {
                        item.setQuantity(item.getQuantity() + quantity);
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    result.add(new CartItem(food, quantity));
                }

                // 3. Sync full list back to MySQL
                Repository.getInstance().syncCart(userId, result, new Repository.ApiCallback<GenericResponse>() {
                    @Override
                    public void onSuccess(GenericResponse syncResult) {
                        binding.btnDetailsAddToCart.setEnabled(true);
                        Toast.makeText(FoodDetailsActivity.this, quantity + " " + food.getName() + "(s) added to Cart!", Toast.LENGTH_SHORT).show();
                        finishAndSlide();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        binding.btnDetailsAddToCart.setEnabled(true);
                        Toast.makeText(FoodDetailsActivity.this, "Cart sync failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                // Create list if fetch fails
                List<CartItem> newList = new ArrayList<>();
                newList.add(new CartItem(food, quantity));
                
                Repository.getInstance().syncCart(userId, newList, new Repository.ApiCallback<GenericResponse>() {
                    @Override
                    public void onSuccess(GenericResponse syncResult) {
                        binding.btnDetailsAddToCart.setEnabled(true);
                        Toast.makeText(FoodDetailsActivity.this, quantity + " " + food.getName() + "(s) added to Cart!", Toast.LENGTH_SHORT).show();
                        finishAndSlide();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        binding.btnDetailsAddToCart.setEnabled(true);
                        Toast.makeText(FoodDetailsActivity.this, "Failed to update Cart: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
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

    /**
     * Programmatic touch-scale micro-animation for buttons.
     */
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
