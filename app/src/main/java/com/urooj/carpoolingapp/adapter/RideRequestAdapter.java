package com.urooj.carpoolingapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.driverui.DriverModel.RideRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideRequestAdapter extends RecyclerView.Adapter<RideRequestAdapter.RequestViewHolder> {

    private List<RideRequest> requestList;
    private OnRequestActionListener actionListener;

    public interface OnRequestActionListener {
        void onRequestAction(String requestId, String action);
    }

    public RideRequestAdapter(List<RideRequest> requestList, OnRequestActionListener actionListener) {
        this.requestList = requestList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RideRequest request = requestList.get(position);

        // Format timestamp to readable date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(request.getTimestamp()));

        // Get passenger details
        fetchPassengerDetails(request.getPassengerId(), holder);

        holder.requestTimeTextView.setText(formattedDate);
        holder.requestStatusTextView.setText("Status: " + capitalize(request.getStatus()));

        // Manage button visibility based on status
        if ("pending".equals(request.getStatus())) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);
        } else {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
        }

        // Set button click listeners
        holder.acceptButton.setOnClickListener(v ->
                actionListener.onRequestAction(request.getRequestId(), "accepted"));

        holder.rejectButton.setOnClickListener(v ->
                actionListener.onRequestAction(request.getRequestId(), "rejected"));
    }

    private void fetchPassengerDetails(String passengerId, RequestViewHolder holder) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(passengerId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String passengerName = snapshot.child("name").exists() ?
                            snapshot.child("name").getValue(String.class) : "Unknown Passenger";
                    holder.passengerNameTextView.setText(passengerName);
                } else {
                    holder.passengerNameTextView.setText("Unknown Passenger");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.passengerNameTextView.setText("Unknown Passenger");
            }
        });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView passengerNameTextView;
        TextView requestTimeTextView;
        TextView requestStatusTextView;
        Button acceptButton;
        Button rejectButton;

        RequestViewHolder(View itemView) {
            super(itemView);
            passengerNameTextView = itemView.findViewById(R.id.passengerNameTextView);
            requestTimeTextView = itemView.findViewById(R.id.requestTimeTextView);
            requestStatusTextView = itemView.findViewById(R.id.requestStatusTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }
}