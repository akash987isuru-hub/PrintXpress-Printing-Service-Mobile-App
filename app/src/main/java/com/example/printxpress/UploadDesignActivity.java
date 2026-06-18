package com.example.printxpress;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class UploadDesignActivity extends AppCompatActivity {

    TextView tvSelectedProduct, tvOrderDetails, tvTotalPriceUpload, tvFileName;
    EditText etInstructions;
    Button btnChooseFile, btnUploadContinue, btnBackDetails;

    String categoryName, productName, productPrice, size, material;
    int quantity, totalPrice;

    Uri selectedFileUri;

    FirebaseAuth auth;

    ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_design);

        tvSelectedProduct = findViewById(R.id.tvSelectedProduct);
        tvOrderDetails = findViewById(R.id.tvOrderDetails);
        tvTotalPriceUpload = findViewById(R.id.tvTotalPriceUpload);
        tvFileName = findViewById(R.id.tvFileName);

        etInstructions = findViewById(R.id.etInstructions);

        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnUploadContinue = findViewById(R.id.btnUploadContinue);
        btnBackDetails = findViewById(R.id.btnBackDetails);

        auth = FirebaseAuth.getInstance();

        getIntentData();
        displayOrderData();

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedFileUri = uri;
                        tvFileName.setText("Selected file: " + uri.getLastPathSegment());
                    }
                }
        );

        btnChooseFile.setOnClickListener(v -> chooseFile());

        btnUploadContinue.setOnClickListener(v -> validateAndContinue());

        btnBackDetails.setOnClickListener(v -> finish());
    }

    private void getIntentData() {
        categoryName = getIntent().getStringExtra("categoryName");
        productName = getIntent().getStringExtra("productName");
        productPrice = getIntent().getStringExtra("productPrice");
        size = getIntent().getStringExtra("size");
        material = getIntent().getStringExtra("material");
        quantity = getIntent().getIntExtra("quantity", 0);
        totalPrice = getIntent().getIntExtra("totalPrice", 0);

        if (categoryName == null) categoryName = "";
        if (productName == null) productName = "";
        if (productPrice == null) productPrice = "";
        if (size == null) size = "";
        if (material == null) material = "";
    }

    private void displayOrderData() {
        tvSelectedProduct.setText(productName + " - " + categoryName);
        tvOrderDetails.setText("Size: " + size + " | Material: " + material + " | Quantity: " + quantity);
        tvTotalPriceUpload.setText("Total Price: Rs. " + totalPrice);
    }

    private void chooseFile() {
        filePickerLauncher.launch("*/*");
    }

    private void validateAndContinue() {
        String instructions = etInstructions.getText().toString().trim();

        if (selectedFileUri == null && TextUtils.isEmpty(instructions)) {
            etInstructions.setError("Upload a file or enter design instructions");
            Toast.makeText(this, "Please upload a file or enter instructions", Toast.LENGTH_SHORT).show();
            return;
        }

        String designFileName = "";

        if (selectedFileUri != null) {
            String fileType = getFileExtension(selectedFileUri);

            if (fileType == null) {
                Toast.makeText(this, "Invalid file type", Toast.LENGTH_SHORT).show();
                return;
            }

            fileType = fileType.toLowerCase();

            if (!fileType.equals("jpg") &&
                    !fileType.equals("jpeg") &&
                    !fileType.equals("png") &&
                    !fileType.equals("pdf")) {

                Toast.makeText(this, "Only JPG, PNG, and PDF files are allowed", Toast.LENGTH_LONG).show();
                return;
            }

            designFileName = selectedFileUri.getLastPathSegment();
        }

        Toast.makeText(this, "Design details added successfully", Toast.LENGTH_SHORT).show();

        goToDeliveryScreen(instructions, designFileName);
    }

    private String getFileExtension(Uri uri) {
        String mimeType = getContentResolver().getType(uri);

        if (mimeType == null) {
            return null;
        }

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
    }

    private void goToDeliveryScreen(String instructions, String designFileName) {
        Intent intent = new Intent(UploadDesignActivity.this, DeliveryActivity.class);

        intent.putExtra("categoryName", categoryName);
        intent.putExtra("productName", productName);
        intent.putExtra("productPrice", productPrice);
        intent.putExtra("size", size);
        intent.putExtra("material", material);
        intent.putExtra("quantity", quantity);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("instructions", instructions);
        intent.putExtra("designFileName", designFileName);

        startActivity(intent);
    }
}