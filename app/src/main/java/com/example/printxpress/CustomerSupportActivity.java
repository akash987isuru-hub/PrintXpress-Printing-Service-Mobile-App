package com.example.printxpress;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class CustomerSupportActivity extends AppCompatActivity {

    EditText etSupportSubject, etSupportMessage;
    Button btnSubmitSupportQuery, btnBackHomeFromSupport;
    LinearLayout customerQueriesContainer;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String userId;
    int queryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support);

        etSupportSubject = findViewById(R.id.etSupportSubject);
        etSupportMessage = findViewById(R.id.etSupportMessage);
        btnSubmitSupportQuery = findViewById(R.id.btnSubmitSupportQuery);
        btnBackHomeFromSupport = findViewById(R.id.btnBackHomeFromSupport);
        customerQueriesContainer = findViewById(R.id.customerQueriesContainer);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "guest_user";

        loadCustomerQueries();

        btnSubmitSupportQuery.setOnClickListener(v -> submitSupportQuery());
        btnBackHomeFromSupport.setOnClickListener(v -> finish());
    }

    private void submitSupportQuery() {
        String subject = etSupportSubject.getText().toString().trim();
        String message = etSupportMessage.getText().toString().trim();

        if (TextUtils.isEmpty(subject)) {
            etSupportSubject.setError("Subject is required");
            return;
        }

        if (TextUtils.isEmpty(message)) {
            etSupportMessage.setError("Message is required");
            return;
        }

        long now = System.currentTimeMillis();

        Map<String, Object> query = new HashMap<>();
        query.put("userId", userId);
        query.put("subject", subject);
        query.put("message", message);
        query.put("reply", "");
        query.put("queryStatus", "Open");
        query.put("createdAt", now);
        query.put("updatedAt", now);

        db.collection("supportQueries")
                .add(query)
                .addOnSuccessListener(documentReference -> {
                    Map<String, Object> chatMessage = new HashMap<>();
                    chatMessage.put("sender", "customer");
                    chatMessage.put("message", message);
                    chatMessage.put("createdAt", now);

                    documentReference.collection("messages")
                            .add(chatMessage)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Support chat started", Toast.LENGTH_SHORT).show();
                                etSupportSubject.setText("");
                                etSupportMessage.setText("");

                                Intent intent = new Intent(CustomerSupportActivity.this, CustomerSupportChatActivity.class);
                                intent.putExtra("queryId", documentReference.getId());
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Chat created, but first message failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                loadCustomerQueries();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to start chat: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void loadCustomerQueries() {
        customerQueriesContainer.removeAllViews();
        customerQueriesContainer.addView(createInfoText("Loading your support chats..."));

        db.collection("supportQueries")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    customerQueriesContainer.removeAllViews();
                    queryCount = 0;

                    if (queryDocumentSnapshots.isEmpty()) {
                        customerQueriesContainer.addView(createInfoText("You have not started any support chat yet."));
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        queryCount++;

                        String queryId = document.getId();
                        String subject = document.getString("subject");
                        String message = document.getString("message");
                        String reply = document.getString("reply");
                        String queryStatus = document.getString("queryStatus");

                        if (subject == null || subject.isEmpty()) subject = "Support Query";
                        if (message == null) message = "";
                        if (reply == null || reply.isEmpty()) reply = "No admin message yet";
                        if (queryStatus == null || queryStatus.isEmpty()) queryStatus = "Open";

                        addQueryCard(queryCount, queryId, subject, message, reply, queryStatus);
                    }
                })
                .addOnFailureListener(e -> {
                    customerQueriesContainer.removeAllViews();
                    customerQueriesContainer.addView(createInfoText("Failed to load support chats."));
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

    private void addQueryCard(int number, String queryId, String subject, String message, String reply, String queryStatus) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_white_card));
        card.setElevation(3f);
        card.setClickable(true);

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
        previewText.setText("Last admin message: " + reply);
        previewText.setTextSize(14);
        previewText.setTextColor(0xFF6B7280);
        previewText.setPadding(0, 6, 0, 0);
        previewText.setMaxLines(2);

        TextView tapText = new TextView(this);
        tapText.setText("Tap to open chat >");
        tapText.setTextSize(13);
        tapText.setTypeface(null, Typeface.BOLD);
        tapText.setTextColor(0xFF123E91);
        tapText.setPadding(0, 10, 0, 0);

        card.addView(titleText);
        card.addView(subjectText);
        card.addView(previewText);
        card.addView(tapText);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerSupportActivity.this, CustomerSupportChatActivity.class);
            intent.putExtra("queryId", queryId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        customerQueriesContainer.addView(card);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomerQueries();
    }
}
