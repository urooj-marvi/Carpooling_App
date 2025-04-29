package com.urooj.carpoolingapp.driverui.fragment.RequestReceive;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.adapter.RideRequestAdapter;
import com.urooj.carpoolingapp.driverui.DriverModel.RideRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestNotificationsFragment extends Fragment {

    private static final String TAG = "RequestNotifications";
    private DatabaseReference rideRequestsRef;
    private String driverId;
    private RecyclerView recyclerView;
    private RideRequestAdapter adapter;
    private List<RideRequest> rideRequestList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private View rootView;

    // Track already seen requests to avoid showing duplicates
    private final Map<String, Boolean> seenRequests = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rideRequestsRef = FirebaseDatabase.getInstance().getReference("ride_requests");
        rideRequestList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_request_notifications, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.requestRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RideRequestAdapter(rideRequestList, this::handleRequestResponse);
        recyclerView.setAdapter(adapter);

        // Get empty view
        emptyView = view.findViewById(R.id.emptyView);

        // Set up pull-to-refresh
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            rideRequestList.clear();
            adapter.notifyDataSetChanged();
            listenForRideRequests();
        });

        listenForRideRequests();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (rideRequestList != null) {
            rideRequestList.clear();
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            listenForRideRequests();
        }
    }

    private void listenForRideRequests() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        rideRequestsRef.orderByChild("driverId").equalTo(driverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        rideRequestList.clear();
                        Log.d(TAG, "Received " + snapshot.getChildrenCount() + " ride requests");

                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            String requestId = requestSnapshot.getKey();
                            String passengerId = requestSnapshot.child("passengerId").getValue(String.class);
                            String status = requestSnapshot.child("status").getValue(String.class);
                            Long timestampLong = requestSnapshot.child("timestamp").getValue(Long.class);
                            long timestamp = timestampLong != null ? timestampLong : 0;

                            RideRequest request = new RideRequest(requestId, passengerId, driverId, status, timestamp);
                            rideRequestList.add(request);

                            // Show new pending requests as dialog, but only if we haven't seen them before
                            if ("pending".equals(status) && !seenRequests.containsKey(requestId)) {
                                seenRequests.put(requestId, true);
                                fetchPassengerDetails(passengerId, requestId);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        // Show empty view if no requests
                        if (rideRequestList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                        Toast.makeText(requireContext(), "Failed to load ride requests", Toast.LENGTH_SHORT).show();

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

    private void fetchPassengerDetails(String passengerId, String requestId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(passengerId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passengerName = snapshot.child("fullName").exists() ?
                            snapshot.child("fullName").getValue(String.class) :
                            snapshot.child("name").exists() ?
                                    snapshot.child("name").getValue(String.class) : "Unknown Passenger";

                    String passengerAddress = snapshot.child("homeAddress").exists() ?
                            snapshot.child("homeAddress").getValue(String.class) :
                            snapshot.child("address").exists() ?
                                    snapshot.child("address").getValue(String.class) : "Address not available";

                    String passengerPhone = snapshot.child("phoneNumber").exists() ?
                            snapshot.child("phoneNumber").getValue(String.class) :
                            snapshot.child("phone").exists() ?
                                    snapshot.child("phone").getValue(String.class) : "Phone not available";

                    showRideRequestDialog(requestId, passengerId, passengerName, passengerAddress, passengerPhone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching passenger details: " + error.getMessage());
                Toast.makeText(requireContext(), "Failed to load passenger details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRideRequestDialog(String requestId, String passengerId, String passengerName,
                                       String passengerAddress, String passengerPhone) {
        if (getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("New Ride Request")
                .setMessage("You have a new ride request from:\n\n" +
                        "Passenger: " + passengerName + "\n" +
                        "Phone: " + passengerPhone + "\n" +
                        "Address: " + passengerAddress)
                .setPositiveButton("Accept", (dialog, which) -> {
                    handleRequestResponse(requestId, "accepted");
                    notifyPassenger(passengerId, "accepted", passengerName);
                })
                .setNegativeButton("Reject", (dialog, which) -> {
                    handleRequestResponse(requestId, "rejected");
                    notifyPassenger(passengerId, "rejected", passengerName);
                })
                .setCancelable(false)
                .show();
    }

    private void handleRequestResponse(String requestId, String response) {
        rideRequestsRef.child(requestId).child("status").setValue(response)
                .addOnSuccessListener(aVoid -> {
                    String message = response.equals("accepted") ? "Ride Accepted" : "Ride Rejected";
                    Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update request status: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to update request status", Toast.LENGTH_SHORT).show();
                });
    }

    private void notifyPassenger(String passengerId, String status, String passengerName) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                .getReference("notifications").child(passengerId);

        String notificationId = notificationsRef.push().getKey();
        if (notificationId == null) return;

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ride_request_response");
        notification.put("status", status);
        notification.put("driverId", driverId);
        notification.put("timestamp", ServerValue.TIMESTAMP);
        notification.put("read", false);
        notification.put("title", "Ride Request " + (status.equals("accepted") ? "Accepted" : "Rejected"));
        notification.put("message", "Your ride request has been " + status +
                (status.equals("accepted") ? ". The driver will pick you up soon." : ". Please try another driver."));

        notificationsRef.child(notificationId).setValue(notification)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send notification to passenger: " + e.getMessage()));

        // Also store in ride_updates for easier tracking
        DatabaseReference updatesRef = FirebaseDatabase.getInstance()
                .getReference("ride_updates").child(passengerId);

        updatesRef.child(driverId).setValue(notification)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update ride status: " + e.getMessage()));
    }
}