package com.urooj.carpoolingapp.passengerui.activity;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.passengerui.fragment.dashbord.PassengerDashbordFragment;
import com.urooj.carpoolingapp.passengerui.fragment.location.LocationMapsFragment;
import com.urooj.carpoolingapp.passengerui.fragment.notification.NotificationFragment;
import com.urooj.carpoolingapp.passengerui.fragment.profile.PassengerProfile;

public class PassengerActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_passenger);

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_Navigation);
        fragmentManager = getSupportFragmentManager();

        // Load the default fragment
        if (savedInstanceState == null) {
            loadFragment(new PassengerDashbordFragment());
        }

        // Set up the bottom navigation listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            switch (id) {
                case R.id.navigation_passengerdashboard:
                    selectedFragment = new PassengerDashbordFragment();
                    break;
                case R.id.navigation_passengernotifications:
                    selectedFragment = new NotificationFragment();
                    break;
                case R.id.navigation_passengerlocation:
                    selectedFragment = new LocationMapsFragment();
                    break;
                case R.id.navigation_passengerprofile:
                    selectedFragment = new PassengerProfile();
                    break;
                default:
                    return false;
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Add to back stack
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(); // Pop the back stack
        } else {
            super.onBackPressed(); // Exit the app if no fragments in the back stack
        }
    }
}