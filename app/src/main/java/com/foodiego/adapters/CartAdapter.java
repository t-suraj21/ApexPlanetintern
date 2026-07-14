package com.foodiego.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.foodiego.R;
import com.foodiego.databinding.ItemCartFoodBinding;
import com.foodiego.firebase.AuthManager;
import com.foodiego.firebase.FirestoreManager;
import com.foodiego.models.CartItem;

import java.util.List;

/**
 * Adapter for the Cart Items RecyclerView.
 * Automatically syncs quantity changes and removals with Firebase Firestore.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItemList;
    private final OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onChanged(List<CartItem> updatedList);
    }

    public CartAdapter(List<CartItem> cartItemList, OnCartChangeListener listener) {
        this.cartItemList = cartItemList;
        this.listener = listener;
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
        holder.bind(cartItemList.get(position));
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

        public void bind(CartItem cartItem) {
            binding.txtCartFoodName.setText(cartItem.getFood().getName());
            binding.txtCartFoodPrice.setText(cartItem.getFood().getPrice());
            binding.txtCartQuantity.setText(String.valueOf(cartItem.getQuantity()));

            Glide.with(itemView.getContext())
                    .load(cartItem.getFood().getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_foodiego_logo)
                    .error(R.drawable.ic_foodiego_logo)
                    .centerCrop()
                    .into(binding.imgCartFood);

            binding.btnCartPlus.setOnClickListener(v -> updateQuantity(cartItem, 1));
            binding.btnCartMinus.setOnClickListener(v -> updateQuantity(cartItem, -1));
        }

        private void updateQuantity(CartItem item, int change) {
            String userId = AuthManager.getInstance().getCurrentUserId();
            if (userId == null) return;

            int newQty = item.getQuantity() + change;

            if (newQty <= 0) {
                FirestoreManager.getInstance().removeFromCart(userId, item.getFoodId(), new FirestoreManager.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            cartItemList.remove(pos);
                            notifyItemRemoved(pos);
                            if (listener != null) listener.onChanged(cartItemList);
                        }
                    }

                    @Override
                    public void onFailure(String message) {}
                });
            } else {
                item.setQuantity(newQty);
                FirestoreManager.getInstance().addToCart(userId, item, new FirestoreManager.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        binding.txtCartQuantity.setText(String.valueOf(newQty));
                        if (listener != null) listener.onChanged(cartItemList);
                    }

                    @Override
                    public void onFailure(String message) {
                        item.setQuantity(item.getQuantity() - change);
                    }
                });
            }
        }
    }
}
