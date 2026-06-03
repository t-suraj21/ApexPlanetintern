package com.foodiego.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.foodiego.R;
import com.foodiego.adapters.CategoryAdapter;
import com.foodiego.adapters.PopularFoodAdapter;
import com.foodiego.adapters.RecommendedFoodAdapter;
import com.foodiego.databinding.ActivityHomeBinding;
import com.foodiego.models.Category;
import com.foodiego.models.Food;
import com.foodiego.models.User;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Home Screen Dashboard Activity.
 * Displays Categories list, Popular Foods horizontal list, and Recommended Foods vertical list.
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private List<Food> fullFoodList = new ArrayList<>();
    private PopularFoodAdapter popularAdapter;
    private RecommendedFoodAdapter recommendedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerViews(); // Initialize with empty adapters
        setupBottomNavigation();
        fetchFoodData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        loadUserProfile();
    }

    private void setupToolbar() {
        binding.imgToolbarProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void loadUserProfile() {
        User user = SessionManager.getInstance(this).getUserDetails();
        if (user != null) {
            String name = user.getName();
            if (name != null && !name.isEmpty()) {
                binding.txtGreeting.setText("Hello, " + name.split(" ")[0] + " 👋");
            }
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(HomeActivity.this)
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(binding.imgToolbarProfile);
            }
        }
    }

    private void setupRecyclerViews() {
        // Categories
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Pizza", R.drawable.ic_pizza));
        categories.add(new Category("Burger", R.drawable.ic_burger));
        categories.add(new Category("Pasta", R.drawable.ic_pasta));
        categories.add(new Category("Sandwich", R.drawable.ic_sandwich));
        categories.add(new Category("Drinks", R.drawable.ic_drinks));
        categories.add(new Category("Desserts", R.drawable.ic_dessert));

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(new CategoryAdapter(categories, cat -> filterFoods(cat.getName())));

        // Popular Foods (Empty initially)
        popularAdapter = new PopularFoodAdapter(new ArrayList<>());
        binding.rvPopularFoods.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPopularFoods.setAdapter(popularAdapter);

        // Recommended Foods (Empty initially)
        recommendedAdapter = new RecommendedFoodAdapter(new ArrayList<>());
        binding.rvRecommendedFoods.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecommendedFoods.setAdapter(recommendedAdapter);
    }

    private void fetchFoodData() {
        binding.layoutHomeLoading.setVisibility(View.VISIBLE);
        Repository.getInstance().getFoods(new Repository.ApiCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> result) {
                binding.layoutHomeLoading.setVisibility(View.GONE);
                if (result == null || result.isEmpty()) return;
                
                fullFoodList = result;

                // Update Popular
                List<Food> popularFoods = new ArrayList<>(result.subList(0, Math.min(5, result.size())));
                popularAdapter = new PopularFoodAdapter(popularFoods);
                binding.rvPopularFoods.setAdapter(popularAdapter);

                // Update Recommended
                recommendedAdapter = new RecommendedFoodAdapter(result);
                binding.rvRecommendedFoods.setAdapter(recommendedAdapter);
            }

            @Override
            public void onFailure(String errorMessage) {
                binding.layoutHomeLoading.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterFoods(String categoryName) {
        if (fullFoodList.isEmpty()) return;
        List<Food> filtered = new ArrayList<>();
        for (Food f : fullFoodList) {
            if (f.getName().toLowerCase().contains(categoryName.toLowerCase())) filtered.add(f);
        }
        binding.rvRecommendedFoods.setAdapter(new RecommendedFoodAdapter(filtered.isEmpty() ? fullFoodList : filtered));
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            
            Intent intent = null;
            if (itemId == R.id.nav_cart) intent = new Intent(this, CartActivity.class);
            else if (itemId == R.id.nav_orders) intent = new Intent(this, OrdersActivity.class);
            else if (itemId == R.id.nav_profile) intent = new Intent(this, ProfileActivity.class);
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
