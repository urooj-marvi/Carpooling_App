package com.urooj.carpoolingapp.passengerui.fragment;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.urooj.carpoolingapp.R;

import java.util.HashMap;
import java.util.Map;

public class ViewDriverProfile extends BottomSheetDialogFragment {

    private static final String ARG_DRIVER_ID = "driver_id";
    private static final String TAG = "ViewDriverProfile";
    private String driverId;

    private TextView addressTextView, carNumberTextView, casteTextView, nameTextView;
    private ImageView profileImageView;
    private ProgressBar loadingProgressBar;
    private Button requestRideButton;
    private String currentRequestId;
    private String currentRequestStatus;

    public static ViewDriverProfile newInstance(String driverId) {
        ViewDriverProfile fragment = new ViewDriverProfile();
        Bundle args = new Bundle();
        args.putString(ARG_DRIVER_ID, driverId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            driverId = getArguments().getString(ARG_DRIVER_ID);
            Log.d(TAG, "Received driver ID in arguments: " + driverId);
        } else {
            Log.e(TAG, "No arguments received, driver ID is null");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_driver_profile, container, false);

        try {
            // Initialize all UI elements
            nameTextView = view.findViewById(R.id.nameTextView);
            addressTextView = view.findViewById(R.id.addressTextView);
            carNumberTextView = view.findViewById(R.id.carNumberTextView);
            casteTextView = view.findViewById(R.id.casteTextView);
            profileImageView = view.findViewById(R.id.profileImageView);
            loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
            requestRideButton = view.findViewById(R.id.requestRideButton);

            // Set up click listener for request button
            if (requestRideButton != null) {
                requestRideButton.setOnClickListener(v -> requestRide());
            }

            // Fetch data and check for existing ride requests
            if (driverId != null && !driverId.isEmpty()) {
                fetchDriverData();
                checkExistingRideRequest();
            } else {
                Log.e(TAG, "Driver ID is null or empty, cannot fetch data");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: Invalid driver ID", Toast.LENGTH_SHORT).show();
                }
                dismiss();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up profile view: " + e.getMessage());
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error setting up profile view", Toast.LENGTH_SHORT).show();
            }
            e.printStackTrace();
        }

        return view;
    }

    private void fetchDriverData() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        if (driverId == null || driverId.isEmpty()) {
            Log.e(TAG, "Cannot fetch driver data: driverId is null or empty");
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error: Invalid driver ID", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        try {
            // First get location data from driverShareLocation
            DatabaseReference locationRef = FirebaseDatabase.getInstance()
                    .getReference("driverShareLocation")
                    .child(driverId);

            Log.d(TAG, "Fetching location data for driver ID: " + driverId);

            locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").exists() ? snapshot.child("name").getValue(String.class) : "Driver";
                        Double latitude = snapshot.child("latitude").exists() ? snapshot.child("latitude").getValue(Double.class) : null;
                        Double longitude = snapshot.child("longitude").exists() ? snapshot.child("longitude").getValue(Double.class) : null;

                        // Update the name field immediately
                        if (nameTextView != null) {
                            nameTextView.setText(name);
                        }

                        if (latitude != null && longitude != null) {
                            if (addressTextView != null) {
                                addressTextView.setText("Current Location: " + latitude + ", " + longitude);
                            }
                        }

                        fetchProfileData();
                    } else {
                        fetchProfileData();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching location data: " + error.getMessage());
                    fetchProfileData();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching driver data: " + e.getMessage());
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading driver data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchProfileData() {
        // Get detailed profile data from users folder
        try {
            DatabaseReference profileRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(driverId);

            Log.d(TAG, "Fetching profile data for driver ID: " + driverId);

            profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (loadingProgressBar != null) {
                        loadingProgressBar.setVisibility(View.GONE);
                    } else {
                        Log.e(TAG, "loadingProgressBar is null, cannot hide it");
                    }

                    if (snapshot.exists()) {
                        Log.d(TAG, "Profile data found");

                        // Extract profile data
                        String address = snapshot.child("homeAddress").exists() ?
                                snapshot.child("homeAddress").getValue(String.class) : null;
                        String carNumber = snapshot.child("carNumber").exists() ?
                                snapshot.child("carNumber").getValue(String.class) : null;
                        String caste = snapshot.child("caste").exists() ?
                                snapshot.child("caste").getValue(String.class) : null;
                        String name = snapshot.child("fullName").exists() ?
                                snapshot.child("fullName").getValue(String.class) : null;

                        // Only update name if it wasn't already set and we have a value
                        if (name != null && nameTextView != null && nameTextView.getText().toString().equals("Driver")) {
                            nameTextView.setText(name);
                        } else if (nameTextView == null) {
                            Log.e(TAG, "nameTextView is null, cannot update name");
                        }

                        // Update the address if we have a proper one (not coordinates)
                        if (address != null && addressTextView != null) {
                            addressTextView.setText(address);
                        } else if (addressTextView == null) {
                            Log.e(TAG, "addressTextView is null, cannot update address");
                        }

                        if (carNumberTextView != null) {
                            carNumberTextView.setText(carNumber != null ? carNumber : "Not provided");
                        } else {
                            Log.e(TAG, "carNumberTextView is null, cannot update car number");
                        }

                        if (casteTextView != null) {
                            casteTextView.setText(caste != null ? caste : "Not provided");
                        } else {
                            Log.e(TAG, "casteTextView is null, cannot update caste");
                        }

                        // Load profile image from storage
                        loadProfileImage();
                    } else {
                        Log.e(TAG, "Profile data not found for driver ID: " + driverId);
                        // Keep whatever data we already have displayed
                        if (addressTextView != null && !addressTextView.getText().toString().contains("Location")) {
                            addressTextView.setText("No address information available");
                        } else if (addressTextView == null) {
                            Log.e(TAG, "addressTextView is null, cannot update address");
                        }
                        if (carNumberTextView != null) {
                            carNumberTextView.setText("Not provided");
                        } else {
                            Log.e(TAG, "carNumberTextView is null, cannot update car number");
                        }
                        if (casteTextView != null) {
                            casteTextView.setText("Not provided");
                        } else {
                            Log.e(TAG, "casteTextView is null, cannot update caste");
                        }
                        if (loadingProgressBar != null) {
                            loadingProgressBar.setVisibility(View.GONE);
                        } else {
                            Log.e(TAG, "loadingProgressBar is null, cannot hide it");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (loadingProgressBar != null) {
                        loadingProgressBar.setVisibility(View.GONE);
                    } else {
                        Log.e(TAG, "loadingProgressBar is null, cannot hide it");
                    }
                    Log.e(TAG, "Error fetching profile data: " + error.getMessage());
                    Toast.makeText(getContext(), "Failed to load driver profile data", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error fetching profile data: " + e.getMessage());
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading driver profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfileImage() {
        if (driverId == null || driverId.isEmpty()) {
            Log.e(TAG, "Cannot load profile image: driver ID is null or empty");
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_images").child(driverId);

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            if (profileImageView != null && isAdded()) {
                Picasso.get().load(uri)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(profileImageView);
            } else {
                Log.e(TAG, "profileImageView is null or fragment not attached, cannot load profile image");
            }
        }).addOnFailureListener(e -> {
            // Use default image if profile image not available
            if (profileImageView != null && isAdded()) {
                profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
            } else {
                Log.e(TAG, "profileImageView is null or fragment not attached, cannot set default image");
            }
        });
    }

    private void checkExistingRideRequest() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot check ride requests: User not authenticated");
            return;
        }

        String passengerId = currentUser.getUid();
        DatabaseReference rideRequestsRef = FirebaseDatabase.getInstance().getReference("ride_requests");

        rideRequestsRef.orderByChild("passengerId").equalTo(passengerId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean requestFound = false;

                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            String requestDriverId = requestSnapshot.child("driverId").getValue(String.class);

                            if (requestDriverId != null && driverId != null && driverId.equals(requestDriverId)) {
                                requestFound = true;
                                currentRequestId = requestSnapshot.getKey();
                                currentRequestStatus = requestSnapshot.child("status").getValue(String.class);
                                if (currentRequestStatus != null) {
                                    updateRequestButton(currentRequestStatus);
                                } else {
                                    updateRequestButton("default");
                                }
                                break;
                            }
                        }

                        if (!requestFound && requestRideButton != null) {
                            requestRideButton.setText("Request Ride");
                            requestRideButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error checking ride requests: " + error.getMessage());
                    }
                });
    }

    private void updateRequestButton(String status) {
        if (requestRideButton == null) return;

        switch (status) {
            case "pending":
                requestRideButton.setText("Request Pending");
                requestRideButton.setEnabled(false);
                break;
            case "accepted":
                requestRideButton.setText("Request Accepted");
                requestRideButton.setEnabled(false);
                break;
            case "rejected":
                requestRideButton.setText("Request Rejected");
                requestRideButton.setEnabled(true);
                requestRideButton.setOnClickListener(v -> requestRide());
                break;
            default:
                requestRideButton.setText("Request Ride");
                requestRideButton.setEnabled(true);
                requestRideButton.setOnClickListener(v -> requestRide());
        }
    }

    private void requestRide() {
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot request ride");
            return;
        }

        if (driverId == null || driverId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid driver information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "You must be logged in to request a ride", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check connectivity first
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    proceedWithRequest();
                } else {
                    Toast.makeText(getContext(), "No internet connection. Please try again when online.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to send request: No internet connection");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Connection check failed. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Connection check failed: " + error.getMessage());
            }
        });
    }

    private void proceedWithRequest() {
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot proceed with request");
            return;
        }

        DatabaseReference rideRequestsRef = FirebaseDatabase.getInstance().getReference("ride_requests");
        String requestId = rideRequestsRef.push().getKey();

        // Check if user is authenticated
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to request a ride", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to send request: User not authenticated");
            return;
        }

        String passengerId = currentUser.getUid();

        if (driverId == null || driverId.isEmpty()) {
            Toast.makeText(getContext(), "Invalid driver information", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to send request: Driver ID is null or empty");
            return;
        }

        Log.d(TAG, "Attempting to send ride request. Driver ID: " + driverId);
        Log.d(TAG, "Passenger ID: " + passengerId);
        Log.d(TAG, "Database path: " + rideRequestsRef.toString());

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("passengerId", passengerId);
        requestData.put("driverId", driverId);
        requestData.put("status", "pending");
        requestData.put("timestamp", System.currentTimeMillis());

        if (requestId != null) {
            // Show a toast indicating the request is being processed
            Toast.makeText(getContext(), "Sending ride request...", Toast.LENGTH_SHORT).show();

            rideRequestsRef.child(requestId).setValue(requestData)
                    .addOnSuccessListener(aVoid -> {
                        if (getContext() == null) return;
                        Log.d(TAG, "Ride request sent successfully. Request ID: " + requestId);
                        Toast.makeText(getContext(), "Ride request sent to driver", Toast.LENGTH_SHORT).show();
                        currentRequestId = requestId;
                        currentRequestStatus = "pending";
                        updateRequestButton("pending");
                    })
                    .addOnFailureListener(e -> {
                        if (getContext() == null) return;
                        Log.e(TAG, "Failed to send request: " + e.getMessage(), e);
                        Toast.makeText(getContext(), "Failed to send request: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "Failed to create request ID");
            Toast.makeText(getContext(), "Failed to create request ID", Toast.LENGTH_SHORT).show();
        }
    }
}