package com.urooj.carpoolingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.urooj.carpoolingapp.driverui.activities.DriverRegister;
import com.urooj.carpoolingapp.driverui.activities.LoginDriver;
import com.urooj.carpoolingapp.passengerui.activity.LoginPassenger;

public class UserSelectionActivity extends AppCompatActivity {
    private Button btnPassenger, btnDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        btnPassenger = findViewById(R.id.btnPassenger);
        btnDriver = findViewById(R.id.btnDriver);

        btnPassenger.setOnClickListener(v -> {
            startActivity(new Intent(UserSelectionActivity.this, LoginPassenger.class));
            finish();
        });

        btnDriver.setOnClickListener(v -> {
            startActivity(new Intent(UserSelectionActivity.this, DriverRegister.class));
            finish();
        });
    }
}