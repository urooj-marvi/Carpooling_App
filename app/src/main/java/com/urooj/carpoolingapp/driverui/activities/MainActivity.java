package com.urooj.carpoolingapp.driverui.activities;

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
import com.urooj.carpoolingapp.driverui.fragment.Location.DriverLocationMapsFragment;
import com.urooj.carpoolingapp.driverui.fragment.Profile.DriverProfile;
import com.urooj.carpoolingapp.driverui.fragment.dashboard.DriverShareRoute;
import com.urooj.carpoolingapp.passengerui.fragment.notification.NotificationFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment_activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            loadFragment(new DriverShareRoute());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    selectedFragment = new DriverShareRoute();
                    break;
                case R.id.navigation_notifications:
                    selectedFragment = new NotificationFragment();
                    break;
                case R.id.navigation_location:
                    selectedFragment = new DriverLocationMapsFragment();
                    break;
                case R.id.navigation_profile:
                    selectedFragment = new DriverProfile();
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
                .replace(R.id.nav_host_fragment_activity_main, fragment)
                .addToBackStack(null) // adds the fragment to the back stack
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(); // go back to previous fragment
        } else {
            super.onBackPressed(); // close the app if no back stack
        }
    }
}
