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

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvGoLogin;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            return;
        }

        if (phone.length() != 10) {
            etPhone.setError("Phone number must be 10 digits");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnRegister.setEnabled(false);
        LoadingDialogUtil.show(this, "Creating account...", 0);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("fullName", fullName);
                    user.put("email", email);
                    user.put("phone", phone);
                    user.put("userType", "Customer");

                    db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                LoadingDialogUtil.hide(this);
                                Toast.makeText(this, "Registration successful. Please login.", Toast.LENGTH_SHORT).show();

                                // Sign out the newly registered user so they must login manually
                                auth.signOut();

                                // Go to Login page after successful registration
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnRegister.setEnabled(true);
                                LoadingDialogUtil.hide(this);
                                Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    LoadingDialogUtil.hide(this);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}