package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etLoginEmail, etLoginPassword;
    Button btnLogin;
    TextView tvGoRegister;
    LinearLayout loginRoot;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoRegister = findViewById(R.id.tvGoRegister);
        loginRoot = findViewById(R.id.loginRoot);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        loginRoot.startAnimation(fadeIn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void loginUser() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etLoginEmail.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Password is required");
            return;
        }

        btnLogin.setEnabled(false);
        LoadingDialogUtil.showLogin(this);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = auth.getCurrentUser().getUid();

                    db.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                LoadingDialogUtil.hide(this);
                                btnLogin.setEnabled(true);

                                if (documentSnapshot.exists()) {
                                    String userType = documentSnapshot.getString("userType");

                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                                    if (userType != null && userType.equalsIgnoreCase("Admin")) {
                                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    }

                                    finish();

                                } else {
                                    Toast.makeText(this, "User profile not found", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                LoadingDialogUtil.hide(this);
                                btnLogin.setEnabled(true);
                                Toast.makeText(this, "Failed to check user type: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    LoadingDialogUtil.hide(this);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}