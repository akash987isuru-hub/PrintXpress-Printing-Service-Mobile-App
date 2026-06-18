package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    TextView tvPaymentAmount;
    EditText etCardHolderName, etCardNumber, etExpiryDate, etCvv;
    Button btnPayNow, btnBackPayment;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String categoryName, productName, productPrice, size, material;
    String instructions, designFileName, deliveryMethod, deliveryAddress, orderDate, orderTime;
    int quantity, totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvPaymentAmount = findViewById(R.id.tvPaymentAmount);
        etCardHolderName = findViewById(R.id.etCardHolderName);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCvv = findViewById(R.id.etCvv);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnBackPayment = findViewById(R.id.btnBackPayment);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getIntentData();
        tvPaymentAmount.setText("Amount to Pay: Rs. " + totalPrice);

        btnPayNow.setOnClickListener(v -> validatePaymentAndPlaceOrder());
        btnBackPayment.setOnClickListener(v -> finish());
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

    private void validatePaymentAndPlaceOrder() {
        String cardHolder = etCardHolderName.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiry = etExpiryDate.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();

        if (TextUtils.isEmpty(cardHolder)) {
            etCardHolderName.setError("Card holder name is required");
            return;
        }

        if (cardNumber.length() != 16) {
            etCardNumber.setError("Card number must be 16 digits");
            return;
        }

        if (TextUtils.isEmpty(expiry) || expiry.length() != 5 || !expiry.contains("/")) {
            etExpiryDate.setError("Enter expiry as MM/YY");
            return;
        }

        if (cvv.length() != 3) {
            etCvv.setError("CVV must be 3 digits");
            return;
        }

        placeOrder(cardHolder, cardNumber);
    }

    private void placeOrder(String cardHolder, String cardNumber) {
        String userId = "guest_user";
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        String last4 = cardNumber.substring(cardNumber.length() - 4);

        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);
        order.put("categoryName", categoryName);
        order.put("productName", productName);
        order.put("productPrice", productPrice);
        order.put("size", size);
        order.put("material", material);
        order.put("quantity", quantity);
        order.put("instructions", instructions);
        order.put("designFileName", designFileName);
        order.put("deliveryMethod", deliveryMethod);
        order.put("deliveryAddress", deliveryAddress);
        order.put("orderDate", orderDate);
        order.put("orderTime", orderTime);
        order.put("totalPrice", totalPrice);
        order.put("orderStatus", "Pending");
        order.put("paymentStatus", "Paid");
        order.put("paymentMethod", "Dummy Card");
        order.put("cardHolderName", cardHolder);
        order.put("cardLast4", last4);
        order.put("paidAt", System.currentTimeMillis());
        order.put("createdAt", System.currentTimeMillis());

        Toast.makeText(this, "Processing dummy payment...", Toast.LENGTH_SHORT).show();

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Payment successful. Order placed.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PaymentActivity.this, CartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("highlightOrderId", documentReference.getId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Order failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
