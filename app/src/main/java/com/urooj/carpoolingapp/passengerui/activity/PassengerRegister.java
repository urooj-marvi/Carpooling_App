package com.urooj.carpoolingapp.passengerui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.passengerui.PassengerModel.Passenger;


public class PassengerRegister extends AppCompatActivity {

    private static final String TAG = "PassengerRegister";

    // UI components
    private EditText fullNameEditText, casteEditText, phoneEditText, cnicEditText,
            emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup genderRadioGroup;
    private Button registerButton;
    private TextView loginLinkTextView;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Set up click listeners
        registerButton.setOnClickListener(v -> registerPassenger());
        loginLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(PassengerRegister.this, LoginPassenger.class));
            finish();
        });
    }

    private void initializeViews() {
        fullNameEditText = findViewById(R.id.full_name_edit_text);
        casteEditText = findViewById(R.id.caste_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        cnicEditText = findViewById(R.id.cnic_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        registerButton = findViewById(R.id.register_button);
        loginLinkTextView = findViewById(R.id.login_text_view);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void registerPassenger() {
        // Get values from form
        String fullName = fullNameEditText.getText().toString().trim();
        String caste = casteEditText.getText().toString().trim();
        String phoneNumber = phoneEditText.getText().toString().trim();
        String cnic = cnicEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Get gender selection
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();

        // Validate fields
        if (!validateFields(fullName, caste, phoneNumber, cnic, email, password,
                confirmPassword, selectedGenderId)) {
            return;
        }

        // Get gender value from selection
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = selectedGenderButton.getText().toString().toLowerCase();

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        // Create account with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();

                            // Create user object
                            Passenger user = new Passenger(userId, email, fullName, phoneNumber,
                                    caste, cnic, gender);

                            // Save to database
                            mDatabase.child("users").child(userId).setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        registerButton.setEnabled(true);

                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(PassengerRegister.this,
                                                    "Registration successful",
                                                    Toast.LENGTH_SHORT).show();

                                            // Navigate to profile
                                            Intent intent = new Intent(PassengerRegister.this,
                                                    PassengerActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Failed to save user data
                                            String errorMessage = "Failed to save user data";
                                            if (dbTask.getException() != null) {
                                                errorMessage = dbTask.getException().getMessage();
                                            }
                                            Toast.makeText(PassengerRegister.this,
                                                    errorMessage, Toast.LENGTH_LONG).show();
                                            Log.e(TAG, "Failed to save user data: " + errorMessage);
                                        }
                                    });
                        }
                    } else {
                        // Registration failed
                        progressBar.setVisibility(View.GONE);
                        registerButton.setEnabled(true);

                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "Email address is already in use";
                            } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                errorMessage = "Password is too weak";
                            } else {
                                errorMessage = task.getException().getMessage();
                            }
                        }

                        Toast.makeText(PassengerRegister.this,
                                errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration failed: " + errorMessage);
                    }
                });
    }

    private boolean validateFields(String fullName, String caste, String phoneNumber,
                                   String cnic, String email, String password,
                                   String confirmPassword, int selectedGenderId) {
        boolean isValid = true;

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError(getString(R.string.error_name_required));
            isValid = false;
        }

        // Validate caste
        if (TextUtils.isEmpty(caste)) {
            casteEditText.setError(getString(R.string.error_caste_required));
            isValid = false;
        }

        // Validate phone number
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneEditText.setError(getString(R.string.error_phone_required));
            isValid = false;
        }

        // Validate CNIC format (13 digits, no dashes)
        if (TextUtils.isEmpty(cnic)) {
            cnicEditText.setError(getString(R.string.error_cnic_format));
            isValid = false;
        } else if (!cnic.matches("\\d{13}")) {
            cnicEditText.setError(getString(R.string.error_cnic_format));
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_email_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.error_password_length));
            isValid = false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_passwords_not_match));
            isValid = false;
        }

        // Validate gender selection
        if (selectedGenderId == -1) {
            Toast.makeText(this, getString(R.string.error_gender_required), Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }
}