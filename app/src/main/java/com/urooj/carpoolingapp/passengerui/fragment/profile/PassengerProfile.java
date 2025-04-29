package com.urooj.carpoolingapp.passengerui.fragment.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.passengerui.PassengerModel.Passenger;
import com.urooj.carpoolingapp.passengerui.activity.LoginPassenger;
import com.urooj.carpoolingapp.passengerui.fragment.PassengerEditProfile;


public class PassengerProfile extends Fragment {

    private static final String TAG = "PassengerProfileFragment";

    // Views
    private TextView fullNameTextView, userTypeTextView, emailTextView,
            phoneTextView, addressTextView, cnicTextView,
            genderTextView, casteTextView;
    private ImageView profileImageView;
    private Button editProfileButton, logoutButton;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private Passenger currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser == null) {
            // User not logged in, redirect to login
            requireActivity().startActivity(new Intent(requireContext(), LoginPassenger.class));
            requireActivity().finish();
            return;
        }

        userId = firebaseUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_passenger_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initializeViews(view);

        // Load user data
        loadUserData();

        // Set up button listeners
        setupButtonListeners();
    }

    private void initializeViews(View view) {
        fullNameTextView = view.findViewById(R.id.full_name_text_view);
        userTypeTextView = view.findViewById(R.id.user_type_text_view);
        emailTextView = view.findViewById(R.id.email_text_view);
        phoneTextView = view.findViewById(R.id.phone_text_view);
        addressTextView = view.findViewById(R.id.address_text_view);
        cnicTextView = view.findViewById(R.id.cnic_text_view);
        genderTextView = view.findViewById(R.id.gender_text_view);
        casteTextView = view.findViewById(R.id.caste_text_view);
        profileImageView = view.findViewById(R.id.profile_image_view);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        logoutButton = view.findViewById(R.id.logout_button);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupButtonListeners() {
        editProfileButton.setOnClickListener(v -> {
            if (currentUser != null) {
                // Create new instance of PassengerEditProfile fragment
                PassengerEditProfile editProfileFragment = new PassengerEditProfile();

                // Create a bundle to pass data
                Bundle args = new Bundle();
                args.putString("userId", userId);
                args.putString("fullName", currentUser.getFullName());
                args.putString("phoneNumber", currentUser.getPhoneNumber());
                args.putString("address", currentUser.getAddress());
                args.putString("profileImageUrl", currentUser.getProfileImageUrl());
                args.putString("email", currentUser.getEmail());
                args.putString("userType", currentUser.getUserType());

                editProfileFragment.setArguments(args);

                // Perform fragment transaction
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editProfileFragment) // Use your container ID
                        .addToBackStack(null) // Add to back stack so user can press back to return
                        .commit();
            } else {
                Toast.makeText(requireContext(), getString(R.string.info_waiting_data), Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(requireActivity(), LoginPassenger.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            requireActivity().finish();
        });
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        if (!isNetworkConnected()) {
            progressBar.setVisibility(View.GONE);
            showNetworkErrorDialog();
            return;
        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.GONE);

                if (dataSnapshot.exists()) {
                    try {
                        currentUser = dataSnapshot.getValue(Passenger.class);
                        if (currentUser != null) {
                            displayUserData();
                        } else {
                            Log.e(TAG, "User data is null");
                            showErrorMessage(getString(R.string.error_loading_profile));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user data", e);
                        showErrorMessage(getString(R.string.error_loading_profile));
                    }
                } else {
                    Log.e(TAG, "User data doesn't exist");
                    showErrorMessage(getString(R.string.error_loading_profile));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Database error: " + databaseError.getMessage(), databaseError.toException());

                if (databaseError.getCode() == DatabaseError.PERMISSION_DENIED) {
                    showPermissionDeniedError();
                } else {
                    showErrorMessage("Failed to load profile: " + databaseError.getMessage());
                }
            }
        });
    }

    private void displayUserData() {
        if (currentUser == null) return;

        // Set basic user info
        fullNameTextView.setText(currentUser.getFullName() != null ?
                currentUser.getFullName() : getString(R.string.data_placeholder));

        if (currentUser.getUserType() != null) {
            String userType = currentUser.getUserType();
            String capitalizedUserType = userType.substring(0, 1).toUpperCase() + userType.substring(1);
            userTypeTextView.setText(capitalizedUserType);
        } else {
            userTypeTextView.setText(getString(R.string.data_placeholder));
        }

        emailTextView.setText(currentUser.getEmail() != null ?
                currentUser.getEmail() : getString(R.string.data_placeholder));

        phoneTextView.setText(currentUser.getPhoneNumber() != null ?
                currentUser.getPhoneNumber() : getString(R.string.data_placeholder));

        addressTextView.setText(currentUser.getAddress() != null ?
                currentUser.getAddress() : getString(R.string.no_address));

        cnicTextView.setText(currentUser.getCnic() != null ?
                currentUser.getCnic() : getString(R.string.data_placeholder));

        if (currentUser.getGender() != null) {
            String gender = currentUser.getGender();
            String capitalizedGender = gender.substring(0, 1).toUpperCase() + gender.substring(1);
            genderTextView.setText(capitalizedGender);
        } else {
            genderTextView.setText(getString(R.string.data_placeholder));
        }

        casteTextView.setText(currentUser.getCaste() != null ?
                currentUser.getCaste() : getString(R.string.data_placeholder));

        // Load profile image
        if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfileImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
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
                .setNegativeButton(getString(R.string.btn_retry), (dialog, which) -> loadUserData())
                .setCancelable(false)
                .show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkConnected() {
        android.net.ConnectivityManager connectivityManager =
                (android.net.ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            android.net.Network network = connectivityManager.getActiveNetwork();
            android.net.NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(network);

            return capabilities != null &&
                    (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

    private void showNetworkErrorDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.error_network_title))
                .setMessage(getString(R.string.error_network_message))
                .setPositiveButton(getString(R.string.btn_retry), (dialog, which) -> loadUserData())
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDatabase != null) {
            loadUserData();
        }
    }
}