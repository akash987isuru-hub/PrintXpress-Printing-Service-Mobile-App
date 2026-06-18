package com.example.printxpress;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2300;

    ImageView imgSplashLogo;
    TextView tvSplashTitle, tvSplashSubtitle;
    ProgressBar progressSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgSplashLogo = findViewById(R.id.imgSplashLogo);
        tvSplashTitle = findViewById(R.id.tvSplashTitle);
        tvSplashSubtitle = findViewById(R.id.tvSplashSubtitle);
        progressSplash = findViewById(R.id.progressSplash);

        Animation logoAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_logo_enter);
        Animation textAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_text_enter);

        imgSplashLogo.startAnimation(logoAnimation);
        tvSplashTitle.startAnimation(textAnimation);
        tvSplashSubtitle.startAnimation(textAnimation);
        progressSplash.startAnimation(textAnimation);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, SPLASH_DURATION_MS);
    }
}
