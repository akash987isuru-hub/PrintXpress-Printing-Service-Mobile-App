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

public class CustomerSupportChatActivity extends AppCompatActivity {

    TextView tvChatTitle, tvChatSubtitle;
    LinearLayout chatMessagesContainer;
    ScrollView chatScrollView;
    EditText etChatMessage;
    Button btnSendChatMessage, btnBackFromChat;

    FirebaseFirestore db;
    String queryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_support_chat);

        tvChatTitle = findViewById(R.id.tvChatTitle);
        tvChatSubtitle = findViewById(R.id.tvChatSubtitle);
        chatMessagesContainer = findViewById(R.id.chatMessagesContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSendChatMessage = findViewById(R.id.btnSendChatMessage);
        btnBackFromChat = findViewById(R.id.btnBackFromChat);

        db = FirebaseFirestore.getInstance();
        queryId = getIntent().getStringExtra("queryId");

        if (queryId == null || queryId.isEmpty()) {
            tvChatSubtitle.setText("Chat ID not found");
            return;
        }

        loadQueryHeader();
        loadMessages();

        btnSendChatMessage.setOnClickListener(v -> sendCustomerMessage());
        btnBackFromChat.setOnClickListener(v -> finish());
    }

    private void loadQueryHeader() {
        db.collection("supportQueries").document(queryId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String subject = documentSnapshot.getString("subject");
                    String status = documentSnapshot.getString("queryStatus");
                    if (subject == null || subject.isEmpty()) subject = "Support Chat";
                    if (status == null || status.isEmpty()) status = "Open";
                    tvChatTitle.setText(subject);
                    tvChatSubtitle.setText("Status: " + status);
                });
    }

    private void loadMessages() {
        chatMessagesContainer.removeAllViews();
        addInfoMessage("Loading messages...");

        db.collection("supportQueries").document(queryId)
                .collection("messages")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    chatMessagesContainer.removeAllViews();

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
                    chatMessagesContainer.removeAllViews();
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

    private void sendCustomerMessage() {
        String message = etChatMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            etChatMessage.setError("Message is required");
            return;
        }

        long now = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("sender", "customer");
        data.put("message", message);
        data.put("createdAt", now);

        db.collection("supportQueries").document(queryId)
                .collection("messages")
                .add(data)
                .addOnSuccessListener(documentReference -> db.collection("supportQueries").document(queryId)
                        .update("queryStatus", "Open", "message", message, "updatedAt", now)
                        .addOnSuccessListener(unused -> {
                            etChatMessage.setText("");
                            loadMessages();
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void addInfoMessage(String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextColor(0xFF6B7280);
        textView.setTextSize(14);
        textView.setPadding(12, 12, 12, 12);
        chatMessagesContainer.addView(textView);
    }

    private void addChatBubble(String sender, String message) {
        boolean isCustomer = "customer".equalsIgnoreCase(sender);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(isCustomer ? Gravity.END : Gravity.START);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 6, 0, 6);
        row.setLayoutParams(rowParams);

        TextView bubble = new TextView(this);
        bubble.setText((isCustomer ? "You\n" : "Admin\n") + message);
        bubble.setTextSize(14);
        bubble.setLineSpacing(4, 1f);
        bubble.setTextColor(isCustomer ? 0xFFFFFFFF : 0xFF1F2937);
        bubble.setTypeface(null, Typeface.NORMAL);
        bubble.setPadding(14, 10, 14, 10);
        bubble.setBackground(ContextCompat.getDrawable(this, isCustomer ? R.drawable.bg_primary_button : R.drawable.bg_white_card));

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.74),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubble.setLayoutParams(bubbleParams);

        row.addView(bubble);
        chatMessagesContainer.addView(row);
    }

    private void scrollToBottom() {
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
