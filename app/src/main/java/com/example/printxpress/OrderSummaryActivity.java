package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderSummaryActivity extends AppCompatActivity {

    TextView tvSummaryDetails, tvSummaryTotal;
    Button btnConfirmOrder, btnBackDelivery;

    String categoryName, productName, productPrice, size, material;
    String instructions, designFileName, deliveryMethod, deliveryAddress, orderDate, orderTime;
    int quantity, totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        tvSummaryDetails = findViewById(R.id.tvSummaryDetails);
        tvSummaryTotal = findViewById(R.id.tvSummaryTotal);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnBackDelivery = findViewById(R.id.btnBackDelivery);

        getIntentData();
        displaySummary();

        btnConfirmOrder.setOnClickListener(v -> goToPayment());
        btnBackDelivery.setOnClickListener(v -> finish());
    }

    private void getIntentData() {
        categoryName = getIntent().getStringExtra("categoryName");
        productName = getIntent().getStringExtra("productName");
        productPrice = getIntent().getStringExtra("productPrice");
        size = getIntent().getStringExtra("size");
        material = getIntent().getStringExtra("material");
        instructions = getIntent().getStringExtra("instructions");
        designFileName = getIntent().getStringExtra("designFileName");
        deliveryMethod = getIntent().getStringExtra("deliveryMethod");
        deliveryAddress = getIntent().getStringExtra("deliveryAddress");
        orderDate = getIntent().getStringExtra("orderDate");
        orderTime = getIntent().getStringExtra("orderTime");

        quantity = getIntent().getIntExtra("quantity", 0);
        totalPrice = getIntent().getIntExtra("totalPrice", 0);

        if (categoryName == null) categoryName = "";
        if (productName == null) productName = "";
        if (productPrice == null) productPrice = "";
        if (size == null) size = "";
        if (material == null) material = "";
        if (instructions == null) instructions = "";
        if (designFileName == null) designFileName = "";
        if (deliveryMethod == null) deliveryMethod = "";
        if (deliveryAddress == null) deliveryAddress = "";
        if (orderDate == null) orderDate = "";
        if (orderTime == null) orderTime = "";
    }

    private void displaySummary() {
        String summary =
                "Product: " + productName + "\n" +
                        "Category: " + categoryName + "\n" +
                        "Base Price: " + productPrice + "\n\n" +
                        "Size: " + size + "\n" +
                        "Material: " + material + "\n" +
                        "Quantity: " + quantity + "\n\n" +
                        "Design File: " + (designFileName.isEmpty() ? "No file selected" : designFileName) + "\n" +
                        "Instructions: " + (instructions.isEmpty() ? "No instructions" : instructions) + "\n\n" +
                        "Delivery Method: " + deliveryMethod + "\n" +
                        "Address / Pickup: " + deliveryAddress + "\n" +
                        "Date: " + orderDate + "\n" +
                        "Time: " + orderTime + "\n\n" +
                        "Next Step: Dummy card payment";

        tvSummaryDetails.setText(summary);
        tvSummaryTotal.setText("Total Payable: Rs. " + totalPrice);
    }

    private void goToPayment() {
        Intent intent = new Intent(OrderSummaryActivity.this, PaymentActivity.class);
        intent.putExtra("categoryName", categoryName);
        intent.putExtra("productName", productName);
        intent.putExtra("productPrice", productPrice);
        intent.putExtra("size", size);
        intent.putExtra("material", material);
        intent.putExtra("quantity", quantity);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("instructions", instructions);
        intent.putExtra("designFileName", designFileName);
        intent.putExtra("deliveryMethod", deliveryMethod);
        intent.putExtra("deliveryAddress", deliveryAddress);
        intent.putExtra("orderDate", orderDate);
        intent.putExtra("orderTime", orderTime);
        startActivity(intent);
    }
}
