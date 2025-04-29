package com.urooj.carpoolingapp.passengerui.fragment.notification;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.adapter.NotificationAdapter;
import com.urooj.carpoolingapp.passengerui.activity.ViewDriverProfile;
import com.urooj.carpoolingapp.passengerui.fragment.SharedRideFragment;
import com.urooj.carpoolingapp.passengerui.model.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationFragment extends Fragment implements NotificationAdapter.NotificationActionListener {
    private static final String TAG = "NotificationFragment";

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyView;
    private String passengerId;
    private DatabaseReference notificationsRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize data
        notificationList = new ArrayList<>();

        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            passengerId = currentUser.getUid();
            notificationsRef = FirebaseDatabase.getInstance().getReference("notifications").child(passengerId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        emptyView = view.findViewById(R.id.emptyView);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(notificationList, this);
        recyclerView.setAdapter(adapter);

        // Set up pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::fetchNotifications);

        // Fetch notifications
        fetchNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchNotifications();
    }

    private void fetchNotifications() {
        if (passengerId == null) {
            Log.e(TAG, "Cannot fetch notifications: User not authenticated");
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        notificationsRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot notificationSnap : snapshot.getChildren()) {
                    String id = notificationSnap.getKey();
                    String type = notificationSnap.child("type").getValue(String.class);
                    String title = notificationSnap.child("title").getValue(String.class);
                    String message = notificationSnap.child("message").getValue(String.class);
                    String driverId = notificationSnap.child("driverId").getValue(String.class);
                    String status = notificationSnap.child("status").getValue(String.class);
                    Boolean isRead = notificationSnap.child("read").getValue(Boolean.class);
                    Long timestampLong = notificationSnap.child("timestamp").getValue(Long.class);

                    long timestamp = (timestampLong != null) ? timestampLong : 0;
                    boolean read = (isRead != null) ? isRead : false;

                    Notification notification = new Notification(id, type, title, message, driverId, status, timestamp, read);
                    notificationList.add(notification);
                }

                // Sort by timestamp (newest first)
                Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                adapter.notifyDataSetChanged();

                // Toggle empty view visibility
                if (notificationList.isEmpty()) {
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
                Log.e(TAG, "Error fetching notifications: " + error.getMessage());
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Handle notification click (e.g., mark as read if not already)
        if (!notification.isRead()) {
            markAsRead(notification.getId());
        }

        // For ride request responses, handle based on status
        if ("ride_request_response".equals(notification.getType())) {
            if ("accepted".equals(notification.getStatus())) {
                openDriverProfile(notification.getDriverId());
            }
        }
    }

    @Override
    public void onPrimaryActionClick(Notification notification) {
        if ("ride_request_response".equals(notification.getType())) {
            if ("accepted".equals(notification.getStatus())) {
                // View driver profile
                openDriverProfile(notification.getDriverId());
            } else if ("rejected".equals(notification.getStatus())) {
                // Find another driver - navigate back to map
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SharedRideFragment())
                            .commit();
                }
            }
        }
    }

    @Override
    public void onSecondaryActionClick(Notification notification) {
        // Dismiss notification (just mark as read)
        markAsRead(notification.getId());
    }

    @Override
    public void markAsRead(String notificationId) {
        if (notificationId != null && passengerId != null) {
            notificationsRef.child(notificationId).child("read").setValue(true)
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification as read: " + e.getMessage()));
        }
    }

    private void openDriverProfile(String driverId) {
        if (driverId != null && getActivity() != null) {
            Intent intent = new Intent(getActivity(), ViewDriverProfile.class);
            intent.putExtra("driverId", driverId);
            startActivity(intent);
        }
    }
}