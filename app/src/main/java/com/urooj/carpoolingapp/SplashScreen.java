package com.urooj.carpoolingapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCMToken", "Fetching FCM token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCMToken", "Manual fetch: " + token);
                });
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, UserSelectionActivity.class));
            finish();
        }, 2000);
    }
}