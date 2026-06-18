package com.example.printxpress;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminOrderDetailsActivity extends AppCompatActivity {

    TextView tvAdminOrderDetails;
    Spinner spinnerOrderStatus;
    Button btnUpdateStatus, btnBackAdminOrders;

    FirebaseFirestore db;

    String orderId;
    String currentStatus = "Pending";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_details);

        tvAdminOrderDetails = findViewById(R.id.tvAdminOrderDetails);
        spinnerOrderStatus = findViewById(R.id.spinnerOrderStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnBackAdminOrders = findViewById(R.id.btnBackAdminOrders);

        db = FirebaseFirestore.getInstance();

        orderId = getIntent().getStringExtra("orderId");

        setStatusSpinner();

        if (orderId == null || orderId.isEmpty()) {
            tvAdminOrderDetails.setText("Order ID not found.");
        } else {
            loadOrderDetails();
        }

        btnUpdateStatus.setOnClickListener(v -> updateOrderStatus());

        btnBackAdminOrders.setOnClickListener(v -> finish());
    }

    private void setStatusSpinner() {
        String[] statuses = {
                "Pending",
                "Processing",
                "Ready",
                "Complete",
                "Cancelled"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statuses
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderStatus.setAdapter(adapter);
    }

    private void loadOrderDetails() {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String userId = documentSnapshot.getString("userId");
                        String productName = documentSnapshot.getString("productName");
                        String categoryName = documentSnapshot.getString("categoryName");
                        String size = documentSnapshot.getString("size");
                        String material = documentSnapshot.getString("material");
                        String instructions = documentSnapshot.getString("instructions");
                        String designFileName = documentSnapshot.getString("designFileName");
                        String deliveryMethod = documentSnapshot.getString("deliveryMethod");
                        String deliveryAddress = documentSnapshot.getString("deliveryAddress");
                        String orderDate = documentSnapshot.getString("orderDate");
                        String orderTime = documentSnapshot.getString("orderTime");
                        currentStatus = documentSnapshot.getString("orderStatus");
                        String paymentStatus = documentSnapshot.getString("paymentStatus");
                        String paymentMethod = documentSnapshot.getString("paymentMethod");

                        Long quantityLong = documentSnapshot.getLong("quantity");
                        Long totalPriceLong = documentSnapshot.getLong("totalPrice");

                        int quantity = quantityLong == null ? 0 : quantityLong.intValue();
                        int totalPrice = totalPriceLong == null ? 0 : totalPriceLong.intValue();

                        if (userId == null) userId = "";
                        if (productName == null) productName = "";
                        if (categoryName == null) categoryName = "";
                        if (size == null) size = "";
                        if (material == null) material = "";
                        if (instructions == null || instructions.isEmpty()) instructions = "No instructions";
                        if (designFileName == null || designFileName.isEmpty()) designFileName = "No file selected";
                        if (deliveryMethod == null) deliveryMethod = "";
                        if (deliveryAddress == null) deliveryAddress = "";
                        if (orderDate == null) orderDate = "";
                        if (orderTime == null) orderTime = "";
                        if (currentStatus == null) currentStatus = "Pending";
                        if (paymentStatus == null || paymentStatus.isEmpty()) paymentStatus = "Paid";
                        if (paymentMethod == null || paymentMethod.isEmpty()) paymentMethod = "Dummy Card";

                        String details =
                                "Order ID: " + orderId + "\n" +
                                        "Customer ID: " + userId + "\n\n" +

                                        "Product: " + productName + "\n" +
                                        "Category: " + categoryName + "\n" +
                                        "Size: " + size + "\n" +
                                        "Material: " + material + "\n" +
                                        "Quantity: " + quantity + "\n" +
                                        "Total Price: Rs. " + totalPrice + "\n" +
                                        "Payment: " + paymentStatus + " (" + paymentMethod + ")\n\n" +

                                        "Design File: " + designFileName + "\n" +
                                        "Instructions: " + instructions + "\n\n" +

                                        "Delivery Method: " + deliveryMethod + "\n" +
                                        "Address / Pickup: " + deliveryAddress + "\n" +
                                        "Date: " + orderDate + "\n" +
                                        "Time: " + orderTime + "\n\n" +

                                        "Current Status: " + currentStatus;

                        tvAdminOrderDetails.setText(details);
                        setSpinnerToCurrentStatus(currentStatus);

                    } else {
                        tvAdminOrderDetails.setText("Order not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvAdminOrderDetails.setText("Failed to load order details.");
                });
    }

    private void setSpinnerToCurrentStatus(String status) {
        if (status == null) {
            status = "Pending";
        }

        if (status.equalsIgnoreCase("Completed")) {
            status = "Complete";
        }

        if (status.equalsIgnoreCase("Canceled") || status.equalsIgnoreCase("Cancel")) {
            status = "Cancelled";
        }

        for (int i = 0; i < spinnerOrderStatus.getCount(); i++) {
            if (spinnerOrderStatus.getItemAtPosition(i).toString().equalsIgnoreCase(status)) {
                spinnerOrderStatus.setSelection(i);
                break;
            }
        }
    }

    private void updateOrderStatus() {
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedStatus = spinnerOrderStatus.getSelectedItem().toString();

        db.collection("orders").document(orderId)
                .update("orderStatus", selectedStatus)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show();
                    currentStatus = selectedStatus;
                    loadOrderDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}