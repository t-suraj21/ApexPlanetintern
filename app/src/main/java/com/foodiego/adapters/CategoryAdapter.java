package com.foodiego.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.foodiego.R;
import com.foodiego.databinding.ItemCategoryBinding;

import java.util.List;

/**
 * Adapter for the Horizontal Categories RecyclerView.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<Category> categoryList;
    private int selectedPosition = 0; // Default: first category is active
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categoryList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        public CategoryViewHolder(@NonNull ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Category category, int position) {
            binding.txtCategoryName.setText(category.getName());
            binding.imgCategoryIcon.setImageResource(category.getIconResId());

            boolean isSelected = (position == selectedPosition);

            // Dynamic Styling based on Selection State
            if (isSelected) {
                // Highlighted / Selected State
                binding.cardCategory.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.primaryColor));
                binding.cardCategory.setStrokeWidth(4);
                binding.cardCategory.setCardElevation(6);
                
                binding.layoutIconContainer.setBackgroundResource(R.drawable.bg_splash_gradient);
                binding.imgCategoryIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.white));
                binding.txtCategoryName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primaryColor));
            } else {
                // Default / Unselected State
                binding.cardCategory.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.grayBorder));
                binding.cardCategory.setStrokeWidth(2);
                binding.cardCategory.setCardElevation(2);
                
                binding.layoutIconContainer.setBackgroundResource(R.drawable.bg_circle_icon);
                binding.imgCategoryIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.primaryColor));
                binding.txtCategoryName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.textDark));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = getAdapterPosition();
                
                // Refresh selection views
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);

                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }

    /**
     * Category Class Helper inside Adapter
     */
    public static class Category {
        private final String name;
        private final int iconResId;

        public Category(String name, int iconResId) {
            this.name = name;
            this.iconResId = iconResId;
        }

        public String getName() {
            return name;
        }

        public int getIconResId() {
            return iconResId;
        }
    }
}
