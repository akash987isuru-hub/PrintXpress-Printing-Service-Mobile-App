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
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ReviewDesignsActivity extends AppCompatActivity {

    LinearLayout designsContainer;
    Button btnRefreshDesigns, btnBackAdminFromDesigns;

    FirebaseFirestore db;

    int designCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_designs);

        designsContainer = findViewById(R.id.designsContainer);
        btnRefreshDesigns = findViewById(R.id.btnRefreshDesigns);
        btnBackAdminFromDesigns = findViewById(R.id.btnBackAdminFromDesigns);

        db = FirebaseFirestore.getInstance();

        loadDesignOrders();

        btnRefreshDesigns.setOnClickListener(v -> {
            loadDesignOrders();
            Toast.makeText(this, "Designs refreshed", Toast.LENGTH_SHORT).show();
        });

        btnBackAdminFromDesigns.setOnClickListener(v -> finish());
    }

    private void loadDesignOrders() {
        designsContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading uploaded designs...");
        loadingText.setTextSize(15);
        loadingText.setTextColor(0xFF666666);
        loadingText.setPadding(16, 16, 16, 16);
        designsContainer.addView(loadingText);

        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    designsContainer.removeAllViews();
                    designCount = 0;

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyMessage("No customer orders available.");
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String orderId = document.getId();

                        String productName = document.getString("productName");
                        String categoryName = document.getString("categoryName");
                        String designFileName = document.getString("designFileName");
                        String instructions = document.getString("instructions");
                        String orderStatus = document.getString("orderStatus");
                        String userId = document.getString("userId");

                        if (productName == null) productName = "";
                        if (categoryName == null) categoryName = "";
                        if (designFileName == null || designFileName.isEmpty()) designFileName = "No file selected";
                        if (instructions == null || instructions.isEmpty()) instructions = "No instructions provided";
                        if (orderStatus == null) orderStatus = "Pending";
                        if (userId == null) userId = "";

                        designCount++;

                        addDesignCard(
                                designCount,
                                orderId,
                                userId,
                                productName,
                                categoryName,
                                designFileName,
                                instructions,
                                orderStatus
                        );
                    }

                    if (designCount == 0) {
                        showEmptyMessage("No uploaded design details found.");
                    }
                })
                .addOnFailureListener(e -> {
                    designsContainer.removeAllViews();
                    showEmptyMessage("Failed to load uploaded designs.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showEmptyMessage(String message) {
        TextView emptyText = new TextView(this);
        emptyText.setText(message);
        emptyText.setTextSize(15);
        emptyText.setTextColor(0xFF222222);
        emptyText.setPadding(16, 16, 16, 16);
        emptyText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        designsContainer.addView(emptyText);
    }

    private void addDesignCard(int number, String orderId, String userId, String productName,
                               String categoryName, String designFileName,
                               String instructions, String orderStatus) {

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
        title.setText("Design Review " + number);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF0B3D91);

        TextView orderText = new TextView(this);
        orderText.setText("Order ID: " + orderId);
        orderText.setTextSize(12);
        orderText.setTextColor(0xFF777777);
        orderText.setPadding(0, 8, 0, 0);

        TextView customerText = new TextView(this);
        customerText.setText("Customer ID: " + userId);
        customerText.setTextSize(12);
        customerText.setTextColor(0xFF777777);

        TextView productText = new TextView(this);
        productText.setText("Product: " + productName);
        productText.setTextSize(15);
        productText.setTextColor(0xFF222222);
        productText.setPadding(0, 8, 0, 0);

        TextView categoryText = new TextView(this);
        categoryText.setText("Category: " + categoryName);
        categoryText.setTextSize(15);
        categoryText.setTextColor(0xFF222222);

        TextView fileText = new TextView(this);
        fileText.setText("Design File: " + designFileName);
        fileText.setTextSize(15);
        fileText.setTypeface(null, Typeface.BOLD);
        fileText.setTextColor(0xFFFF9800);
        fileText.setPadding(0, 8, 0, 0);

        TextView instructionText = new TextView(this);
        instructionText.setText("Instructions: " + instructions);
        instructionText.setTextSize(15);
        instructionText.setTextColor(0xFF222222);
        instructionText.setPadding(0, 8, 0, 0);

        TextView statusText = new TextView(this);
        statusText.setText("Order Status: " + orderStatus);
        statusText.setTextSize(15);
        statusText.setTypeface(null, Typeface.BOLD);
        statusText.setTextColor(0xFF0B3D91);
        statusText.setPadding(0, 8, 0, 0);

        TextView noteText = new TextView(this);
        noteText.setText("Design details only");
        noteText.setTextSize(13);
        noteText.setTextColor(0xFF666666);
        noteText.setPadding(0, 8, 0, 0);

        card.addView(title);
        card.addView(orderText);
        card.addView(customerText);
        card.addView(productText);
        card.addView(categoryText);
        card.addView(fileText);
        card.addView(instructionText);
        card.addView(statusText);
        card.addView(noteText);


        designsContainer.addView(card);
    }
}