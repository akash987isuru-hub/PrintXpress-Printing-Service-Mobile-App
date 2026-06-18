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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class CartActivity extends AppCompatActivity {

    LinearLayout ordersContainer;
    TextView tvCartTotal;
    Button btnRefreshOrders, btnBackHomeCart;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String userId;
    int totalOrderedAmount = 0;
    int orderCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ordersContainer = findViewById(R.id.ordersContainer);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        btnRefreshOrders = findViewById(R.id.btnRefreshOrders);
        btnBackHomeCart = findViewById(R.id.btnBackHomeCart);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        } else {
            userId = "guest_user";
        }

        loadPlacedOrders();

        btnRefreshOrders.setOnClickListener(v -> {
            loadPlacedOrders();
            Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
        });

        btnBackHomeCart.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadPlacedOrders() {
        ordersContainer.removeAllViews();

        TextView loadingText = createMessageText("Loading your orders...");
        ordersContainer.addView(loadingText);

        db.collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ordersContainer.removeAllViews();
                    totalOrderedAmount = 0;
                    orderCount = 0;

                    if (queryDocumentSnapshots.isEmpty()) {
                        ordersContainer.addView(createMessageText("You have not placed any orders yet."));
                        tvCartTotal.setText("Total Ordered Amount: Rs. 0");
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String orderId = document.getId();
                        String productName = document.getString("productName");
                        String categoryName = document.getString("categoryName");
                        String material = document.getString("material");
                        String orderStatus = document.getString("orderStatus");
                        String paymentStatus = document.getString("paymentStatus");

                        Long quantityLong = document.getLong("quantity");
                        Long totalPriceLong = document.getLong("totalPrice");

                        int quantity = quantityLong == null ? 0 : quantityLong.intValue();
                        int totalPrice = totalPriceLong == null ? 0 : totalPriceLong.intValue();

                        if (productName == null) productName = "";
                        if (categoryName == null) categoryName = "";
                        if (material == null) material = "";
                        if (orderStatus == null) orderStatus = "Pending";
                        if (paymentStatus == null) paymentStatus = "Paid";

                        orderCount++;
                        totalOrderedAmount += totalPrice;
                        addOrderCard(orderCount, orderId, productName, categoryName, material, quantity, totalPrice, orderStatus, paymentStatus);
                    }

                    tvCartTotal.setText("Total Ordered Amount: Rs. " + totalOrderedAmount);
                })
                .addOnFailureListener(e -> {
                    ordersContainer.removeAllViews();
                    ordersContainer.addView(createMessageText("Failed to load your orders."));
                    Toast.makeText(this, "Order loading error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private TextView createMessageText(String message) {
        TextView text = new TextView(this);
        text.setText(message);
        text.setTextSize(15);
        text.setTextColor(0xFF1F2937);
        text.setPadding(16, 16, 16, 16);
        text.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        return text;
    }

    private void addOrderCard(int orderNumber, String orderId, String productName, String categoryName,
                              String material, int quantity, int totalPrice, String orderStatus, String paymentStatus) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setClickable(true);
        card.setElevation(3f);

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
        title.setTextColor(0xFF123E91);

        TextView product = new TextView(this);
        product.setText("Product: " + productName);
        product.setTextSize(15);
        product.setTextColor(0xFF1F2937);
        product.setPadding(0, 8, 0, 0);

        TextView category = new TextView(this);
        category.setText("Category: " + categoryName);
        category.setTextSize(15);
        category.setTextColor(0xFF1F2937);

        TextView materialText = new TextView(this);
        materialText.setText("Material: " + material);
        materialText.setTextSize(15);
        materialText.setTextColor(0xFF1F2937);

        TextView quantityText = new TextView(this);
        quantityText.setText("Quantity: " + quantity);
        quantityText.setTextSize(15);
        quantityText.setTextColor(0xFF1F2937);

        TextView priceText = new TextView(this);
        priceText.setText("Price: Rs. " + totalPrice);
        priceText.setTextSize(16);
        priceText.setTypeface(null, Typeface.BOLD);
        priceText.setTextColor(0xFFFF9800);
        priceText.setPadding(0, 6, 0, 0);

        TextView statusText = new TextView(this);
        statusText.setText("Status: " + orderStatus + "  -  Payment: " + paymentStatus);
        statusText.setTextSize(14);
        statusText.setTypeface(null, Typeface.BOLD);
        statusText.setTextColor(0xFF123E91);
        statusText.setPadding(0, 6, 0, 0);

        TextView tapText = new TextView(this);
        tapText.setText("Tap to view full details");
        tapText.setTextSize(13);
        tapText.setTextColor(0xFF6B7280);
        tapText.setPadding(0, 8, 0, 0);

        card.addView(title);
        card.addView(product);
        card.addView(category);
        card.addView(materialText);
        card.addView(quantityText);
        card.addView(priceText);
        card.addView(statusText);
        card.addView(tapText);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, OrderTrackingActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        ordersContainer.addView(card);
    }
}
