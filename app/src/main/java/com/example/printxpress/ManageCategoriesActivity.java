package com.example.printxpress;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ManageCategoriesActivity extends AppCompatActivity {

    EditText etCategoryName, etCategoryDescription;
    Button btnAddCategory, btnBackAdminFromCategories;
    LinearLayout categoriesContainer;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        etCategoryName = findViewById(R.id.etCategoryName);
        etCategoryDescription = findViewById(R.id.etCategoryDescription);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        btnBackAdminFromCategories = findViewById(R.id.btnBackAdminFromCategories);
        categoriesContainer = findViewById(R.id.categoriesContainer);

        db = FirebaseFirestore.getInstance();

        loadCategories();

        btnAddCategory.setOnClickListener(v -> addCategory());
        btnBackAdminFromCategories.setOnClickListener(v -> finish());
    }

    private void addCategory() {
        String categoryName = etCategoryName.getText().toString().trim();
        String categoryDescription = etCategoryDescription.getText().toString().trim();

        if (TextUtils.isEmpty(categoryName)) {
            etCategoryName.setError("Category name is required");
            return;
        }

        if (TextUtils.isEmpty(categoryDescription)) {
            etCategoryDescription.setError("Description is required");
            return;
        }

        Map<String, Object> category = new HashMap<>();
        category.put("categoryName", categoryName);
        category.put("description", categoryDescription);
        category.put("createdAt", System.currentTimeMillis());

        db.collection("categories")
                .add(category)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();

                    etCategoryName.setText("");
                    etCategoryDescription.setText("");

                    loadCategories();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add category: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadCategories() {
        categoriesContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Loading categories...");
        loadingText.setTextSize(15);
        loadingText.setTextColor(0xFF666666);
        loadingText.setPadding(16, 16, 16, 16);
        categoriesContainer.addView(loadingText);

        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoriesContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptyText = new TextView(this);
                        emptyText.setText("No categories added yet.");
                        emptyText.setTextSize(15);
                        emptyText.setTextColor(0xFF222222);
                        emptyText.setPadding(16, 16, 16, 16);
                        emptyText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                        categoriesContainer.addView(emptyText);
                        return;
                    }

                    int count = 1;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String categoryId = document.getId();
                        String categoryName = document.getString("categoryName");
                        String description = document.getString("description");

                        if (categoryName == null) categoryName = "";
                        if (description == null) description = "";

                        addCategoryCard(count, categoryId, categoryName, description);
                        count++;
                    }
                })
                .addOnFailureListener(e -> {
                    categoriesContainer.removeAllViews();

                    TextView errorText = new TextView(this);
                    errorText.setText("Failed to load categories.");
                    errorText.setTextSize(15);
                    errorText.setTextColor(0xFF222222);
                    errorText.setPadding(16, 16, 16, 16);
                    errorText.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
                    categoriesContainer.addView(errorText);

                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void addCategoryCard(int number, String categoryId, String categoryName, String description) {
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

        TextView title = new TextView(this);
        title.setText(number + ". " + categoryName);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF0B3D91);

        TextView descriptionText = new TextView(this);
        descriptionText.setText("Description: " + description);
        descriptionText.setTextSize(15);
        descriptionText.setTextColor(0xFF222222);
        descriptionText.setPadding(0, 8, 0, 0);

        TextView idText = new TextView(this);
        idText.setText("Category ID: " + categoryId);
        idText.setTextSize(12);
        idText.setTextColor(0xFF777777);
        idText.setPadding(0, 8, 0, 8);

        Button btnDeleteCategory = new Button(this);
        btnDeleteCategory.setText("Delete Category");
        btnDeleteCategory.setTextColor(0xFFFFFFFF);
        btnDeleteCategory.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_danger_button));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 10, 0, 0);
        btnDeleteCategory.setLayoutParams(buttonParams);

        btnDeleteCategory.setOnClickListener(v -> {
            db.collection("categories").document(categoryId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete category: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        card.addView(title);
        card.addView(descriptionText);
        card.addView(idText);
        card.addView(btnDeleteCategory);

        categoriesContainer.addView(card);
    }
}