package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrderTrackingActivity extends AppCompatActivity {

    TextView tvOrderId, tvTrackingDetails, tvTimeline;
    Button btnBackHomeTracking;

    FirebaseFirestore db;

    String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvTrackingDetails = findViewById(R.id.tvTrackingDetails);
        tvTimeline = findViewById(R.id.tvTimeline);

        btnBackHomeTracking = findViewById(R.id.btnBackHomeTracking);

        db = FirebaseFirestore.getInstance();

        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null || orderId.isEmpty()) {
            tvOrderId.setText("Order ID: Not available");
            tvTrackingDetails.setText("Order details could not be loaded.");
        } else {
            tvOrderId.setText("Order ID: " + orderId);
            loadOrderDetails(orderId);
        }

        btnBackHomeTracking.setOnClickListener(v -> {
            LoadingDialogUtil.showQuick(this);
            Intent intent = new Intent(OrderTrackingActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadOrderDetails(String orderId) {
        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

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
                        String orderStatus = documentSnapshot.getString("orderStatus");
                        String paymentStatus = documentSnapshot.getString("paymentStatus");
                        String paymentMethod = documentSnapshot.getString("paymentMethod");

                        Long quantityLong = documentSnapshot.getLong("quantity");
                        Long totalPriceLong = documentSnapshot.getLong("totalPrice");

                        int quantity = quantityLong == null ? 0 : quantityLong.intValue();
                        int totalPrice = totalPriceLong == null ? 0 : totalPriceLong.intValue();

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
                        if (orderStatus == null) orderStatus = "Pending";
                        if (paymentStatus == null || paymentStatus.isEmpty()) paymentStatus = "Paid";
                        if (paymentMethod == null || paymentMethod.isEmpty()) paymentMethod = "Dummy Card";

                        String details =
                                "Product: " + productName + "\n" +
                                        "Category: " + categoryName + "\n" +
                                        "Size: " + size + "\n" +
                                        "Material: " + material + "\n" +
                                        "Quantity: " + quantity + "\n\n" +

                                        "Design File: " + designFileName + "\n" +
                                        "Instructions: " + instructions + "\n\n" +

                                        "Delivery Method: " + deliveryMethod + "\n" +
                                        "Address / Pickup: " + deliveryAddress + "\n" +
                                        "Date: " + orderDate + "\n" +
                                        "Time: " + orderTime + "\n\n" +

                                        "Total Price: Rs. " + totalPrice + "\n" +
                                        "Payment: " + paymentStatus + " (" + paymentMethod + ")\n" +
                                        "Current Status: " + orderStatus;

                        tvTrackingDetails.setText(details);
                        setTimeline(orderStatus);

                    } else {
                        tvTrackingDetails.setText("Order not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvTrackingDetails.setText("Failed to load order details.");
                });
    }

    private void setTimeline(String status) {
        if (status == null) {
            status = "Pending";
        }

        if (status.equalsIgnoreCase("Completed")) {
            status = "Complete";
        }

        if (status.equalsIgnoreCase("Canceled") || status.equalsIgnoreCase("Cancel")) {
            status = "Cancelled";
        }

        if (status.equalsIgnoreCase("Pending")) {
            tvTimeline.setText("* Pending\n- Processing\n- Ready for Pickup / Delivery\n- Complete\n- Cancelled");
        } else if (status.equalsIgnoreCase("Processing")) {
            tvTimeline.setText("Done Pending\n* Processing\n- Ready for Pickup / Delivery\n- Complete\n- Cancelled");
        } else if (status.equalsIgnoreCase("Ready")) {
            tvTimeline.setText("Done Pending\nDone Processing\n* Ready for Pickup / Delivery\n- Complete\n- Cancelled");
        } else if (status.equalsIgnoreCase("Complete")) {
            tvTimeline.setText("Done Pending\nDone Processing\nDone Ready for Pickup / Delivery\n* Complete");
        } else if (status.equalsIgnoreCase("Cancelled")) {
            tvTimeline.setText("Order Cancelled");
        } else {
            tvTimeline.setText("* Pending\n- Processing\n- Ready for Pickup / Delivery\n- Complete\n- Cancelled");
        }
    }
}
