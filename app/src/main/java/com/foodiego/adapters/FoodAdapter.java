package com.foodiego.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.databinding.ItemFoodBinding;
import com.foodiego.models.Food;

import java.util.List;

/**
 * Adapter for the Vertical Popular Foods RecyclerView.
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

            // Glide Premium Image Loading with transition fade-in effect
            Glide.with(itemView.getContext())
                    .load(food.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_foodiego_logo) // custom logo as placeholder
                    .error(R.drawable.ic_foodiego_logo)
                    .centerCrop()
                    .into(binding.imgFood);

            // Active Plus Button Click interaction
            binding.btnAddFood.setOnClickListener(v -> 
                Toast.makeText(itemView.getContext(), 
                        food.getName() + " added to Cart!", 
                        Toast.LENGTH_SHORT).show()
            );

            // Card item tap interaction
            itemView.setOnClickListener(v -> 
                Toast.makeText(itemView.getContext(), 
                        "Opening details for " + food.getName(), 
                        Toast.LENGTH_SHORT).show()
            );
        }
    }
}
