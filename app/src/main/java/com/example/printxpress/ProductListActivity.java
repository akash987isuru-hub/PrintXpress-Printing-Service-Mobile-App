package com.example.printxpress;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    TextView tvCategoryTitle;
    LinearLayout productsContainer;
    Button btnBackHome;

    FirebaseFirestore db;
    String categoryName;

    private static class ProductData {
        String productName;
        String description;
        String material;
        String size;
        int basePrice;

        ProductData(String productName, String description, String material, String size, int basePrice) {
            this.productName = productName;
            this.description = description;
            this.material = material;
            this.size = size;
            this.basePrice = basePrice;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        productsContainer = findViewById(R.id.productsContainer);
        btnBackHome = findViewById(R.id.btnBackHome);

        db = FirebaseFirestore.getInstance();
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryName == null || categoryName.isEmpty()) categoryName = "Products";
        tvCategoryTitle.setText(categoryName);

        loadProductsByCategory();

        btnBackHome.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void loadProductsByCategory() {
        productsContainer.removeAllViews();
        productsContainer.addView(createInfoText("Loading products..."));

        db.collection("products")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productsContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        productsContainer.addView(createInfoText("No products available under " + categoryName + "."));
                        return;
                    }

                    List<ProductData> productList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String productName = document.getString("productName");
                        String description = document.getString("description");
                        String material = document.getString("material");
                        String size = document.getString("size");
                        Long basePriceLong = document.getLong("basePrice");
                        int basePrice = basePriceLong == null ? 0 : basePriceLong.intValue();

                        if (productName == null || productName.isEmpty()) productName = "Product";
                        if (description == null || description.isEmpty()) description = "Printing product";
                        if (material == null || material.isEmpty()) material = "Standard";
                        if (size == null || size.isEmpty()) size = "Custom";

                        productList.add(new ProductData(productName, description, material, size, basePrice));
                    }

                    Collections.sort(productList, (p1, p2) -> p1.productName.compareToIgnoreCase(p2.productName));

                    for (ProductData product : productList) {
                        addProductCard(product.productName, product.description, product.basePrice, product.material, product.size);
                    }
                })
                .addOnFailureListener(e -> {
                    productsContainer.removeAllViews();
                    productsContainer.addView(createInfoText("Failed to load products."));
                    Toast.makeText(this, "Product loading error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private TextView createInfoText(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(0xFF1F2937);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        return textView;
    }

    private void addProductCard(String productName, String description, int basePrice, String material, String size) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(22), dp(18), dp(22));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setClickable(true);
        card.setElevation(3f);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 30);
        card.setLayoutParams(cardParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView productImage = new ImageView(this);
        productImage.setImageResource(getCategoryImageResource(categoryName));
        productImage.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_icon_box));
        productImage.setPadding(dp(6), dp(6), dp(6), dp(6));
        productImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        topRow.addView(productImage, new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        titleParams.setMargins(14, 0, 8, 0);

        TextView title = new TextView(this);
        title.setText(productName);
        title.setTextSize(19);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF123E91);
        title.setMaxLines(2);

        TextView price = new TextView(this);
        price.setText("Starting from Rs. " + basePrice);
        price.setTextSize(15);
        price.setTypeface(null, Typeface.BOLD);
        price.setTextColor(0xFFFF9800);
        price.setPadding(0, 4, 0, 0);

        titleLayout.addView(title);
        titleLayout.addView(price);
        topRow.addView(titleLayout, titleParams);

        TextView arrow = new TextView(this);
        arrow.setText(">");
        arrow.setTextSize(28);
        arrow.setTextColor(0xFF123E91);
        arrow.setTypeface(null, Typeface.BOLD);
        topRow.addView(arrow);

        TextView productInfo = new TextView(this);
        productInfo.setText("Material: " + material + "  |  Size: " + size);
        productInfo.setTextSize(13);
        productInfo.setTextColor(0xFF6B7280);
        productInfo.setPadding(0, 12, 0, 0);
        productInfo.setMaxLines(2);

        TextView descText = new TextView(this);
        descText.setText(description);
        descText.setTextSize(14);
        descText.setTextColor(0xFF1F2937);
        descText.setPadding(0, 6, 0, 0);
        descText.setMaxLines(2);

        TextView actionText = new TextView(this);
        actionText.setText("Tap to customize and order");
        actionText.setTextSize(13);
        actionText.setTypeface(null, Typeface.BOLD);
        actionText.setTextColor(0xFF123E91);
        actionText.setPadding(0, 12, 0, 0);

        card.addView(topRow);
        card.addView(productInfo);
        card.addView(descText);
        card.addView(actionText);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListActivity.this, ProductDetailsActivity.class);
            intent.putExtra("categoryName", categoryName);
            intent.putExtra("productName", productName);
            intent.putExtra("productPrice", "Starting from Rs. " + basePrice);
            intent.putExtra("description", description);
            intent.putExtra("material", material);
            intent.putExtra("size", size);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        productsContainer.addView(card);
    }

    private int getCategoryImageResource(String categoryName) {
        if (categoryName == null) return R.drawable.cat_business_cards;

        String name = categoryName.toLowerCase();

        if (name.contains("business")) return R.drawable.cat_business_cards;
        if (name.contains("flyer")) return R.drawable.cat_flyers;
        if (name.contains("poster")) return R.drawable.cat_posters;
        if (name.contains("banner")) return R.drawable.cat_banners;
        if (name.contains("shirt")) return R.drawable.cat_tshirts;
        if (name.contains("mug")) return R.drawable.cat_mugs;
        if (name.contains("sticker")) return R.drawable.cat_stickers;
        if (name.contains("invitation")) return R.drawable.cat_invitations;
        if (name.contains("brochure")) return R.drawable.cat_brochures;
        if (name.contains("certificate")) return R.drawable.cat_certificates;
        if (name.contains("id")) return R.drawable.cat_id_cards;
        if (name.contains("letter")) return R.drawable.cat_letterheads;
        if (name.contains("envelope")) return R.drawable.cat_envelopes;
        if (name.contains("calendar")) return R.drawable.cat_calendars;
        if (name.contains("booklet")) return R.drawable.cat_booklets;

        return R.drawable.cat_business_cards;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductsByCategory();
    }
}
