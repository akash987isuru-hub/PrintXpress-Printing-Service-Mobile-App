package com.example.printxpress;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminReplyQueryActivity extends AppCompatActivity {

    TextView tvQueryDetails;
    LinearLayout adminChatMessagesContainer;
    ScrollView adminChatScrollView;
    EditText etAdminReply;
    Button btnSendReply, btnBackSupportQueries;

    FirebaseFirestore db;
    String queryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reply_query);

        tvQueryDetails = findViewById(R.id.tvQueryDetails);
        adminChatMessagesContainer = findViewById(R.id.adminChatMessagesContainer);
        adminChatScrollView = findViewById(R.id.adminChatScrollView);
        etAdminReply = findViewById(R.id.etAdminReply);
        btnSendReply = findViewById(R.id.btnSendReply);
        btnBackSupportQueries = findViewById(R.id.btnBackSupportQueries);

        db = FirebaseFirestore.getInstance();
        queryId = getIntent().getStringExtra("queryId");

        if (queryId == null || queryId.isEmpty()) {
            tvQueryDetails.setText("Chat ID not found.");
        } else {
            loadQueryDetails();
            loadMessages();
        }

        btnSendReply.setOnClickListener(v -> sendAdminMessage());
        btnBackSupportQueries.setOnClickListener(v -> finish());
    }

    private void loadQueryDetails() {
        db.collection("supportQueries").document(queryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String subject = documentSnapshot.getString("subject");
                        String userId = documentSnapshot.getString("userId");
                        String queryStatus = documentSnapshot.getString("queryStatus");

                        if (subject == null || subject.isEmpty()) subject = "Support Chat";
                        if (userId == null || userId.isEmpty()) userId = "Unknown customer";
                        if (queryStatus == null || queryStatus.isEmpty()) queryStatus = "Open";

                        tvQueryDetails.setText(subject + "\nCustomer ID: " + userId + "\nStatus: " + queryStatus);
                    } else {
                        tvQueryDetails.setText("Chat not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    tvQueryDetails.setText("Failed to load chat details.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadMessages() {
        adminChatMessagesContainer.removeAllViews();
        addInfoMessage("Loading messages...");

        db.collection("supportQueries").document(queryId)
                .collection("messages")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    adminChatMessagesContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        loadFallbackOriginalMessage();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String sender = document.getString("sender");
                        String message = document.getString("message");
                        if (sender == null) sender = "customer";
                        if (message == null) message = "";
                        addChatBubble(sender, message);
                    }

                    scrollToBottom();
                })
                .addOnFailureListener(e -> {
                    adminChatMessagesContainer.removeAllViews();
                    addInfoMessage("Failed to load messages.");
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadFallbackOriginalMessage() {
        db.collection("supportQueries").document(queryId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String message = documentSnapshot.getString("message");
                    String reply = documentSnapshot.getString("reply");
                    if (message != null && !message.isEmpty()) addChatBubble("customer", message);
                    if (reply != null && !reply.isEmpty()) addChatBubble("admin", reply);
                    scrollToBottom();
                });
    }

    private void sendAdminMessage() {
        String reply = etAdminReply.getText().toString().trim();

        if (queryId == null || queryId.isEmpty()) {
            Toast.makeText(this, "Chat ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(reply)) {
            etAdminReply.setError("Message is required");
            return;
        }

        long now = System.currentTimeMillis();
        Map<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("sender", "admin");
        chatMessage.put("message", reply);
        chatMessage.put("createdAt", now);

        db.collection("supportQueries").document(queryId)
                .collection("messages")
                .add(chatMessage)
                .addOnSuccessListener(documentReference -> db.collection("supportQueries").document(queryId)
                        .update(
                                "reply", reply,
                                "queryStatus", "Replied",
                                "repliedAt", now,
                                "updatedAt", now
                        )
                        .addOnSuccessListener(unused -> {
                            etAdminReply.setText("");
                            loadQueryDetails();
                            loadMessages();
                            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void addInfoMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(0xFF6B7280);
        textView.setTextSize(14);
        textView.setPadding(12, 12, 12, 12);
        adminChatMessagesContainer.addView(textView);
    }

    private void addChatBubble(String sender, String message) {
        boolean isAdmin = "admin".equalsIgnoreCase(sender);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(isAdmin ? Gravity.END : Gravity.START);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 6, 0, 6);
        row.setLayoutParams(rowParams);

        TextView bubble = new TextView(this);
        bubble.setText((isAdmin ? "Admin\n" : "Customer\n") + message);
        bubble.setTextSize(14);
        bubble.setLineSpacing(4, 1f);
        bubble.setTextColor(isAdmin ? 0xFFFFFFFF : 0xFF1F2937);
        bubble.setPadding(14, 10, 14, 10);
        bubble.setBackground(ContextCompat.getDrawable(this, isAdmin ? R.drawable.bg_primary_button : R.drawable.bg_white_card));

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.74),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubble.setLayoutParams(bubbleParams);

        row.addView(bubble);
        adminChatMessagesContainer.addView(row);
    }

    private void scrollToBottom() {
        adminChatScrollView.post(() -> adminChatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
