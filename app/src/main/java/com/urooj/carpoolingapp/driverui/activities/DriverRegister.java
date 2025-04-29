package com.urooj.carpoolingapp.driverui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.driverui.DriverModel.Driver;


public class DriverRegister extends AppCompatActivity {

    private static final String TAG = "DriverRegister";

    // UI components for step 1
    private ViewFlipper viewFlipper;
    private EditText fullNameEditText, casteEditText, religionEditText, phoneEditText,
            nationalityEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioGroup genderRadioGroup;
    private Button nextButton;
    private TextView loginLinkTextView, stepTextView;

    // UI components for step 2
    private EditText carNumberEditText, cnicEditText, homeAddressEditText, driverPhotoUrlEditText, licensePhotoUrlEditText;
    private Button registerButton, previousButton;
    private ImageView driverPhotoPreview, licenseImagePreview;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Form data
    private String fullName, caste, religion, phoneNumber, nationality, email, password;
    private String gender, carNumber, cnic, homeAddress, driverPhotoUrl, licensePhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views and view flipper for multi-step form
        initializeViews();

        // Set up click listeners
        setupClickListeners();
        setupTextWatchers();
    }

    private void initializeViews() {
        viewFlipper = findViewById(R.id.view_flipper);
        stepTextView = findViewById(R.id.step_text);

        // Step 1 views
        fullNameEditText = findViewById(R.id.full_name_edit_text);
        casteEditText = findViewById(R.id.caste_edit_text);
        religionEditText = findViewById(R.id.religion_edit_text);
        phoneEditText = findViewById(R.id.phone_edit_text);
        nationalityEditText = findViewById(R.id.nationality_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        genderRadioGroup = findViewById(R.id.gender_radio_group);
        nextButton = findViewById(R.id.next_button);
        loginLinkTextView = findViewById(R.id.login_text_view);

        // Step 2 views
        carNumberEditText = findViewById(R.id.car_number_edit_text);
        cnicEditText = findViewById(R.id.cnic_edit_text);
        homeAddressEditText = findViewById(R.id.home_address_edit_text);
        driverPhotoUrlEditText = findViewById(R.id.driver_photo_url_edit_text);
        licensePhotoUrlEditText = findViewById(R.id.license_photo_url_edit_text);
        registerButton = findViewById(R.id.register_button);
        previousButton = findViewById(R.id.previous_button);
        driverPhotoPreview = findViewById(R.id.driver_photo_preview);
        licenseImagePreview = findViewById(R.id.license_image_preview);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupTextWatchers() {
        // Add URL validation for driver photo
        driverPhotoUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    // Load image from URL using Glide
                    Glide.with(DriverRegister.this)
                            .load(url)
                            .apply(new RequestOptions()
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(driverPhotoPreview);

                    driverPhotoPreview.setVisibility(View.VISIBLE);
                }
            }
        });

        // Add URL validation for license photo
        licensePhotoUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    // Load image from URL using Glide
                    Glide.with(DriverRegister.this)
                            .load(url)
                            .apply(new RequestOptions()
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image))
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(licenseImagePreview);

                    licenseImagePreview.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupClickListeners() {
        // Step 1 listeners
        nextButton.setOnClickListener(v -> {
            if (validateStep1()) {
                // Save data from step 1
                saveStep1Data();
                // Move to step 2
                viewFlipper.showNext();
                stepTextView.setText(R.string.step_2_of_2);
            }
        });

        loginLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(DriverRegister.this, LoginDriver.class));
            finish();
        });

        // Step 2 listeners
        previousButton.setOnClickListener(v -> {
            viewFlipper.showPrevious();
            stepTextView.setText(R.string.step_1_of_2);
        });

        registerButton.setOnClickListener(v -> {
            if (validateStep2()) {
                saveStep2Data();
                registerDriver();
            }
        });
    }

    private boolean validateStep1() {
        boolean isValid = true;

        // Get data from form
        String fullName = fullNameEditText.getText().toString().trim();
        String caste = casteEditText.getText().toString().trim();
        String religion = religionEditText.getText().toString().trim();
        String phoneNumber = phoneEditText.getText().toString().trim();
        String nationality = nationalityEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();

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

        // Validate religion
        if (TextUtils.isEmpty(religion)) {
            religionEditText.setError(getString(R.string.error_religion_required));
            isValid = false;
        }

        // Validate phone number
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneEditText.setError(getString(R.string.error_phone_required));
            isValid = false;
        }

        // Validate nationality
        if (TextUtils.isEmpty(nationality)) {
            nationalityEditText.setError(getString(R.string.error_nationality_required));
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

    private void saveStep1Data() {
        // Save data from step 1 form
        fullName = fullNameEditText.getText().toString().trim();
        caste = casteEditText.getText().toString().trim();
        religion = religionEditText.getText().toString().trim();
        phoneNumber = phoneEditText.getText().toString().trim();
        nationality = nationalityEditText.getText().toString().trim();
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();

        // Get gender
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        gender = selectedGenderButton.getText().toString().toLowerCase();
    }

    private boolean validateStep2() {
        boolean isValid = true;

        // Get data from step 2 form
        String carNumber = carNumberEditText.getText().toString().trim();
        String cnic = cnicEditText.getText().toString().trim();
        String homeAddress = homeAddressEditText.getText().toString().trim();

        // Validate car number
        if (TextUtils.isEmpty(carNumber)) {
            carNumberEditText.setError(getString(R.string.error_car_number_required));
            isValid = false;
        }

        if (TextUtils.isEmpty(cnic)) {
            cnicEditText.setError(getString(R.string.error_cnic_format));
            isValid = false;
        } else if (!cnic.matches("\\d{13}")) {
            cnicEditText.setError(getString(R.string.error_cnic_format));
            isValid = false;
        }

        // Validate home address
        if (TextUtils.isEmpty(homeAddress)) {
            homeAddressEditText.setError(getString(R.string.error_home_address_required));
            isValid = false;
        }

        // Validate driver photo URL
        String driverPhotoUrl = driverPhotoUrlEditText.getText().toString().trim();
        if (TextUtils.isEmpty(driverPhotoUrl)) {
            driverPhotoUrlEditText.setError(getString(R.string.error_driver_photo_url_required));
            isValid = false;
        } else if (!Patterns.WEB_URL.matcher(driverPhotoUrl).matches()) {
            driverPhotoUrlEditText.setError(getString(R.string.error_invalid_url));
            isValid = false;
        }

        // Validate license photo URL
        String licensePhotoUrl = licensePhotoUrlEditText.getText().toString().trim();
        if (TextUtils.isEmpty(licensePhotoUrl)) {
            licensePhotoUrlEditText.setError(getString(R.string.error_license_photo_url_required));
            isValid = false;
        } else if (!Patterns.WEB_URL.matcher(licensePhotoUrl).matches()) {
            licensePhotoUrlEditText.setError(getString(R.string.error_invalid_url));
            isValid = false;
        }

        return isValid;
    }

    private void saveStep2Data() {
        carNumber = carNumberEditText.getText().toString().trim();
        cnic = cnicEditText.getText().toString().trim();
        homeAddress = homeAddressEditText.getText().toString().trim();
        driverPhotoUrl = driverPhotoUrlEditText.getText().toString().trim();
        licensePhotoUrl = licensePhotoUrlEditText.getText().toString().trim();
    }

    private void registerDriver() {
        progressBar.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        // Create Firebase account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            createUserInDatabase(userId);
                        } else {
                            // Unlikely to happen
                            registrationFailed("Failed to get user data after registration");
                        }
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private void createUserInDatabase(String userId) {
        // Create user object
        Driver user = new Driver(userId, email, fullName, phoneNumber, caste, cnic, gender,
                religion, nationality, carNumber, homeAddress);

        // Add photo URLs
        user.setDriverPhotoUrl(driverPhotoUrl);
        user.setLicensePhotoUrl(licensePhotoUrl);
        user.setProfileImageUrl(driverPhotoUrl); // Use the driver photo as the profile image by default

        // Save to database
        mDatabase.child("users").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    registerButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        registrationSuccessful();
                    } else {
                        String errorMessage = "Failed to save user data";
                        if (task.getException() != null) {
                            errorMessage += ": " + task.getException().getMessage();
                        }
                        registrationFailed(errorMessage);
                    }
                });
    }

    private void registrationSuccessful() {
        Toast.makeText(this, getString(R.string.success_registration), Toast.LENGTH_SHORT).show();

        // Navigate to profile activity
        Intent intent = new Intent(DriverRegister.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void registrationFailed(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        registerButton.setEnabled(true);

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Registration failed: " + errorMessage);

        // Delete user account if it was created but data couldn't be saved
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User account deleted after failed registration.");
                }
            });
        }
    }

    private void handleRegistrationError(Exception exception) {
        progressBar.setVisibility(View.GONE);
        registerButton.setEnabled(true);

        String errorMessage = "Registration failed";

        if (exception != null) {
            if (exception instanceof FirebaseAuthUserCollisionException) {
                errorMessage = "Email address is already in use";
            } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                errorMessage = "Password is too weak";
            } else if (exception.getMessage() != null) {
                errorMessage = exception.getMessage();
            }
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Registration error: " + errorMessage);
    }
}