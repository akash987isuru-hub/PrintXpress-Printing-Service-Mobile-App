package com.example.printxpress;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminOrdersActivity extends AppCompatActivity {

    LinearLayout adminOrdersContainer;
    Button btnRefreshAdminOrders, btnBackAdminFromOrders;

    FirebaseFirestore db;
    int orderCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        adminOrdersContainer = findViewById(R.id.adminOrdersContainer);
        btnRefreshAdminOrders = findViewById(R.id.btnRefreshAdminOrders);
        btnBackAdminFromOrders = findViewById(R.id.btnBackAdminFromOrders);

        db = FirebaseFirestore.getInstance();

        loadAllOrders();

        btnRefreshAdminOrders.setOnClickListener(v -> {
            loadAllOrders();
            Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
        });

        btnBackAdminFromOrders.setOnClickListener(v -> finish());
    }

    private void loadAllOrders() {
        adminOrdersContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading customer orders...");
        loadingText.setTextSize(15);
        loadingText.setTextColor(0xFF666666);
        loadingText.setPadding(16, 16, 16, 16);
        adminOrdersContainer.addView(loadingText);

        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    adminOrdersContainer.removeAllViews();
                    orderCount = 0;

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptyText = new TextView(this);
                        emptyText.setText("No customer orders available.");
                        emptyText.setTextSize(15);
                        emptyText.setTextColor(0xFF222222);
                        emptyText.setPadding(16, 16, 16, 16);
                        emptyText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                        adminOrdersContainer.addView(emptyText);
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        orderCount++;

                        String orderId = document.getId();

                        String productName = document.getString("productName");
                        String categoryName = document.getString("categoryName");
                        String orderStatus = document.getString("orderStatus");
                        String deliveryMethod = document.getString("deliveryMethod");

                        Long quantityLong = document.getLong("quantity");
                        Long totalPriceLong = document.getLong("totalPrice");

                        int quantity = quantityLong == null ? 0 : quantityLong.intValue();
                        int totalPrice = totalPriceLong == null ? 0 : totalPriceLong.intValue();

                        if (productName == null) productName = "";
                        if (categoryName == null) categoryName = "";
                        if (orderStatus == null) orderStatus = "Pending";
                        if (deliveryMethod == null) deliveryMethod = "";

                        addOrderCard(orderCount, orderId, productName, categoryName, quantity, totalPrice, orderStatus, deliveryMethod);
                    }
                })
                .addOnFailureListener(e -> {
                    adminOrdersContainer.removeAllViews();

                    TextView errorText = new TextView(this);
                    errorText.setText("Failed to load orders.");
                    errorText.setTextSize(15);
                    errorText.setTextColor(0xFF222222);
                    errorText.setPadding(16, 16, 16, 16);
                    errorText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                    adminOrdersContainer.addView(errorText);

                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addOrderCard(int orderNumber, String orderId, String productName, String categoryName,
                              int quantity, int totalPrice, String orderStatus, String deliveryMethod) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setClickable(true);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 14);
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText("Order " + orderNumber);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF0B3D91);

        TextView product = new TextView(this);
        product.setText("Product: " + productName);
        product.setTextSize(15);
        product.setTextColor(0xFF222222);
        product.setPadding(0, 8, 0, 0);

        TextView category = new TextView(this);
        category.setText("Category: " + categoryName);
        category.setTextSize(15);
        category.setTextColor(0xFF222222);

        TextView quantityText = new TextView(this);
        quantityText.setText("Quantity: " + quantity);
        quantityText.setTextSize(15);
        quantityText.setTextColor(0xFF222222);

        TextView priceText = new TextView(this);
        priceText.setText("Price: Rs. " + totalPrice);
        priceText.setTextSize(15);
        priceText.setTypeface(null, Typeface.BOLD);
        priceText.setTextColor(0xFFFF9800);

        TextView deliveryText = new TextView(this);
        deliveryText.setText("Delivery Method: " + deliveryMethod);
        deliveryText.setTextSize(15);
        deliveryText.setTextColor(0xFF222222);

        TextView statusText = new TextView(this);
        statusText.setText("Status: " + orderStatus);
        statusText.setTextSize(16);
        statusText.setTypeface(null, Typeface.BOLD);
        statusText.setTextColor(0xFF0B3D91);
        statusText.setPadding(0, 8, 0, 0);

        TextView tapText = new TextView(this);
        tapText.setText("Tap to update status");
        tapText.setTextSize(13);
        tapText.setTextColor(0xFF666666);
        tapText.setPadding(0, 8, 0, 0);

        card.addView(title);
        card.addView(product);
        card.addView(category);
        card.addView(quantityText);
        card.addView(priceText);
        card.addView(deliveryText);
        card.addView(statusText);
        card.addView(tapText);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(AdminOrdersActivity.this, AdminOrderDetailsActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        adminOrdersContainer.addView(card);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllOrders();
    }
}