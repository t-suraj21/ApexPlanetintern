package com.foodiego.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.databinding.ItemCartFoodBinding;
import com.foodiego.models.CartItem;

import java.util.List;

/**
 * Adapter for the Cart Items RecyclerView.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItemList;
    private final OnCartQuantityChangeListener quantityChangeListener;

    public interface OnCartQuantityChangeListener {
        void onQuantityChanged(List<CartItem> updatedList);
    }

    public CartAdapter(List<CartItem> cartItemList, OnCartQuantityChangeListener listener) {
        this.cartItemList = cartItemList;
        this.quantityChangeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartFoodBinding binding = ItemCartFoodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItemList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartFoodBinding binding;

        public CartViewHolder(@NonNull ItemCartFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem cartItem, int position) {
            binding.txtCartFoodName.setText(cartItem.getFood().getName());
            binding.txtCartFoodPrice.setText(cartItem.getFood().getPrice());
            binding.txtCartQuantity.setText(String.valueOf(cartItem.getQuantity()));

            Context context = itemView.getContext();

            // Load Image
            Glide.with(context)
                    .load(cartItem.getFood().getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_foodiego_logo)
                    .error(R.drawable.ic_foodiego_logo)
                    .centerCrop()
                    .into(binding.imgCartFood);

            // Plus Action Click Handler
            binding.btnCartPlus.setOnClickListener(v -> {
                int currentQty = cartItem.getQuantity();
                cartItem.setQuantity(currentQty + 1);
                binding.txtCartQuantity.setText(String.valueOf(cartItem.getQuantity()));
                
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged(cartItemList);
                }
            });

            // Minus Action Click Handler
            binding.btnCartMinus.setOnClickListener(v -> {
                int currentQty = cartItem.getQuantity();
                if (currentQty > 1) {
                    cartItem.setQuantity(currentQty - 1);
                    binding.txtCartQuantity.setText(String.valueOf(cartItem.getQuantity()));
                } else {
                    // Qty is 1, remove item from cart
                    cartItemList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItemList.size());
                }

                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged(cartItemList);
                }
            });
        }
    }
}
