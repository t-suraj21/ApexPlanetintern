package com.foodiego.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.activities.FoodDetailsActivity;
import com.foodiego.databinding.ItemFoodBinding;
import com.foodiego.models.CartItem;
import com.foodiego.models.Food;
import com.foodiego.models.GenericResponse;
import com.foodiego.network.Repository;
import com.foodiego.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Vertical Foods RecyclerView.
 */
public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private final List<Food> foodList;

    public FoodAdapter(List<Food> foodList) {
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodBinding binding = ItemFoodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        holder.bind(foodList.get(position));
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        private final ItemFoodBinding binding;

        public FoodViewHolder(@NonNull ItemFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Food food) {
            binding.txtFoodName.setText(food.getName());
            binding.txtFoodDescription.setText(food.getDescription());
            binding.txtFoodPrice.setText(food.getPrice());

            Context context = itemView.getContext();

            // Glide Premium Image Loading
            Glide.with(context)
                    .load(food.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_foodiego_logo)
                    .error(R.drawable.ic_foodiego_logo)
                    .centerCrop()
                    .into(binding.imgFood);

            // Active Plus Button Click interaction
            binding.btnAddFood.setOnClickListener(v -> {
                String userId = SessionManager.getInstance(context).getUserId();
                if (userId == null) {
                    Toast.makeText(context, "Please sign in to add items!", Toast.LENGTH_SHORT).show();
                    return;
                }

                binding.btnAddFood.setEnabled(false);
                Repository.getInstance().getCart(userId, new Repository.ApiCallback<List<CartItem>>() {
                    @Override
                    public void onSuccess(List<CartItem> result) {
                        boolean exists = false;
                        for (CartItem item : result) {
                            if (item.getFood().getId() != null && item.getFood().getId().equalsIgnoreCase(food.getId())) {
                                item.setQuantity(item.getQuantity() + 1);
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            result.add(new CartItem(food, 1));
                        }

                        Repository.getInstance().syncCart(userId, result, new Repository.ApiCallback<GenericResponse>() {
                            @Override
                            public void onSuccess(GenericResponse syncResult) {
                                binding.btnAddFood.setEnabled(true);
                                Toast.makeText(context, food.getName() + " added to Cart!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                binding.btnAddFood.setEnabled(true);
                                Toast.makeText(context, "Cart update failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        List<CartItem> newList = new ArrayList<>();
                        newList.add(new CartItem(food, 1));

                        Repository.getInstance().syncCart(userId, newList, new Repository.ApiCallback<GenericResponse>() {
                            @Override
                            public void onSuccess(GenericResponse syncResult) {
                                binding.btnAddFood.setEnabled(true);
                                Toast.makeText(context, food.getName() + " added to Cart!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                binding.btnAddFood.setEnabled(true);
                                Toast.makeText(context, "Failed to update Cart: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            });

            // Card item tap interaction opens details
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, FoodDetailsActivity.class);
                intent.putExtra("food_item", food);
                context.startActivity(intent);
                if (context instanceof Activity) {
                    ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
        }
    }
}
