package com.example.printxpress;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ManagePromotionsActivity extends AppCompatActivity {

    EditText etPromotionTitle, etPromotionDescription, etDiscount, etValidUntil;
    Button btnAddPromotion, btnRefreshPromotions, btnBackAdminFromPromotions;
    LinearLayout promotionsContainer;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_promotions);

        etPromotionTitle = findViewById(R.id.etPromotionTitle);
        etPromotionDescription = findViewById(R.id.etPromotionDescription);
        etDiscount = findViewById(R.id.etDiscount);
        etValidUntil = findViewById(R.id.etValidUntil);

        btnAddPromotion = findViewById(R.id.btnAddPromotion);
        btnRefreshPromotions = findViewById(R.id.btnRefreshPromotions);
        btnBackAdminFromPromotions = findViewById(R.id.btnBackAdminFromPromotions);
        promotionsContainer = findViewById(R.id.promotionsContainer);

        db = FirebaseFirestore.getInstance();

        loadPromotions();

        btnAddPromotion.setOnClickListener(v -> addPromotion());

        btnRefreshPromotions.setOnClickListener(v -> {
            loadPromotions();
            Toast.makeText(this, "Promotions refreshed", Toast.LENGTH_SHORT).show();
        });

        btnBackAdminFromPromotions.setOnClickListener(v -> finish());
    }

    private void addPromotion() {
        String title = etPromotionTitle.getText().toString().trim();
        String description = etPromotionDescription.getText().toString().trim();
        String discountText = etDiscount.getText().toString().trim();
        String validUntil = etValidUntil.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etPromotionTitle.setError("Promotion title is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etPromotionDescription.setError("Promotion description is required");
            return;
        }

        if (TextUtils.isEmpty(discountText)) {
            etDiscount.setError("Discount is required");
            return;
        }

        if (TextUtils.isEmpty(validUntil)) {
            etValidUntil.setError("Valid until date is required");
            return;
        }

        int discount;

        try {
            discount = Integer.parseInt(discountText);
        } catch (Exception e) {
            etDiscount.setError("Enter a valid discount");
            return;
        }

        if (discount <= 0 || discount > 100) {
            etDiscount.setError("Discount must be between 1 and 100");
            return;
        }

        Map<String, Object> promotion = new HashMap<>();
        promotion.put("title", title);
        promotion.put("description", description);
        promotion.put("discount", discount);
        promotion.put("validUntil", validUntil);
        promotion.put("createdAt", System.currentTimeMillis());

        db.collection("promotions")
                .add(promotion)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Promotion added successfully", Toast.LENGTH_SHORT).show();

                    etPromotionTitle.setText("");
                    etPromotionDescription.setText("");
                    etDiscount.setText("");
                    etValidUntil.setText("");

                    loadPromotions();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add promotion: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadPromotions() {
        promotionsContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading promotions...");
        loadingText.setTextSize(15);
        loadingText.setTextColor(0xFF666666);
        loadingText.setPadding(16, 16, 16, 16);
        promotionsContainer.addView(loadingText);

        db.collection("promotions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    promotionsContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptyText = new TextView(this);
                        emptyText.setText("No promotions added yet.");
                        emptyText.setTextSize(15);
                        emptyText.setTextColor(0xFF222222);
                        emptyText.setPadding(16, 16, 16, 16);
                        emptyText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                        promotionsContainer.addView(emptyText);
                        return;
                    }

                    int count = 1;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String promotionId = document.getId();
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String validUntil = document.getString("validUntil");

                        Long discountLong = document.getLong("discount");
                        int discount = discountLong == null ? 0 : discountLong.intValue();

                        if (title == null) title = "";
                        if (description == null) description = "";
                        if (validUntil == null) validUntil = "";

                        addPromotionCard(count, promotionId, title, description, discount, validUntil);
                        count++;
                    }
                })
                .addOnFailureListener(e -> {
                    promotionsContainer.removeAllViews();

                    TextView errorText = new TextView(this);
                    errorText.setText("Failed to load promotions.");
                    errorText.setTextSize(15);
                    errorText.setTextColor(0xFF222222);
                    errorText.setPadding(16, 16, 16, 16);
                    errorText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                    promotionsContainer.addView(errorText);

                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addPromotionCard(int number, String promotionId, String title,
                                  String description, int discount, String validUntil) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 14);
        card.setLayoutParams(cardParams);

        TextView titleText = new TextView(this);
        titleText.setText(number + ". " + title);
        titleText.setTextSize(18);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setTextColor(0xFF0B3D91);

        TextView descText = new TextView(this);
        descText.setText("Description: " + description);
        descText.setTextSize(15);
        descText.setTextColor(0xFF222222);
        descText.setPadding(0, 8, 0, 0);

        TextView discountText = new TextView(this);
        discountText.setText("Discount: " + discount + "%");
        discountText.setTextSize(16);
        discountText.setTypeface(null, Typeface.BOLD);
        discountText.setTextColor(0xFFFF9800);
        discountText.setPadding(0, 6, 0, 0);

        TextView validText = new TextView(this);
        validText.setText("Valid Until: " + validUntil);
        validText.setTextSize(15);
        validText.setTextColor(0xFF222222);

        TextView idText = new TextView(this);
        idText.setText("Promotion ID: " + promotionId);
        idText.setTextSize(12);
        idText.setTextColor(0xFF777777);
        idText.setPadding(0, 8, 0, 8);

        Button btnDeletePromotion = new Button(this);
        btnDeletePromotion.setText("Delete Promotion");
        btnDeletePromotion.setTextColor(0xFFFFFFFF);
        btnDeletePromotion.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_danger_button));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 10, 0, 0);
        btnDeletePromotion.setLayoutParams(buttonParams);

        btnDeletePromotion.setOnClickListener(v -> {
            db.collection("promotions").document(promotionId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Promotion deleted successfully", Toast.LENGTH_SHORT).show();
                        loadPromotions();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete promotion: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        card.addView(titleText);
        card.addView(descText);
        card.addView(discountText);
        card.addView(validText);
        card.addView(idText);
        card.addView(btnDeletePromotion);

        promotionsContainer.addView(card);
    }
}