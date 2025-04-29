package com.urooj.carpoolingapp.driverui.fragment.RequestReceive;

import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.adapter.RideRequestAdapter;
import com.urooj.carpoolingapp.driverui.DriverModel.RideRequest;
import java.util.ArrayList;
import java.util.List;

public class RequestNotificationsFragment extends Fragment {

    private DatabaseReference rideRequestsRef;
    private String driverId;
    private RecyclerView recyclerView;
    private RideRequestAdapter adapter;
    private List<RideRequest> rideRequestList;

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
        return inflater.inflate(R.layout.fragment_request_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.requestRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RideRequestAdapter(rideRequestList, this::handleRequestResponse);
        recyclerView.setAdapter(adapter);

        // Get empty view
        TextView emptyView = view.findViewById(R.id.emptyView);

        listenForRideRequests(emptyView);
    }

    private void listenForRideRequests(TextView emptyView) {
        rideRequestsRef.orderByChild("driverId").equalTo(driverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        rideRequestList.clear();

                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            String requestId = requestSnapshot.getKey();
                            String passengerId = requestSnapshot.child("passengerId").getValue(String.class);
                            String status = requestSnapshot.child("status").getValue(String.class);
                            Long timestampLong = requestSnapshot.child("timestamp").getValue(Long.class);
                            long timestamp = timestampLong != null ? timestampLong : 0;

                            RideRequest request = new RideRequest(requestId, passengerId, driverId, status, timestamp);
                            rideRequestList.add(request);

                            // Show new pending requests as dialog
                            if ("pending".equals(status)) {
                                fetchPassengerDetails(passengerId, requestId);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        // Show empty view if no requests
                        if (rideRequestList.isEmpty() && emptyView != null) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        } else if (emptyView != null) {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "Failed to load ride requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPassengerDetails(String passengerId, String requestId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(passengerId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passengerName = snapshot.child("name").exists() ?
                            snapshot.child("name").getValue(String.class) : "Unknown Passenger";
                    String passengerAddress = snapshot.child("address").exists() ?
                            snapshot.child("address").getValue(String.class) : "Address not available";

                    showRideRequestDialog(requestId, passengerId, passengerName, passengerAddress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load passenger details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRideRequestDialog(String requestId, String passengerId, String passengerName, String passengerAddress) {
        new AlertDialog.Builder(requireContext())
                .setTitle("New Ride Request")
                .setMessage("You have a new ride request from:\n\n" +
                        "Passenger: " + passengerName + "\n" +
                        "Address: " + passengerAddress)
                .setPositiveButton("Accept", (dialog, which) -> {
                    handleRequestResponse(requestId, "accepted");
                })
                .setNegativeButton("Reject", (dialog, which) -> {
                    handleRequestResponse(requestId, "rejected");
                })
                .setCancelable(false)
                .show();
    }

    private void handleRequestResponse(String requestId, String response) {
        rideRequestsRef.child(requestId).child("status").setValue(response)
                .addOnSuccessListener(aVoid -> {
                    String message = response.equals("accepted") ? "Ride Accepted" : "Ride Rejected";
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update request status", Toast.LENGTH_SHORT).show();
                });
    }
}