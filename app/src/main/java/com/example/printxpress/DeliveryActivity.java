package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DeliveryActivity extends AppCompatActivity {

    TextView tvDeliveryProduct;
    RadioGroup radioGroupDelivery;
    RadioButton rbPickup, rbDelivery;
    LinearLayout addressLayout;

    EditText etDate, etTime, etAddress, etCity, etDistrict, etContactNumber;
    Button btnContinueSummary, btnBackUpload;

    String categoryName, productName, productPrice, size, material, instructions, designFileName;
    int quantity, totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        tvDeliveryProduct = findViewById(R.id.tvDeliveryProduct);

        radioGroupDelivery = findViewById(R.id.radioGroupDelivery);
        rbPickup = findViewById(R.id.rbPickup);
        rbDelivery = findViewById(R.id.rbDelivery);
        addressLayout = findViewById(R.id.addressLayout);

        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etContactNumber = findViewById(R.id.etContactNumber);

        btnContinueSummary = findViewById(R.id.btnContinueSummary);
        btnBackUpload = findViewById(R.id.btnBackUpload);

        getIntentData();

        tvDeliveryProduct.setText(productName + " | Total: Rs. " + totalPrice);

        radioGroupDelivery.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDelivery) {
                addressLayout.setVisibility(View.VISIBLE);
            } else {
                addressLayout.setVisibility(View.GONE);
            }
        });

        btnContinueSummary.setOnClickListener(v -> validateAndGoToSummary());

        btnBackUpload.setOnClickListener(v -> finish());
    }

    private void getIntentData() {
        categoryName = getIntent().getStringExtra("categoryName");
        productName = getIntent().getStringExtra("productName");
        productPrice = getIntent().getStringExtra("productPrice");
        size = getIntent().getStringExtra("size");
        material = getIntent().getStringExtra("material");
        instructions = getIntent().getStringExtra("instructions");
        designFileName = getIntent().getStringExtra("designFileName");

        quantity = getIntent().getIntExtra("quantity", 0);
        totalPrice = getIntent().getIntExtra("totalPrice", 0);

        if (categoryName == null) categoryName = "";
        if (productName == null) productName = "";
        if (productPrice == null) productPrice = "";
        if (size == null) size = "";
        if (material == null) material = "";
        if (instructions == null) instructions = "";
        if (designFileName == null) designFileName = "";
    }

    private void validateAndGoToSummary() {
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (TextUtils.isEmpty(date)) {
            etDate.setError("Date is required");
            return;
        }

        if (TextUtils.isEmpty(time)) {
            etTime.setError("Time is required");
            return;
        }

        String deliveryMethod;
        String deliveryAddress = "";

        if (rbDelivery.isChecked()) {
            deliveryMethod = "Home Delivery";

            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String district = etDistrict.getText().toString().trim();
            String contactNumber = etContactNumber.getText().toString().trim();

            if (TextUtils.isEmpty(address)) {
                etAddress.setError("Address is required");
                return;
            }

            if (TextUtils.isEmpty(city)) {
                etCity.setError("City is required");
                return;
            }

            if (TextUtils.isEmpty(district)) {
                etDistrict.setError("District is required");
                return;
            }

            if (TextUtils.isEmpty(contactNumber)) {
                etContactNumber.setError("Contact number is required");
                return;
            }

            if (contactNumber.length() != 10) {
                etContactNumber.setError("Contact number must be 10 digits");
                return;
            }

            deliveryAddress = address + ", " + city + ", " + district + " | Contact: " + contactNumber;

        } else {
            deliveryMethod = "Pickup";
            deliveryAddress = "Customer will collect from PrintXpress shop";
        }

        Toast.makeText(this, "Delivery details added", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(DeliveryActivity.this, OrderSummaryActivity.class);

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
        intent.putExtra("orderDate", date);
        intent.putExtra("orderTime", time);

        startActivity(intent);
    }
}