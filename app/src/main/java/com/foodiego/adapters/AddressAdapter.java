package com.foodiego.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodiego.databinding.ItemAddressBinding;
import com.foodiego.models.Address;

import java.util.List;

/**
 * Adapter for the Addresses RecyclerView in the Address book.
 */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private final List<Address> addressList;
    private final OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onSetDefault(Address address);
        void onEdit(Address address);
        void onDelete(Address address);
    }

    public AddressAdapter(List<Address> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAddressBinding binding = ItemAddressBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AddressViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        holder.bind(addressList.get(position));
    }

    @Override
    public int getItemCount() {
        return addressList != null ? addressList.size() : 0;
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private final ItemAddressBinding binding;

        public AddressViewHolder(@NonNull ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Address address) {
            binding.txtAddressTitle.setText(address.getTitle());
            binding.txtAddressDetail.setText(address.getDetail());
            binding.txtAddressPhone.setText("Phone: " + address.getPhone());

            // Prevent infinite loops on checking changes
            binding.radioDefault.setOnCheckedChangeListener(null);
            binding.radioDefault.setChecked(address.isDefault());

            binding.radioDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && listener != null) {
                    listener.onSetDefault(address);
                }
            });

            binding.btnEditAddress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(address);
                }
            });

            binding.btnDeleteAddress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(address);
                }
            });
        }
    }
}
