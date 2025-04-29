package com.urooj.carpoolingapp.passengerui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.urooj.carpoolingapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ViewDriverProfile extends AppCompatActivity {
    private static final String TAG = "ViewDriverProfile";

    // UI components
    private TextView nameTextView, phoneTextView, emailTextView;
    private TextView carNumberTextView, addressTextView, ratingTextView;
    private ImageView profileImageView;
    private ProgressBar progressBar;
    private Button requestRideButton;
    private Toolbar toolbar;

    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_driver_profile);

        // Initialize UI components
        initializeViews();

        // Get driver ID from intent
        driverId = getIntent().getStringExtra("driverId");

        if (driverId != null) {
            Log.d(TAG, "Loading driver profile for ID: " + driverId);
            loadDriverProfile();
        } else {
            Log.e(TAG, "No driver ID provided");
            Toast.makeText(this, "Error: No driver ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up request ride button
        setupRequestRideButton();
    }

    private void initializeViews() {
        // Find all views
        toolbar = findViewById(R.id.toolbar);
        nameTextView = findViewById(R.id.driverName);
        phoneTextView = findViewById(R.id.driverPhone);
        emailTextView = findViewById(R.id.driverEmail);
        carNumberTextView = findViewById(R.id.driverCarNumber);
        addressTextView = findViewById(R.id.driverAddress);
        ratingTextView = findViewById(R.id.driverRating);
        profileImageView = findViewById(R.id.driverImage);
        progressBar = findViewById(R.id.progressBar);
        requestRideButton = findViewById(R.id.requestRideButton);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Driver Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadDriverProfile() {
        progressBar.setVisibility(View.VISIBLE);

        // First check driverShareLocation for recent data
        DatabaseReference locationRef = FirebaseDatabase.getInstance()
                .getReference("driverShareLocation").child(driverId);

        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get basic info from driver location data
                    String name = snapshot.child("name").exists() ?
                            snapshot.child("name").getValue(String.class) : "Driver";

                    nameTextView.setText(name);

                    // Now load full profile data
                    loadDetailedProfile();
                } else {
                    // No location data, try detailed profile directly
                    loadDetailedProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading location data: " + error.getMessage());
                loadDetailedProfile();
            }
        });
    }

    private void loadDetailedProfile() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(driverId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                if (snapshot.exists()) {
                    // Get detailed profile data
                    String name = snapshot.child("fullName").exists() ?
                            snapshot.child("fullName").getValue(String.class) : nameTextView.getText().toString();
                    String phone = snapshot.child("phoneNumber").exists() ?
                            snapshot.child("phoneNumber").getValue(String.class) : "Not available";
                    String email = snapshot.child("email").exists() ?
                            snapshot.child("email").getValue(String.class) : "Not available";
                    String carNumber = snapshot.child("carNumber").exists() ?
                            snapshot.child("carNumber").getValue(String.class) : "Not available";
                    String address = snapshot.child("homeAddress").exists() ?
                            snapshot.child("homeAddress").getValue(String.class) : "Not available";

                    // Update UI
                    nameTextView.setText(name);
                    phoneTextView.setText(phone);
                    emailTextView.setText(email);
                    carNumberTextView.setText(carNumber);
                    addressTextView.setText(address);

                    // Load profile image if exists
                    if (snapshot.child("profileImageUrl").exists()) {
                        String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(ViewDriverProfile.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .error(R.drawable.ic_profile_placeholder)
                                    .into(profileImageView);
                        }
                    } else {
                        // Try to load from storage using UID
                        loadProfileImageFromStorage();
                    }

                    // Display dummy rating for now
                    ratingTextView.setText("4.8");
                } else {
                    Toast.makeText(ViewDriverProfile.this, "Driver profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading profile: " + error.getMessage());
                Toast.makeText(ViewDriverProfile.this, "Error loading driver profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileImageFromStorage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_images").child(driverId);

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(ViewDriverProfile.this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(profileImageView);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading profile image: " + e.getMessage());
            // Use default image
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        });
    }

    private void setupRequestRideButton() {
        requestRideButton.setOnClickListener(v -> {
            requestRideButton.setText("Sending Request...");
            requestRideButton.setEnabled(false);

            // Check if user is logged in
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "You must be logged in to request a ride", Toast.LENGTH_SHORT).show();
                resetRequestButton();
                return;
            }

            String passengerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // First get passenger details for the notification
            DatabaseReference passengerRef = FirebaseDatabase.getInstance()
                    .getReference("users").child(passengerId);

            passengerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot passengerSnapshot) {
                    if (!passengerSnapshot.exists()) {
                        Toast.makeText(ViewDriverProfile.this, "Error: Passenger data not found", Toast.LENGTH_SHORT).show();
                        resetRequestButton();
                        return;
                    }

                    String passengerName = passengerSnapshot.child("fullName").getValue(String.class);
                    if (passengerName == null) passengerName = "A passenger";

                    // Create ride request
                    DatabaseReference requestsRef = FirebaseDatabase.getInstance()
                            .getReference("ride_requests");

                    String requestId = requestsRef.push().getKey();
                    if (requestId == null) {
                        Toast.makeText(ViewDriverProfile.this, "Failed to create request", Toast.LENGTH_SHORT).show();
                        resetRequestButton();
                        return;
                    }

                    Map<String, Object> requestData = new HashMap<>();
                    requestData.put("passengerId", passengerId);
                    requestData.put("driverId", driverId);
                    requestData.put("status", "pending");
                    requestData.put("timestamp", System.currentTimeMillis());
                    requestData.put("passengerName", passengerName);

                    // Save request to database
                    String finalPassengerName = passengerName;
                    requestsRef.child(requestId).setValue(requestData)
                            .addOnSuccessListener(aVoid -> {
                                // Request saved successfully, now send notification
                                sendRideRequestNotification(driverId, requestId, finalPassengerName);
                                requestRideButton.setText("Request Sent");
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ViewDriverProfile.this, "Failed to send request: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                resetRequestButton();
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ViewDriverProfile.this, "Error loading passenger data", Toast.LENGTH_SHORT).show();
                    resetRequestButton();
                }
            });
        });
    }

    private void resetRequestButton() {
        requestRideButton.setText("Request Ride");
        requestRideButton.setEnabled(true);
    }

    private void sendRideRequestNotification(String driverId, String requestId, String passengerName) {
        // Get driver's FCM token from database
        DatabaseReference driverRef = FirebaseDatabase.getInstance()
                .getReference("drivers").child(driverId);

        driverRef.child("fcmToken").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult().getValue() == null) {
                Log.e(TAG, "Couldn't get driver's FCM token");
                return;
            }

            String driverToken = task.getResult().getValue(String.class);

            try {
                JSONObject payload = new JSONObject();
                payload.put("to", driverToken);

                // Notification payload (for when app is in background)
                JSONObject notification = new JSONObject();
                notification.put("title", "New Ride Request");
                notification.put("body", passengerName + " is requesting a ride");

                // Data payload (for when app is in foreground)
                JSONObject data = new JSONObject();
                data.put("type", "ride_request");
                data.put("requestId", requestId);
                data.put("passengerName", passengerName);
                data.put("passengerId", FirebaseAuth.getInstance().getCurrentUser().getUid());

                payload.put("notification", notification);
                payload.put("data", data);

                // Send using Volley (which you already have in dependencies)
                sendFcmRequest(payload);

            } catch (JSONException e) {
                Log.e(TAG, "Failed to create FCM payload", e);
            }
        });
    }

    private void sendFcmRequest(JSONObject payload) {
        String FCM_URL = "https://fcm.googleapis.com/fcm/send";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                FCM_URL,
                payload,
                response -> Log.d(TAG, "Notification sent successfully"),
                error -> Log.e(TAG, "Failed to send notification: " + error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                //headers.put("Authorization", "key=" + getString(R.string.server_key));
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add to Volley request queue
        Volley.newRequestQueue(this).add(request);
    }
}