package com.example.printxpress;

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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminRevenueActivity extends AppCompatActivity {

    TextView tvTotalRevenue, tvTotalOrders, tvRevenueBreakdown;
    LinearLayout revenueOrdersContainer;
    Button btnBackAdminFromRevenue;

    FirebaseFirestore db;
    ListenerRegistration orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_revenue);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvRevenueBreakdown = findViewById(R.id.tvRevenueBreakdown);
        revenueOrdersContainer = findViewById(R.id.revenueOrdersContainer);
        btnBackAdminFromRevenue = findViewById(R.id.btnBackAdminFromRevenue);

        db = FirebaseFirestore.getInstance();

        listenRevenue();

        btnBackAdminFromRevenue.setOnClickListener(v -> finish());
    }

    private void listenRevenue() {
        revenueOrdersContainer.removeAllViews();
        revenueOrdersContainer.addView(createInfoText("Loading revenue summary..."));

        orderListener = db.collection("orders")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        revenueOrdersContainer.removeAllViews();
                        revenueOrdersContainer.addView(createInfoText("Failed to load revenue details."));
                        Toast.makeText(this, "Revenue loading error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    revenueOrdersContainer.removeAllViews();

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        tvTotalRevenue.setText("Total Revenue\nRs. 0");
                        tvTotalOrders.setText("Total Orders: 0");
                        tvRevenueBreakdown.setText("No orders received yet.");
                        revenueOrdersContainer.addView(createInfoText("No order records available."));
                        return;
                    }

                    int totalOrders = 0;
                    int totalRevenue = 0;

                    int pending = 0;
                    int processing = 0;
                    int ready = 0;
                    int complete = 0;
                    int cancelled = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        totalOrders++;

                        Long priceLong = document.getLong("totalPrice");
                        int totalPrice = priceLong == null ? 0 : priceLong.intValue();
                        totalRevenue += totalPrice;

                        String status = document.getString("orderStatus");
                        if (status == null || status.trim().isEmpty()) {
                            status = "Pending";
                        }

                        if (status.equalsIgnoreCase("Pending")) {
                            pending++;
                        } else if (status.equalsIgnoreCase("Processing")) {
                            processing++;
                        } else if (status.equalsIgnoreCase("Ready")) {
                            ready++;
                        } else if (status.equalsIgnoreCase("Complete") || status.equalsIgnoreCase("Completed")) {
                            complete++;
                        } else if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Canceled") || status.equalsIgnoreCase("Cancel")) {
                            cancelled++;
                        }

                        String productName = document.getString("productName");
                        String categoryName = document.getString("categoryName");

                        if (productName == null || productName.trim().isEmpty()) {
                            productName = "Product";
                        }

                        if (categoryName == null || categoryName.trim().isEmpty()) {
                            categoryName = "Category";
                        }

                        addOrderCard(document.getId(), productName, categoryName, status, totalPrice);
                    }

                    tvTotalRevenue.setText("Total Revenue\nRs. " + totalRevenue);
                    tvTotalOrders.setText("Total Orders: " + totalOrders);

                    String breakdown = "Pending Orders: " + pending + "\n"
                            + "Processing Orders: " + processing + "\n"
                            + "Ready Orders: " + ready + "\n"
                            + "Complete Orders: " + complete + "\n"
                            + "Cancelled Orders: " + cancelled;

                    tvRevenueBreakdown.setText(breakdown);
                });
    }

    private void addOrderCard(String orderId, String productName, String categoryName, String status, int totalPrice) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setElevation(3f);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText(productName);
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF123E91);

        TextView details = new TextView(this);
        details.setText("Category: " + categoryName + "\n"
                + "Order ID: " + orderId + "\n"
                + "Status: " + status + "\n"
                + "Amount: Rs. " + totalPrice);
        details.setTextSize(13);
        details.setTextColor(0xFF1F2937);
        details.setPadding(0, dp(6), 0, 0);
        details.setLineSpacing(2f, 1f);

        card.addView(title);
        card.addView(details);

        revenueOrdersContainer.addView(card);
    }

    private TextView createInfoText(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(0xFF1F2937);
        textView.setPadding(dp(16), dp(16), dp(16), dp(16));
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        return textView;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
