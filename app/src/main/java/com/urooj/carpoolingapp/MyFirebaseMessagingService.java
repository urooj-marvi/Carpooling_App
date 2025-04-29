package com.urooj.carpoolingapp;

import android.content.Intent;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.urooj.carpoolingapp.passengerui.fragment.notification.NotificationFragment;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token: " + token);

        // You can send this token to your server or Firebase Realtime Database
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String type = remoteMessage.getData().get("type");
            if ("ride_request".equals(type)) {
                // Extract data from message
                String requestId = remoteMessage.getData().get("requestId");
                String passengerName = remoteMessage.getData().get("passengerName");

                // Create intent to open when notification is tapped
                Intent intent = new Intent(this, NotificationFragment.class);
                intent.putExtra("requestId", requestId);
                intent.putExtra("passengerName", passengerName);

                // Build and show notification
                showNotification(
                        "New Ride Request",
                        passengerName + " is requesting a ride",
                        intent
                );
            }
        }
    }

    private void showNotification(String title, String body, Intent intent) {
        // ... use the notification building code from previous examples ...
    }
}
