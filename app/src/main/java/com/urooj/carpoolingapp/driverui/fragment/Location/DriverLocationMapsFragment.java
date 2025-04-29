package com.urooj.carpoolingapp.driverui.fragment.Location;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.urooj.carpoolingapp.R;
import com.urooj.carpoolingapp.driverui.DriverModel.DriverShareLocation;

public class DriverLocationMapsFragment extends Fragment {

    private static final String TAG = "DriverLocationMapsFragment";

    private MapView mapView;
    private FloatingActionButton floatingActionButton;
    private MaterialButton shareLocationButton;

    private Point point;
    private DatabaseReference reference;
    private DriverShareLocation location;
    private PointAnnotationManager pointAnnotationManager;

    private ActivityResultLauncher<String> permissionLauncher;

    private final OnIndicatorBearingChangedListener bearingChangedListener = v -> {
        if (mapView != null && getContext() != null) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
        }
    };

    private final OnIndicatorPositionChangedListener positionChangedListener = point -> {
        if (mapView != null && getContext() != null) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(20.0).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
            this.point = point;
        }
    };

    private final OnMoveListener moveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector detector) {
            LocationComponentPlugin locationComponent = getLocationComponent(mapView);
            if (locationComponent != null) {
                locationComponent.removeOnIndicatorBearingChangedListener(bearingChangedListener);
                locationComponent.removeOnIndicatorPositionChangedListener(positionChangedListener);
                getGestures(mapView).removeOnMoveListener(this);
                if (floatingActionButton != null) {
                    floatingActionButton.show();
                }
            }
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector detector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector detector) {}
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        location = new DriverShareLocation();
        setupPermissionLauncher();
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        initializeMap();
                    } else {
                        Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_location_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        floatingActionButton = view.findViewById(R.id.focusLocation);
        shareLocationButton = view.findViewById(R.id.shareLocation);

        if (floatingActionButton == null || shareLocationButton == null) {
            Log.e(TAG, "Critical views not found in layout");
            requireActivity().finish();
            return;
        }

        floatingActionButton.hide();
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initializeMap();
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void initializeMap() {
        if (getContext() == null || mapView == null) return;

        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE, style -> {
            setupLocationComponent();
            setupMapCamera();
            setupAnnotations();
            setupButtons();
            setupFirebaseListener();
        });
    }

    private void setupLocationComponent() {
        LocationComponentPlugin locationComponent = getLocationComponent(mapView);
        if (locationComponent == null) return;

        locationComponent.setEnabled(true);

        LocationPuck2D locationPuck = new LocationPuck2D();
        locationPuck.setBearingImage(ContextCompat.getDrawable(requireContext(), R.drawable.mybaseline));
        locationComponent.setLocationPuck(locationPuck);

        locationComponent.addOnIndicatorPositionChangedListener(positionChangedListener);
        locationComponent.addOnIndicatorBearingChangedListener(bearingChangedListener);
        getGestures(mapView).addOnMoveListener(moveListener);
    }

    private void setupMapCamera() {
        if (mapView != null) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
        }
    }

    private void setupAnnotations() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.driver_car);
        if (bitmap == null) {
            Toast.makeText(requireContext(), "Marker image not found", Toast.LENGTH_SHORT).show();
            return;
        }

        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
        if (annotationPlugin != null) {
            pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());
        }
    }

    private void setupButtons() {
        floatingActionButton.setOnClickListener(v -> {
            LocationComponentPlugin locationComponent = getLocationComponent(mapView);
            if (locationComponent != null) {
                locationComponent.addOnIndicatorBearingChangedListener(bearingChangedListener);
                locationComponent.addOnIndicatorPositionChangedListener(positionChangedListener);
            }
            getGestures(mapView).addOnMoveListener(moveListener);
            floatingActionButton.hide();
        });

        shareLocationButton.setOnClickListener(v -> {
            if (point == null) {
                Toast.makeText(requireContext(), "Waiting for location...", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reference == null) {
                startSharingLocation();
            } else {
                stopSharingLocation();
            }
        });
    }

    private void startSharingLocation() {
        if (point == null) return;

        reference = FirebaseDatabase.getInstance().getReference("driverShareLocation").push();
        location.setId(reference.getKey());
        location.setName("Driver");
        location.setLongitude(point.longitude());
        location.setLatitude(point.latitude());
        reference.setValue(location);
        shareLocationButton.setText("Stop Sharing");
        Toast.makeText(requireContext(), "Sharing driver's location", Toast.LENGTH_SHORT).show();
    }

    private void stopSharingLocation() {
        if (reference != null) {
            reference.removeValue();
            reference = null;
        }
        shareLocationButton.setText("Share Location");
        Toast.makeText(requireContext(), "Stopped sharing location", Toast.LENGTH_SHORT).show();
    }

    private void setupFirebaseListener() {
        FirebaseDatabase.getInstance().getReference("driverShareLocation")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (pointAnnotationManager == null) return;

                        pointAnnotationManager.deleteAll();
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.driver_car);
                        if (bitmap == null) return;

                        for (DataSnapshot data : snapshot.getChildren()) {
                            DriverShareLocation loc = data.getValue(DriverShareLocation.class);
                            if (loc != null && !loc.getId().equals(location.getId())) {
                                PointAnnotationOptions options = new PointAnnotationOptions()
                                        .withTextAnchor(TextAnchor.CENTER)
                                        .withIconImage(bitmap)
                                        .withPoint(Point.fromLngLat(loc.getLongitude(), loc.getLatitude()));
                                pointAnnotationManager.create(options);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase error: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.onDestroy();
        if (reference != null) reference.removeValue();
        super.onDestroyView();
    }
}
