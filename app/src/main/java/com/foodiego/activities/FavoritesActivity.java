package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.databinding.ActivityFavoritesBinding;
import com.foodiego.databinding.ItemFoodBinding;
import com.foodiego.firebase.FirebaseHelper;
import com.foodiego.models.Food;
import com.foodiego.utils.OfflineCacheManager;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity for displaying and managing User Favorite Food items.
 * Integrates Firestore with offline caching support.
 */
public class FavoritesActivity extends AppCompatActivity {

    private ActivityFavoritesBinding binding;
    private List<Food> favoriteList = new ArrayList<>();
    private FavoritesAdapter adapter;
    private String userId;

    private static final String PREF_FAV = "FoodieGoFavs";
    private static final String KEY_FAV_IDS = "favorite_ids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) {
            Toast.makeText(this, "Sign in to see favorites!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        loadFavorites();
    }

    private void setupUI() {
        binding.btnFavoritesBack.setOnClickListener(v -> finish());

        adapter = new FavoritesAdapter(favoriteList);
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorites.setAdapter(adapter);

        // Load dummy Lottie animation placeholder if empty
        binding.lottieEmptyFavorites.setAnimation("search_empty.json");
    }

    private void loadFavorites() {
        binding.progressFavorites.setVisibility(View.VISIBLE);
        binding.rvFavorites.setVisibility(View.GONE);
        binding.layoutEmptyFavorites.setVisibility(View.GONE);

        if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
            // Load local backup from cached foods filtering by locally saved favorite IDs
            List<Food> cachedFoods = OfflineCacheManager.getInstance(this).getCachedFoods();
            Set<String> favIds = getLocalFavoriteIds();
            favoriteList.clear();
            for (Food f : cachedFoods) {
                if (favIds.contains(f.getId())) {
                    favoriteList.add(f);
                }
            }
            onFavoritesLoaded();
            return;
        }

        FirebaseHelper.getInstance().getFavorites(userId, new FirebaseHelper.FirebaseCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> result) {
                favoriteList.clear();
                if (result != null) {
                    favoriteList.addAll(result);
                    // Sync local favorite IDs list
                    Set<String> favIds = new HashSet<>();
                    for (Food f : result) {
                        favIds.add(f.getId());
                    }
                    saveLocalFavoriteIds(favIds);
                }
                onFavoritesLoaded();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Fallback to local
                List<Food> cachedFoods = OfflineCacheManager.getInstance(FavoritesActivity.this).getCachedFoods();
                Set<String> favIds = getLocalFavoriteIds();
                favoriteList.clear();
                for (Food f : cachedFoods) {
                    if (favIds.contains(f.getId())) {
                        favoriteList.add(f);
                    }
                }
                onFavoritesLoaded();
                Toast.makeText(FavoritesActivity.this, "Offline Mode: Showing cached favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onFavoritesLoaded() {
        binding.progressFavorites.setVisibility(View.GONE);
        if (favoriteList.isEmpty()) {
            binding.layoutEmptyFavorites.setVisibility(View.VISIBLE);
        } else {
            binding.rvFavorites.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private Set<String> getLocalFavoriteIds() {
        return getSharedPreferences(PREF_FAV, Context.MODE_PRIVATE)
                .getStringSet(KEY_FAV_IDS, new HashSet<>());
    }

    private void saveLocalFavoriteIds(Set<String> ids) {
        getSharedPreferences(PREF_FAV, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_FAV_IDS, ids)
                .apply();
    }

    // --- Inner Adapter class ---
    private class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavViewHolder> {

        private final List<Food> items;

        public FavoritesAdapter(List<Food> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemFoodBinding binding = ItemFoodBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new FavViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class FavViewHolder extends RecyclerView.ViewHolder {
            private final ItemFoodBinding binding;

            public FavViewHolder(@NonNull ItemFoodBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(Food food) {
                binding.txtFoodName.setText(food.getName());
                binding.txtFoodDescription.setText(food.getDescription());
                binding.txtFoodPrice.setText(food.getPrice());

                // Set favorite heart active
                binding.btnAddFood.setText("❤️");

                Glide.with(FavoritesActivity.this)
                        .load(food.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_foodiego_logo)
                        .error(R.drawable.ic_foodiego_logo)
                        .centerCrop()
                        .into(binding.imgFood);

                // Clicking the heart removes it from favorites
                binding.btnAddFood.setOnClickListener(v -> {
                    String fId = food.getId();
                    FirebaseHelper.getInstance().removeFavorite(userId, fId, new FirebaseHelper.FirebaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // Update local list
                            Set<String> localFavs = getLocalFavoriteIds();
                            localFavs.remove(fId);
                            saveLocalFavoriteIds(localFavs);

                            items.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());
                            if (items.isEmpty()) {
                                binding.getRoot().post(() -> onFavoritesLoaded());
                            }
                            Toast.makeText(FavoritesActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(FavoritesActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        }
    }
}
