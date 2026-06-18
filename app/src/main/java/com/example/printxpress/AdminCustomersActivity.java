package com.example.printxpress;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminCustomersActivity extends AppCompatActivity {

    TextView tvCustomerSummary;
    LinearLayout customersContainer;
    Button btnBackAdminFromCustomers;

    FirebaseFirestore db;
    ListenerRegistration customerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_customers);

        tvCustomerSummary = findViewById(R.id.tvCustomerSummary);
        customersContainer = findViewById(R.id.customersContainer);
        btnBackAdminFromCustomers = findViewById(R.id.btnBackAdminFromCustomers);

        db = FirebaseFirestore.getInstance();

        listenCustomers();

        btnBackAdminFromCustomers.setOnClickListener(v -> finish());
    }

    private void listenCustomers() {
        customersContainer.removeAllViews();
        customersContainer.addView(createInfoText("Loading customers..."));

        customerListener = db.collection("users")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        customersContainer.removeAllViews();
                        customersContainer.addView(createInfoText("Failed to load customers."));
                        Toast.makeText(this, "Customer loading error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    customersContainer.removeAllViews();

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        tvCustomerSummary.setText("No registered customers found.");
                        customersContainer.addView(createInfoText("No customers registered yet."));
                        return;
                    }

                    int count = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userType = document.getString("userType");
                        String role = document.getString("role");

                        String typeText = ((userType == null ? "" : userType) + " " + (role == null ? "" : role)).toLowerCase();

                        if (typeText.contains("admin") || typeText.contains("staff")) {
                            continue;
                        }

                        String fullName = document.getString("fullName");
                        String email = document.getString("email");
                        String phone = document.getString("phone");

                        if (fullName == null || fullName.trim().isEmpty()) {
                            fullName = "Customer";
                        }

                        if (email == null || email.trim().isEmpty()) {
                            email = "Email not available";
                        }

                        if (phone == null || phone.trim().isEmpty()) {
                            phone = "Mobile not available";
                        }

                        count++;
                        addCustomerCard(count, fullName, email, phone);
                    }

                    if (count == 0) {
                        tvCustomerSummary.setText("No registered customers found.");
                        customersContainer.addView(createInfoText("No customer accounts found."));
                    } else {
                        tvCustomerSummary.setText("Total registered customers: " + count);
                    }
                });
    }

    private void addCustomerCard(int number, String name, String email, String phone) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setElevation(3f);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(cardParams);

        TextView title = new TextView(this);
        title.setText(number + ". " + name);
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(0xFF123E91);

        TextView emailText = new TextView(this);
        emailText.setText("Email: " + email);
        emailText.setTextSize(14);
        emailText.setTextColor(0xFF1F2937);
        emailText.setPadding(0, dp(8), 0, 0);

        TextView phoneText = new TextView(this);
        phoneText.setText("Mobile: " + phone);
        phoneText.setTextSize(14);
        phoneText.setTextColor(0xFF1F2937);
        phoneText.setPadding(0, dp(4), 0, 0);

        card.addView(title);
        card.addView(emailText);
        card.addView(phoneText);

        customersContainer.addView(card);
    }

    private TextView createInfoText(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(0xFF1F2937);
        textView.setPadding(dp(16), dp(16), dp(16), dp(16));
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        return textView;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (customerListener != null) {
            customerListener.remove();
        }
    }
}
