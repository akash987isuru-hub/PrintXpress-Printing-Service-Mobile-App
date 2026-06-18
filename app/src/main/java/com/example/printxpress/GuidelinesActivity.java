package com.example.printxpress;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GuidelinesActivity extends AppCompatActivity {

    Button btnBackGuidelines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidelines);

        btnBackGuidelines = findViewById(R.id.btnBackGuidelines);

        btnBackGuidelines.setOnClickListener(v -> finish());
    }
}