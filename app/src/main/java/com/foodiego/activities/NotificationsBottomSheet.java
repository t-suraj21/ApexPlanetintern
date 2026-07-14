package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foodiego.R;
import com.foodiego.databinding.ItemNotificationBinding;
import com.foodiego.databinding.LayoutNotificationsBottomSheetBinding;
import com.foodiego.models.NotificationItem;
import com.foodiego.utils.NotificationHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Bottom Sheet Dialog showing list of recent notifications.
 */
public class NotificationsBottomSheet extends BottomSheetDialogFragment {

    private LayoutNotificationsBottomSheetBinding binding;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private NotificationAdapter adapter;

    public static NotificationsBottomSheet newInstance() {
        return new NotificationsBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutNotificationsBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();
        loadNotifications();
    }

    private void setupUI() {
        binding.lottieEmptyNotifications.setAnimation("search_empty.json");

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        binding.rvNotifications.setAdapter(adapter);

        binding.btnClearNotifications.setOnClickListener(v -> {
            NotificationHelper.clearAllNotifications(getContext());
            loadNotifications();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadNotifications() {
        List<NotificationItem> items = NotificationHelper.getNotifications(getContext());
        notificationList.clear();
        notificationList.addAll(items);

        if (notificationList.isEmpty()) {
            binding.layoutEmptyNotifications.setVisibility(View.VISIBLE);
            binding.lottieEmptyNotifications.playAnimation();
            binding.rvNotifications.setVisibility(View.GONE);
            binding.btnClearNotifications.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyNotifications.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.btnClearNotifications.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Adapter Inner Class ---
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        private final List<NotificationItem> list;

        public NotificationAdapter(List<NotificationItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemNotificationBinding itemBinding = ItemNotificationBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem item = list.get(position);
            holder.binding.txtNotificationTitle.setText(item.getTitle());
            holder.binding.txtNotificationBody.setText(item.getBody());
            holder.binding.txtNotificationTime.setText(formatTime(item.getTimestamp()));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private String formatTime(long timestamp) {
            long diff = System.currentTimeMillis() - timestamp;
            if (diff < 60000) {
                return "Just now";
            } else if (diff < 3600000) {
                long mins = diff / 60000;
                return mins + (mins == 1 ? " minute ago" : " minutes ago");
            } else if (diff < 86400000) {
                long hours = diff / 3600000;
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ItemNotificationBinding binding;

            ViewHolder(ItemNotificationBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
