package com.example.printxpress;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    Button btnBackAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        btnBackAbout = findViewById(R.id.btnBackAbout);
        btnBackAbout.setOnClickListener(v -> finish());
    }
}
