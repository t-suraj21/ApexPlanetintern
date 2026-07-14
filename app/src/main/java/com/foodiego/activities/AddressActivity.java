package com.foodiego.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.foodiego.R;
import com.foodiego.adapters.AddressAdapter;
import com.foodiego.databinding.ActivityAddressBinding;
import com.foodiego.firebase.FirebaseHelper;
import com.foodiego.models.Address;
import com.foodiego.utils.OfflineCacheManager;
import com.foodiego.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity managing the Address book.
 * Allows adding, editing, deleting, and selecting a default address with Firestore backend and offline caching.
 */
public class AddressActivity extends AppCompatActivity implements AddressAdapter.OnAddressActionListener {

    private ActivityAddressBinding binding;
    private List<Address> addressList = new ArrayList<>();
    private AddressAdapter adapter;
    private String userId;

    private static final String PREFS_NAME = "FoodieGoAddresses";
    private static final String KEY_ADDR_CACHE = "address_list_cache";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = SessionManager.getInstance(this).getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please sign in to manage addresses!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        loadAddresses();
    }

    private void setupUI() {
        binding.btnAddressBack.setOnClickListener(v -> finish());
        binding.btnAddAddress.setOnClickListener(v -> showAddressFormDialog(null));

        adapter = new AddressAdapter(addressList, this);
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddresses.setAdapter(adapter);
    }

    private void loadAddresses() {
        binding.progressAddress.setVisibility(View.VISIBLE);
        binding.rvAddresses.setVisibility(View.GONE);
        binding.layoutEmptyAddress.setVisibility(View.GONE);

        if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
            loadFromCache();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (firestore == null) {
            loadFromCache();
            return;
        }

        firestore.collection("addresses")
                .document(userId)
                .collection("list")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        addressList.clear();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Address address = doc.toObject(Address.class);
                            if (address != null) {
                                addressList.add(address);
                            }
                        }
                        saveToCache();
                        onAddressesLoaded();
                    } else {
                        loadFromCache();
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onAddressesLoaded() {
        binding.progressAddress.setVisibility(View.GONE);
        if (addressList.isEmpty()) {
            binding.layoutEmptyAddress.setVisibility(View.VISIBLE);
        } else {
            binding.rvAddresses.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void saveToCache() {
        String json = new Gson().toJson(addressList);
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ADDR_CACHE + "_" + userId, json)
                .apply();
    }

    private void loadFromCache() {
        String json = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ADDR_CACHE + "_" + userId, null);
        addressList.clear();
        if (json != null) {
            Type type = new TypeToken<ArrayList<Address>>() {}.getType();
            List<Address> cached = new Gson().fromJson(json, type);
            if (cached != null) {
                addressList.addAll(cached);
            }
        }
        onAddressesLoaded();
    }

    private void showAddressFormDialog(Address existingAddress) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_address_form, null);
        EditText etDetail = dialogView.findViewById(R.id.etDialogAddressDetail);
        EditText etPhone = dialogView.findViewById(R.id.etDialogAddressPhone);
        RadioGroup rgTag = dialogView.findViewById(R.id.rgDialogAddressTag);
        RadioButton rbHome = dialogView.findViewById(R.id.rbDialogHome);
        RadioButton rbWork = dialogView.findViewById(R.id.rbDialogWork);
        RadioButton rbOther = dialogView.findViewById(R.id.rbDialogOther);

        if (existingAddress != null) {
            etDetail.setText(existingAddress.getDetail());
            etPhone.setText(existingAddress.getPhone());
            String tag = existingAddress.getTitle();
            if (tag.equalsIgnoreCase("Home")) rbHome.setChecked(true);
            else if (tag.equalsIgnoreCase("Work")) rbWork.setChecked(true);
            else rbOther.setChecked(true);
        } else {
            rbHome.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existingAddress == null ? "Add Address" : "Edit Address")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String detail = etDetail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String tag = "Other";
                int selectedId = rgTag.getCheckedRadioButtonId();
                if (selectedId == R.id.rbDialogHome) tag = "Home";
                else if (selectedId == R.id.rbDialogWork) tag = "Work";

                if (detail.isEmpty()) {
                    etDetail.setError("Address is required!");
                    return;
                }
                if (phone.isEmpty() || phone.length() < 10) {
                    etPhone.setError("Valid phone number is required!");
                    return;
                }

                dialog.dismiss();
                if (existingAddress == null) {
                    // Create new
                    String id = UUID.randomUUID().toString();
                    boolean isDefault = addressList.isEmpty(); // First address is default
                    Address newAddr = new Address(id, tag, detail, phone, isDefault);
                    saveAddress(newAddr);
                } else {
                    // Update existing
                    existingAddress.setTitle(tag);
                    existingAddress.setDetail(detail);
                    existingAddress.setPhone(phone);
                    saveAddress(existingAddress);
                }
            });
        });

        dialog.show();
    }

    private void saveAddress(Address address) {
        if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
            // Local save only
            int index = -1;
            for (int i = 0; i < addressList.size(); i++) {
                if (addressList.get(i).getId().equalsIgnoreCase(address.getId())) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                addressList.set(index, address);
            } else {
                addressList.add(address);
            }
            saveToCache();
            onAddressesLoaded();
            Toast.makeText(this, "Saved locally (Offline Mode)", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (firestore == null) return;

        firestore.collection("addresses")
                .document(userId)
                .collection("list")
                .document(address.getId())
                .set(address)
                .addOnSuccessListener(aVoid -> {
                    loadAddresses();
                    Toast.makeText(AddressActivity.this, "Address saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddressActivity.this, "Failed to save to cloud: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onSetDefault(Address address) {
        // Mark all others as non-default
        for (Address addr : addressList) {
            addr.setDefault(addr.getId().equalsIgnoreCase(address.getId()));
        }

        if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
            saveToCache();
            adapter.notifyDataSetChanged();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (firestore == null) return;

        // Perform transactional update
        for (Address addr : addressList) {
            firestore.collection("addresses")
                    .document(userId)
                    .collection("list")
                    .document(addr.getId())
                    .update("default", addr.isDefault());
        }
        Toast.makeText(this, "Default address updated", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEdit(Address address) {
        showAddressFormDialog(address);
    }

    @Override
    public void onDelete(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (!OfflineCacheManager.getInstance(this).isNetworkAvailable()) {
                        addressList.remove(address);
                        saveToCache();
                        onAddressesLoaded();
                        return;
                    }

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    if (firestore == null) return;

                    firestore.collection("addresses")
                            .document(userId)
                            .collection("list")
                            .document(address.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                loadAddresses();
                                Toast.makeText(AddressActivity.this, "Address deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddressActivity.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
