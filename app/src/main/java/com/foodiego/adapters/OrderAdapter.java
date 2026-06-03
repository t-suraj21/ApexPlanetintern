package com.foodiego.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodiego.databinding.ItemOrderBinding;
import com.foodiego.models.CartItem;
import com.foodiego.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter representing User's historical Order tickets.
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(@NonNull ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            // Formatted ID
            String croppedId = order.getOrderId();
            if (croppedId != null && croppedId.length() > 8) {
                croppedId = croppedId.substring(0, 8).toUpperCase();
            }
            binding.txtOrderNumber.setText("Order #" + (croppedId != null ? croppedId : "N/A"));

            // Total Amount
            binding.txtOrderTotal.setText(order.getTotalPrice());

            // Status Custom Tint Styling
            String status = order.getStatus() != null ? order.getStatus() : "Pending";
            binding.txtOrderStatus.setText(status);

            if (status.equalsIgnoreCase("Completed")) {
                binding.cardOrderStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // Light Green
                binding.txtOrderStatus.setTextColor(Color.parseColor("#2E7D32")); // Dark Green
            } else if (status.equalsIgnoreCase("Pending")) {
                binding.cardOrderStatus.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // Light Orange
                binding.txtOrderStatus.setTextColor(Color.parseColor("#E65100")); // Dark Orange
            } else {
                binding.cardOrderStatus.setCardBackgroundColor(Color.parseColor("#ECEFF1")); // Light Grey
                binding.txtOrderStatus.setTextColor(Color.parseColor("#37474F")); // Dark Grey
            }

            // Timestamp Formatter
            long timeMillis = order.getTimestamp();
            if (timeMillis > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
                binding.txtOrderDate.setText(sdf.format(new Date(timeMillis)));
            } else {
                binding.txtOrderDate.setText("Date Unavailable");
            }

            // Joined Items Summary list
            List<CartItem> items = order.getItems();
            if (items != null && !items.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    CartItem item = items.get(i);
                    builder.append(item.getFood().getName())
                            .append(" x")
                            .append(item.getQuantity());
                    if (i < items.size() - 1) {
                        builder.append(", ");
                    }
                }
                binding.txtOrderItemsSummary.setText(builder.toString());
            } else {
                binding.txtOrderItemsSummary.setText("No items in order");
            }
        }
    }
}
