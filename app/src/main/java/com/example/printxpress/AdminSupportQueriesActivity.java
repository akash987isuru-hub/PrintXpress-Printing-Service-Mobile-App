package com.example.printxpress;

import android.content.Intent;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminSupportQueriesActivity extends AppCompatActivity {

    LinearLayout supportQueriesContainer;
    Button btnRefreshSupportQueries, btnBackAdminFromSupport;

    FirebaseFirestore db;
    int queryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_support_queries);

        supportQueriesContainer = findViewById(R.id.supportQueriesContainer);
        btnRefreshSupportQueries = findViewById(R.id.btnRefreshSupportQueries);
        btnBackAdminFromSupport = findViewById(R.id.btnBackAdminFromSupport);

        db = FirebaseFirestore.getInstance();

        loadSupportQueries();

        btnRefreshSupportQueries.setOnClickListener(v -> {
            loadSupportQueries();
            Toast.makeText(this, "Support chats refreshed", Toast.LENGTH_SHORT).show();
        });

        btnBackAdminFromSupport.setOnClickListener(v -> finish());
    }

    private void loadSupportQueries() {
        supportQueriesContainer.removeAllViews();
        supportQueriesContainer.addView(createInfoText("Loading support chats..."));

        db.collection("supportQueries")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    supportQueriesContainer.removeAllViews();
                    queryCount = 0;

                    if (queryDocumentSnapshots.isEmpty()) {
                        supportQueriesContainer.addView(createInfoText("No support chats available."));
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        queryCount++;

                        String queryId = document.getId();
                        String userId = document.getString("userId");
                        String subject = document.getString("subject");
                        String message = document.getString("message");
                        String reply = document.getString("reply");
                        String queryStatus = document.getString("queryStatus");

                        if (userId == null) userId = "Unknown customer";
                        if (subject == null || subject.isEmpty()) subject = "Support Query";
                        if (message == null) message = "";
                        if (reply == null || reply.isEmpty()) reply = "No admin reply yet";
                        if (queryStatus == null || queryStatus.isEmpty()) queryStatus = "Open";

                        addQueryCard(queryCount, queryId, userId, subject, message, reply, queryStatus);
                    }
                })
                .addOnFailureListener(e -> {
                    supportQueriesContainer.removeAllViews();
                    supportQueriesContainer.addView(createInfoText("Failed to load support chats."));
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private TextView createInfoText(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(15);
        textView.setTextColor(0xFF1F2937);
        textView.setPadding(16, 16, 16, 16);
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        return textView;
    }

    private void addQueryCard(int number, String queryId, String userId, String subject,
                              String message, String reply, String queryStatus) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setClickable(true);
        card.setElevation(3f);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 14);
        card.setLayoutParams(cardParams);

        TextView titleText = new TextView(this);
        titleText.setText("Chat " + number + "  •  " + queryStatus);
        titleText.setTextSize(17);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setTextColor(0xFF123E91);

        TextView subjectText = new TextView(this);
        subjectText.setText(subject);
        subjectText.setTextSize(15);
        subjectText.setTypeface(null, Typeface.BOLD);
        subjectText.setTextColor(0xFF1F2937);
        subjectText.setPadding(0, 8, 0, 0);

        TextView previewText = new TextView(this);
        previewText.setText("Customer: " + message);
        previewText.setTextSize(14);
        previewText.setTextColor(0xFF6B7280);
        previewText.setPadding(0, 6, 0, 0);
        previewText.setMaxLines(2);

        TextView customerText = new TextView(this);
        customerText.setText("Customer ID: " + userId);
        customerText.setTextSize(12);
        customerText.setTextColor(0xFF777777);
        customerText.setPadding(0, 8, 0, 0);

        TextView tapText = new TextView(this);
        tapText.setText("Tap to open chat >");
        tapText.setTextSize(13);
        tapText.setTypeface(null, Typeface.BOLD);
        tapText.setTextColor(0xFF123E91);
        tapText.setPadding(0, 10, 0, 0);

        card.addView(titleText);
        card.addView(subjectText);
        card.addView(previewText);
        card.addView(customerText);
        card.addView(tapText);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(AdminSupportQueriesActivity.this, AdminReplyQueryActivity.class);
            intent.putExtra("queryId", queryId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        supportQueriesContainer.addView(card);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSupportQueries();
    }
}
