package com.urooj.carpoolingapp.driverui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.urooj.carpoolingapp.R;

public class LoginDriver extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_driver);

        // Initialize UI elements
        etEmail = findViewById(R.id.etloginEMail);
        etPassword = findViewById(R.id.et_loginpassword);
        btnLogin = findViewById(R.id.btn_Login);
        tvRegister = findViewById(R.id.text_register);
        progressBar = findViewById(R.id.PROGRESS_Bar);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Login button click listener
        btnLogin.setOnClickListener(v -> performLogin());

        // Register TextView click listener
        tvRegister.setOnClickListener(v -> navigateToRegisterActivity());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Perform login with email and password
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Hide ProgressBar
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Log.d("Login to Dashboard", "going");
                        Intent intent = new Intent(LoginDriver.this, MainActivity.class);
                        intent.putExtra("fragment_to_load", "DriverDashboardFragment"); // Pass fragment name
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToRegisterActivity() {
        // Navigate to Register Activity
        startActivity(new Intent(this, DriverRegister.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user is already signed in
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginDriver.this, MainActivity.class);
            intent.putExtra("fragment_to_load", "DriverDashboardFragment");
            startActivity(intent);
            finish();
        }
    }
}
