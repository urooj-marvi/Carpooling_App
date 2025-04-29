package com.urooj.carpoolingapp.passengerui.fragment.dashbord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.passengerui.fragment.SharedRideFragment;
import com.urooj.carpoolingapp.passengerui.fragment.PersonalRideFragment;

public class PassengerDashbordFragment extends Fragment {

    private RadioGroup radioGroupRideType;
    private Button btnFindRide;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_passenger_dashbord, container, false);

        // Initialize views
        radioGroupRideType = view.findViewById(R.id.radioGroupRideType);
        btnFindRide = view.findViewById(R.id.btnFindRide);

        // Set click listener for the "Find Ride" button
        btnFindRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroupRideType.getCheckedRadioButtonId();

                if (selectedId == -1) {
                    // No ride type selected
                    Toast.makeText(getActivity(), "Please select a ride type", Toast.LENGTH_SHORT).show();
                } else {
                    // Navigate to the corresponding fragment based on the selected ride type
                    Fragment fragment = null;
                    if (selectedId == R.id.radioSharedRide) {
                        fragment = new SharedRideFragment();
                    } else if (selectedId == R.id.radioPersonalRide) {
                        fragment = new PersonalRideFragment();
                    }

                    if (fragment != null) {
                        FragmentManager fragmentManager = getParentFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, fragment); // Replace 'fragment_container' with your container ID
                        fragmentTransaction.addToBackStack(null); // Optional: Add to back stack
                        fragmentTransaction.commit();
                    }
                }
            }
        });

        return view;
    }
}