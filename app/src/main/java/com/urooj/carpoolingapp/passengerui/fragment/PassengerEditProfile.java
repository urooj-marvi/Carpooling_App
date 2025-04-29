package com.urooj.carpoolingapp.passengerui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.urooj.carpoolingapp.R;

import com.urooj.carpoolingapp.passengerui.activity.LoginPassenger;

import java.util.HashMap;
import java.util.Map;

public class PassengerEditProfile extends Fragment {

    private static final String TAG = "PassengerEditProfileFragment";

    private TextInputEditText fullNameEditText, phoneEditText, addressEditText, profileImageUrlEditText;
    private ShapeableImageView profileImageView;
    private Button saveButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private String profileImageUrl;
    private String email;
    private String userType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Get data from arguments
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
            profileImageUrl = args.getString("profileImageUrl");
            email = args.getString("email");
            userType = args.getString("userType");
        }

        if (userId == null) {
            Toast.makeText(requireContext(), getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Set up listeners
        setupListeners();

        // Load existing data
        loadExistingData();
    }

    private void initializeViews(View view) {
        fullNameEditText = view.findViewById(R.id.full_name_edit_text);
        phoneEditText = view.findViewById(R.id.phone_edit_text);
        addressEditText = view.findViewById(R.id.address_edit_text);
        profileImageUrlEditText = view.findViewById(R.id.profile_image_url_edit_text);
        profileImageView = view.findViewById(R.id.profile_image_card);
        saveButton = view.findViewById(R.id.save_button);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupListeners() {
        // Image URL preview listener
        profileImageUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String url = s.toString().trim();
                if (!url.isEmpty() && Patterns.WEB_URL.matcher(url).matches()) {
                    loadImageWithGlide(url, profileImageView);
                }
            }
        });

        // Save button click listener
        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void loadExistingData() {
        Bundle args = getArguments();
        if (args != null) {
            String fullName = args.getString("fullName");
            String phoneNumber = args.getString("phoneNumber");
            String address = args.getString("address");
            profileImageUrl = args.getString("profileImageUrl");

            if (fullName != null) fullNameEditText.setText(fullName);
            if (phoneNumber != null) phoneEditText.setText(phoneNumber);
            if (address != null) addressEditText.setText(address);
            if (profileImageUrl != null) {
                profileImageUrlEditText.setText(profileImageUrl);
                loadImageWithGlide(profileImageUrl, profileImageView);
            }
        }
    }

    private void saveUserData() {
        String fullName = fullNameEditText.getText().toString().trim();
        String phoneNumber = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String profileImageUrl = profileImageUrlEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(fullName)) {
            fullNameEditText.setError(getString(R.string.error_name_required));
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            phoneEditText.setError(getString(R.string.error_phone_required));
            return;
        }

        if (!TextUtils.isEmpty(profileImageUrl) && !Patterns.WEB_URL.matcher(profileImageUrl).matches()) {
            profileImageUrlEditText.setError(getString(R.string.error_invalid_url));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("fullName", fullName);
            updates.put("phoneNumber", phoneNumber);
            updates.put("address", address);

            if (!profileImageUrl.isEmpty()) {
                updates.put("profileImageUrl", profileImageUrl);
            }

            mDatabase.updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    getString(R.string.success_profile_update),
                                    Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed(); // Go back to profile
                        } else {
                            handleDatabaseError(task.getException());
                        }
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            saveButton.setEnabled(true);
            Log.e(TAG, "Error saving profile", e);
            Toast.makeText(requireContext(),
                    getString(R.string.error_update_profile),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImageWithGlide(String imageUrl, ImageView imageView) {
        Glide.with(this)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(imageView);
    }

    private void handleDatabaseError(Exception exception) {
        String errorMessage = getString(R.string.error_update_profile);

        if (exception != null) {
            Log.e(TAG, "Error updating profile", exception);

            if (exception.getMessage() != null &&
                    exception.getMessage().contains("Permission denied")) {
                showPermissionDeniedError();
                return;
            }

            if (exception.getMessage() != null) {
                errorMessage += ": " + exception.getMessage();
            }
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void showPermissionDeniedError() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.error_permission_denied_title))
                .setMessage(getString(R.string.error_permission_denied_message))
                .setPositiveButton(getString(R.string.btn_sign_out), (dialog, which) -> {
                    mAuth.signOut();
                    startActivity(new Intent(requireActivity(), LoginPassenger.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    requireActivity().finish();
                })
                .setNegativeButton(getString(R.string.btn_retry), (dialog, which) -> saveUserData())
                .setCancelable(false)
                .show();
    }
}