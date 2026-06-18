package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProductDetailsActivity extends AppCompatActivity {

    TextView tvProductName, tvCategoryName, tvProductPrice, tvDescription;
    TextView tvSizeExtra, tvMaterialExtra, tvUnitPrice, tvTotalPrice;
    Spinner spinnerSize, spinnerMaterial;
    EditText etQuantity;
    Button btnCalculateTotal, btnNextUpload, btnBackProducts;

    FirebaseFirestore db;

    String categoryName, productName, productPrice, description, defaultMaterial, defaultSize;
    int basePrice = 0;
    int finalUnitPrice = 0;
    int totalPrice = 0;

    ArrayList<String> sizeList = new ArrayList<>();
    ArrayList<String> materialList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        tvProductName = findViewById(R.id.tvProductName);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvDescription = findViewById(R.id.tvDescription);

        tvSizeExtra = findViewById(R.id.tvSizeExtra);
        tvMaterialExtra = findViewById(R.id.tvMaterialExtra);
        tvUnitPrice = findViewById(R.id.tvUnitPrice);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        spinnerSize = findViewById(R.id.spinnerSize);
        spinnerMaterial = findViewById(R.id.spinnerMaterial);

        etQuantity = findViewById(R.id.etQuantity);

        btnCalculateTotal = findViewById(R.id.btnCalculateTotal);
        btnNextUpload = findViewById(R.id.btnNextUpload);
        btnBackProducts = findViewById(R.id.btnBackProducts);

        db = FirebaseFirestore.getInstance();

        getIntentData();
        displayProductData();
        loadCategoryOptions();

        btnCalculateTotal.setOnClickListener(v -> calculateTotal());

        btnNextUpload.setOnClickListener(v -> goToUploadDesign());

        btnBackProducts.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void getIntentData() {
        categoryName = getIntent().getStringExtra("categoryName");
        productName = getIntent().getStringExtra("productName");
        productPrice = getIntent().getStringExtra("productPrice");
        description = getIntent().getStringExtra("description");
        defaultMaterial = getIntent().getStringExtra("material");
        defaultSize = getIntent().getStringExtra("size");

        if (categoryName == null) categoryName = "";
        if (productName == null) productName = "";
        if (productPrice == null) productPrice = "";
        if (description == null) description = "";
        if (defaultMaterial == null) defaultMaterial = "";
        if (defaultSize == null) defaultSize = "";

        basePrice = extractPrice(productPrice);
        finalUnitPrice = basePrice;
    }

    private void displayProductData() {
        tvProductName.setText(productName);
        tvCategoryName.setText(categoryName);
        tvProductPrice.setText("Starting from Rs. " + basePrice);
        tvDescription.setText(description);
        tvUnitPrice.setText("Unit Price: Rs. " + finalUnitPrice);
        tvTotalPrice.setText("Total Price: Rs. 0");
    }

    private int extractPrice(String priceText) {
        if (priceText == null) return 0;

        String onlyNumbers = priceText.replaceAll("[^0-9]", "");

        if (onlyNumbers.isEmpty()) return 0;

        try {
            return Integer.parseInt(onlyNumbers);
        } catch (Exception e) {
            return 0;
        }
    }

    private void loadCategoryOptions() {
        Set<String> sizeSet = new LinkedHashSet<>();
        Set<String> materialSet = new LinkedHashSet<>();

        if (!defaultSize.isEmpty()) {
            sizeSet.add(defaultSize);
        }

        if (!defaultMaterial.isEmpty()) {
            materialSet.add(defaultMaterial);
        }

        db.collection("products")
                .whereEqualTo("categoryName", categoryName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String size = document.getString("size");
                        String material = document.getString("material");

                        if (size != null && !size.trim().isEmpty()) {
                            sizeSet.add(size.trim());
                        }

                        if (material != null && !material.trim().isEmpty()) {
                            materialSet.add(material.trim());
                        }
                    }

                    sizeList.clear();
                    materialList.clear();

                    sizeList.addAll(sizeSet);
                    materialList.addAll(materialSet);

                    if (sizeList.isEmpty()) {
                        sizeList.add("Standard");
                    }

                    if (materialList.isEmpty()) {
                        materialList.add("Standard");
                    }

                    setSpinnerData();
                })
                .addOnFailureListener(e -> {
                    sizeList.clear();
                    materialList.clear();

                    sizeList.add(defaultSize.isEmpty() ? "Standard" : defaultSize);
                    materialList.add(defaultMaterial.isEmpty() ? "Standard" : defaultMaterial);

                    setSpinnerData();

                    Toast.makeText(this, "Failed to load options: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setSpinnerData() {
        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                sizeList
        );

        ArrayAdapter<String> materialAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                materialList
        );

        spinnerSize.setAdapter(sizeAdapter);
        spinnerMaterial.setAdapter(materialAdapter);

        int sizeIndex = sizeList.indexOf(defaultSize);
        int materialIndex = materialList.indexOf(defaultMaterial);

        if (sizeIndex >= 0) {
            spinnerSize.setSelection(sizeIndex);
        }

        if (materialIndex >= 0) {
            spinnerMaterial.setSelection(materialIndex);
        }
    }

    private void calculateTotal() {
        String quantityText = etQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(quantityText)) {
            etQuantity.setError("Quantity is required");
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (Exception e) {
            etQuantity.setError("Enter a valid quantity");
            return;
        }

        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than 0");
            return;
        }

        String selectedSize = spinnerSize.getSelectedItem().toString();
        String selectedMaterial = spinnerMaterial.getSelectedItem().toString();

        int sizeExtra = getSizeExtraCharge(selectedSize);
        int materialExtra = getMaterialExtraCharge(selectedMaterial);

        finalUnitPrice = basePrice + sizeExtra + materialExtra;
        totalPrice = finalUnitPrice * quantity;

        tvSizeExtra.setText("Size extra charge: Rs. " + sizeExtra);
        tvMaterialExtra.setText("Material extra charge: Rs. " + materialExtra);
        tvUnitPrice.setText("Unit Price: Rs. " + finalUnitPrice);
        tvTotalPrice.setText("Total Price: Rs. " + totalPrice);
    }

    private int getSizeExtraCharge(String selectedSize) {
        if (selectedSize == null) return 0;

        if (selectedSize.equalsIgnoreCase(defaultSize)) {
            return 0;
        }

        String size = selectedSize.toLowerCase();

        if (size.contains("a5")) return 300;
        if (size.contains("a4")) return 600;
        if (size.contains("a3")) return 1200;
        if (size.contains("a2")) return 2000;
        if (size.contains("3ft")) return 2500;
        if (size.contains("6ft")) return 4500;
        if (size.contains("8ft")) return 6000;
        if (size.contains("xl")) return 500;
        if (size.contains("custom")) return 700;
        if (size.contains("standard")) return 0;

        return 300;
    }

    private int getMaterialExtraCharge(String selectedMaterial) {
        if (selectedMaterial == null) return 0;

        if (selectedMaterial.equalsIgnoreCase(defaultMaterial)) {
            return 0;
        }

        String material = selectedMaterial.toLowerCase();

        if (material.contains("premium")) return 700;
        if (material.contains("gloss")) return 500;
        if (material.contains("matte")) return 300;
        if (material.contains("vinyl")) return 700;
        if (material.contains("pvc")) return 600;
        if (material.contains("ceramic")) return 400;
        if (material.contains("cotton")) return 500;
        if (material.contains("flex")) return 800;
        if (material.contains("bond")) return 300;
        if (material.contains("art")) return 400;
        if (material.contains("standard")) return 0;

        return 300;
    }

    private void goToUploadDesign() {
        String quantityText = etQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(quantityText)) {
            etQuantity.setError("Calculate total first");
            return;
        }

        if (totalPrice <= 0) {
            calculateTotal();

            if (totalPrice <= 0) {
                Toast.makeText(this, "Please calculate total before continuing", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String selectedSize = spinnerSize.getSelectedItem().toString();
        String selectedMaterial = spinnerMaterial.getSelectedItem().toString();

        int quantity = Integer.parseInt(quantityText);

        Intent intent = new Intent(ProductDetailsActivity.this, UploadDesignActivity.class);

        intent.putExtra("categoryName", categoryName);
        intent.putExtra("productName", productName);
        intent.putExtra("productPrice", "Starting from Rs. " + finalUnitPrice);
        intent.putExtra("description", description);
        intent.putExtra("material", selectedMaterial);
        intent.putExtra("size", selectedSize);
        intent.putExtra("quantity", quantity);
        intent.putExtra("totalPrice", totalPrice);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}