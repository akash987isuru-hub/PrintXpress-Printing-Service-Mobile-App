package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    LinearLayout cardManageCategories, cardManageProducts, cardReviewDesigns;
    LinearLayout cardUpdateOrderStatus, cardManagePromotions, cardSupportQueries;
    LinearLayout cardViewCustomers, cardViewRevenue;
    Button btnAdminLogout;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        auth = FirebaseAuth.getInstance();

        cardManageCategories = findViewById(R.id.cardManageCategories);
        cardManageProducts = findViewById(R.id.cardManageProducts);
        cardReviewDesigns = findViewById(R.id.cardReviewDesigns);
        cardUpdateOrderStatus = findViewById(R.id.cardUpdateOrderStatus);
        cardManagePromotions = findViewById(R.id.cardManagePromotions);
        cardSupportQueries = findViewById(R.id.cardSupportQueries);
        cardViewCustomers = findViewById(R.id.cardViewCustomers);
        cardViewRevenue = findViewById(R.id.cardViewRevenue);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        cardManageCategories.setOnClickListener(v -> openScreen(ManageCategoriesActivity.class));
        cardManageProducts.setOnClickListener(v -> openScreen(ManageProductsActivity.class));
        cardReviewDesigns.setOnClickListener(v -> openScreen(ReviewDesignsActivity.class));
        cardUpdateOrderStatus.setOnClickListener(v -> openScreen(AdminOrdersActivity.class));
        cardManagePromotions.setOnClickListener(v -> openScreen(ManagePromotionsActivity.class));
        cardSupportQueries.setOnClickListener(v -> openScreen(AdminSupportQueriesActivity.class));
        cardViewCustomers.setOnClickListener(v -> openScreen(AdminCustomersActivity.class));
        cardViewRevenue.setOnClickListener(v -> openScreen(AdminRevenueActivity.class));

        btnAdminLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openScreen(Class<?> activityClass) {
        startActivity(new Intent(AdminActivity.this, activityClass));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
