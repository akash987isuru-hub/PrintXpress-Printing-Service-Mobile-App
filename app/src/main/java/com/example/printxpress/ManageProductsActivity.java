package com.example.printxpress;

import android.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageProductsActivity extends AppCompatActivity {

    EditText etProductName, etProductCategory, etProductDescription, etBasePrice, etMaterial, etSize;
    Button btnAddProduct, btnBackAdminFromProducts;
    LinearLayout productsContainer;

    FirebaseFirestore db;

    private static class ProductData {
        String productId;
        String productName;
        String categoryName;
        String description;
        String material;
        String size;
        int basePrice;

        ProductData(String productId, String productName, String categoryName,
                    String description, String material, String size, int basePrice) {
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.description = description;
            this.material = material;
            this.size = size;
            this.basePrice = basePrice;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        etProductName = findViewById(R.id.etProductName);
        etProductCategory = findViewById(R.id.etProductCategory);
        etProductDescription = findViewById(R.id.etProductDescription);
        etBasePrice = findViewById(R.id.etBasePrice);
        etMaterial = findViewById(R.id.etMaterial);
        etSize = findViewById(R.id.etSize);

        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnBackAdminFromProducts = findViewById(R.id.btnBackAdminFromProducts);
        productsContainer = findViewById(R.id.productsContainer);

        db = FirebaseFirestore.getInstance();

        loadProducts();

        btnAddProduct.setOnClickListener(v -> addProduct());


        btnBackAdminFromProducts.setOnClickListener(v -> finish());
    }

    private void addProduct() {
        String productName = etProductName.getText().toString().trim();
        String categoryName = etProductCategory.getText().toString().trim();
        String description = etProductDescription.getText().toString().trim();
        String basePriceText = etBasePrice.getText().toString().trim();
        String material = etMaterial.getText().toString().trim();
        String size = etSize.getText().toString().trim();

        if (TextUtils.isEmpty(productName)) {
            etProductName.setError("Product name is required");
            return;
        }

        if (TextUtils.isEmpty(categoryName)) {
            etProductCategory.setError("Category name is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etProductDescription.setError("Description is required");
            return;
        }

        if (TextUtils.isEmpty(basePriceText)) {
            etBasePrice.setError("Base price is required");
            return;
        }

        if (TextUtils.isEmpty(material)) {
            etMaterial.setError("Material is required");
            return;
        }

        if (TextUtils.isEmpty(size)) {
            etSize.setError("Size is required");
            return;
        }

        int basePrice;

        try {
            basePrice = Integer.parseInt(basePriceText);
        } catch (Exception e) {
            etBasePrice.setError("Enter a valid price");
            return;
        }

        if (basePrice <= 0) {
            etBasePrice.setError("Price must be greater than 0");
            return;
        }

        Map<String, Object> product = new HashMap<>();
        product.put("productName", productName);
        product.put("categoryName", categoryName);
        product.put("description", description);
        product.put("basePrice", basePrice);
        product.put("material", material);
        product.put("size", size);
        product.put("createdAt", System.currentTimeMillis());

        db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();

                    etProductName.setText("");
                    etProductCategory.setText("");
                    etProductDescription.setText("");
                    etBasePrice.setText("");
                    etMaterial.setText("");
                    etSize.setText("");

                    loadProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add product: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadProducts() {
        productsContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading products...");
        loadingText.setTextSize(15);
        loadingText.setTextColor(0xFF6B7280);
        loadingText.setPadding(16, 16, 16, 16);
        loadingText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        productsContainer.addView(loadingText);

        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productsContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptyText = new TextView(this);
                        emptyText.setText("No products added yet.");
                        emptyText.setTextSize(15);
                        emptyText.setTextColor(0xFF1F2937);
                        emptyText.setPadding(16, 16, 16, 16);
                        emptyText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                        productsContainer.addView(emptyText);
                        return;
                    }

                    List<ProductData> productList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String productId = document.getId();
                        String productName = document.getString("productName");
                        String categoryName = document.getString("categoryName");
                        String description = document.getString("description");
                        String material = document.getString("material");
                        String size = document.getString("size");

                        Long basePriceLong = document.getLong("basePrice");
                        int basePrice = basePriceLong == null ? 0 : basePriceLong.intValue();

                        if (productName == null) productName = "";
                        if (categoryName == null) categoryName = "";
                        if (description == null) description = "";
                        if (material == null) material = "";
                        if (size == null) size = "";

                        productList.add(new ProductData(
                                productId,
                                productName,
                                categoryName,
                                description,
                                material,
                                size,
                                basePrice
                        ));
                    }

                    Collections.sort(productList, (p1, p2) -> {
                        int categoryCompare = getCategoryOrder(p1.categoryName) - getCategoryOrder(p2.categoryName);

                        if (categoryCompare != 0) {
                            return categoryCompare;
                        }

                        return p1.productName.compareToIgnoreCase(p2.productName);
                    });

                    String currentCategory = "";
                    int count = 1;

                    for (ProductData product : productList) {
                        if (!product.categoryName.equalsIgnoreCase(currentCategory)) {
                            currentCategory = product.categoryName;
                            addCategoryHeader(currentCategory);
                        }

                        addProductCard(
                                count,
                                product.productId,
                                product.productName,
                                product.categoryName,
                                product.description,
                                product.basePrice,
                                product.material,
                                product.size
                        );

                        count++;
                    }
                })
                .addOnFailureListener(e -> {
                    productsContainer.removeAllViews();

                    TextView errorText = new TextView(this);
                    errorText.setText("Failed to load products.");
                    errorText.setTextSize(15);
                    errorText.setTextColor(0xFF1F2937);
                    errorText.setPadding(16, 16, 16, 16);
                    errorText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                    productsContainer.addView(errorText);

                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private int getCategoryOrder(String categoryName) {
        if (categoryName == null) return 999;

        String name = categoryName.toLowerCase();

        if (name.equals("business cards")) return 1;
        if (name.equals("flyers")) return 2;
        if (name.equals("posters")) return 3;
        if (name.equals("banners")) return 4;
        if (name.equals("t-shirts")) return 5;
        if (name.equals("mugs")) return 6;
        if (name.equals("stickers")) return 7;
        if (name.equals("invitations")) return 8;
        if (name.equals("brochures")) return 9;
        if (name.equals("certificates")) return 10;
        if (name.equals("id cards")) return 11;
        if (name.equals("letterheads")) return 12;
        if (name.equals("envelopes")) return 13;
        if (name.equals("calendars")) return 14;
        if (name.equals("booklets")) return 15;

        return 999;
    }

    private void addCategoryHeader(String categoryName) {
        TextView header = new TextView(this);

        if (categoryName == null || categoryName.isEmpty()) {
            categoryName = "Other Products";
        }

        header.setText(categoryName);
        header.setTextSize(20);
        header.setTypeface(null, Typeface.BOLD);
        header.setTextColor(0xFF123E91);
        header.setPadding(4, 20, 4, 10);

        productsContainer.addView(header);
    }

    private void addProductCard(int number, String productId, String productName, String categoryName,
                                String description, int basePrice, String material, String size) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setElevation(3f);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 14);
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText(number + ". " + productName);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF123E91);

        TextView category = new TextView(this);
        category.setText("Category: " + categoryName);
        category.setTextSize(15);
        category.setTextColor(0xFF1F2937);
        category.setPadding(0, 8, 0, 0);

        TextView price = new TextView(this);
        price.setText("Base Price: Rs. " + basePrice);
        price.setTextSize(15);
        price.setTypeface(null, Typeface.BOLD);
        price.setTextColor(0xFFFF9800);
        price.setPadding(0, 6, 0, 0);

        TextView materialText = new TextView(this);
        materialText.setText("Material: " + material);
        materialText.setTextSize(15);
        materialText.setTextColor(0xFF1F2937);

        TextView sizeText = new TextView(this);
        sizeText.setText("Size: " + size);
        sizeText.setTextSize(15);
        sizeText.setTextColor(0xFF1F2937);

        TextView descriptionText = new TextView(this);
        descriptionText.setText("Description: " + description);
        descriptionText.setTextSize(14);
        descriptionText.setTextColor(0xFF555555);
        descriptionText.setPadding(0, 6, 0, 0);

        TextView idText = new TextView(this);
        idText.setText("Product ID: " + productId);
        idText.setTextSize(12);
        idText.setTextColor(0xFF777777);
        idText.setPadding(0, 8, 0, 8);

        Button btnEditProduct = new Button(this);
        btnEditProduct.setText("Edit Product");
        btnEditProduct.setTextColor(0xFFFFFFFF);
        btnEditProduct.setTextSize(14);
        btnEditProduct.setTypeface(null, Typeface.BOLD);
        btnEditProduct.setAllCaps(false);
        btnEditProduct.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_primary_button));

        LinearLayout.LayoutParams editButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        editButtonParams.setMargins(0, 10, 0, 0);
        btnEditProduct.setLayoutParams(editButtonParams);

        btnEditProduct.setOnClickListener(v -> {
            showEditProductDialog(productId, productName, categoryName, basePrice, material, size);
        });

        Button btnDeleteProduct = new Button(this);
        btnDeleteProduct.setText("Delete Product");
        btnDeleteProduct.setTextColor(0xFFFFFFFF);
        btnDeleteProduct.setTextSize(14);
        btnDeleteProduct.setTypeface(null, Typeface.BOLD);
        btnDeleteProduct.setAllCaps(false);
        btnDeleteProduct.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_danger_button));

        LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        deleteButtonParams.setMargins(0, 10, 0, 0);
        btnDeleteProduct.setLayoutParams(deleteButtonParams);

        btnDeleteProduct.setOnClickListener(v -> {
            db.collection("products").document(productId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete product: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        card.addView(title);
        card.addView(category);
        card.addView(price);
        card.addView(materialText);
        card.addView(sizeText);
        card.addView(descriptionText);
        card.addView(idText);
        card.addView(btnEditProduct);
        card.addView(btnDeleteProduct);

        productsContainer.addView(card);
    }

    private void showEditProductDialog(String productId, String oldProductName, String oldCategoryName,
                                       int oldBasePrice, String oldMaterial, String oldSize) {

        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(30, 20, 30, 10);

        TextView productInfo = new TextView(this);
        productInfo.setText("Product: " + oldProductName + "\nCategory: " + oldCategoryName);
        productInfo.setTextSize(15);
        productInfo.setTypeface(null, Typeface.BOLD);
        productInfo.setTextColor(0xFF123E91);
        productInfo.setPadding(0, 0, 0, 16);

        EditText editBasePrice = new EditText(this);
        editBasePrice.setHint("Base Price");
        editBasePrice.setText(String.valueOf(oldBasePrice));
        editBasePrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        EditText editMaterial = new EditText(this);
        editMaterial.setHint("Material");
        editMaterial.setText(oldMaterial);
        editMaterial.setSingleLine(false);

        EditText editSize = new EditText(this);
        editSize.setHint("Size");
        editSize.setText(oldSize);
        editSize.setSingleLine(false);

        dialogLayout.addView(productInfo);
        dialogLayout.addView(editBasePrice);
        dialogLayout.addView(editMaterial);
        dialogLayout.addView(editSize);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Product Details")
                .setView(dialogLayout)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            updateButton.setOnClickListener(v -> {
                String basePriceText = editBasePrice.getText().toString().trim();
                String material = editMaterial.getText().toString().trim();
                String size = editSize.getText().toString().trim();

                if (TextUtils.isEmpty(basePriceText)) {
                    editBasePrice.setError("Base price is required");
                    return;
                }

                if (TextUtils.isEmpty(material)) {
                    editMaterial.setError("Material is required");
                    return;
                }

                if (TextUtils.isEmpty(size)) {
                    editSize.setError("Size is required");
                    return;
                }

                int basePrice;

                try {
                    basePrice = Integer.parseInt(basePriceText);
                } catch (Exception e) {
                    editBasePrice.setError("Enter a valid price");
                    return;
                }

                if (basePrice <= 0) {
                    editBasePrice.setError("Price must be greater than 0");
                    return;
                }

                updateProduct(productId, basePrice, material, size);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void updateProduct(String productId, int basePrice, String material, String size) {

        Map<String, Object> updatedProduct = new HashMap<>();
        updatedProduct.put("basePrice", basePrice);
        updatedProduct.put("material", material);
        updatedProduct.put("size", size);
        updatedProduct.put("updatedAt", System.currentTimeMillis());

        db.collection("products").document(productId)
                .update(updatedProduct)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    loadProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update product: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}