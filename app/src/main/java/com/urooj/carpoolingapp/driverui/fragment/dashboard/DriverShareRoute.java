package com.urooj.carpoolingapp.driverui.fragment.dashboard;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.api.directions.v5.models.VoiceInstructions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.navigation.ui.voice.api.MapboxSpeechApi;
import com.mapbox.navigation.ui.voice.api.MapboxVoiceInstructionsPlayer;
import com.mapbox.navigation.ui.voice.model.SpeechAnnouncement;
import com.mapbox.navigation.ui.voice.model.SpeechError;
import com.mapbox.navigation.ui.voice.model.SpeechValue;
import com.mapbox.navigation.ui.voice.model.SpeechVolume;
import com.mapbox.navigation.ui.voice.view.MapboxSoundButton;
import com.urooj.carpoolingapp.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
public class DriverShareRoute extends Fragment {
    private MapView mapView;
    private MaterialButton setRoute;
    private FloatingActionButton focusLocationBtn;
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;
    private boolean focusLocation = true;
    private MapboxNavigation mapboxNavigation;
    private MapboxSpeechApi speechApi;
    private MapboxVoiceInstructionsPlayer mapboxVoiceInstructionsPlayer;
    private boolean isVoiceInstructionsMuted = false;
    private PointAnnotationManager pointAnnotationManager;
    private Bitmap bitmap;
    private DatabaseReference databaseReference;


    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {
        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);

            if (focusLocation) {
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
            }
        }
    };

    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(),
                    routeLineErrorRouteSetValueExpected -> mapView.getMapboxMap().getStyle(style -> {
                        routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                    }));
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapView).removeOnMoveListener(this);
            focusLocationBtn.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
        }
    };

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(getContext(), "Permission granted! Restart this app", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private final MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> speechCallback = new MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>>() {
        @Override
        public void accept(Expected<SpeechError, SpeechValue> speechErrorSpeechValueExpected) {
            speechErrorSpeechValueExpected.fold(new Expected.Transformer<SpeechError, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechError input) {
                    mapboxVoiceInstructionsPlayer.play(input.getFallback(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            }, new Expected.Transformer<SpeechValue, Unit>() {
                @NonNull
                @Override
                public Unit invoke(@NonNull SpeechValue input) {
                    mapboxVoiceInstructionsPlayer.play(input.getAnnouncement(), voiceInstructionsPlayerCallback);
                    return Unit.INSTANCE;
                }
            });
        }
    };

    private final MapboxNavigationConsumer<SpeechAnnouncement> voiceInstructionsPlayerCallback = new MapboxNavigationConsumer<SpeechAnnouncement>() {
        @Override
        public void accept(SpeechAnnouncement speechAnnouncement) {
            speechApi.clean(speechAnnouncement);
        }
    };

    private final VoiceInstructionsObserver voiceInstructionsObserver = new VoiceInstructionsObserver() {
        @Override
        public void onNewVoiceInstructions(@NonNull VoiceInstructions voiceInstructions) {
            speechApi.generate(voiceInstructions, speechCallback);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_share_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.mapView);
        focusLocationBtn = view.findViewById(R.id.focusLocation);
        setRoute = view.findViewById(R.id.setRoute);
        MaterialButton clearRouteButton = view.findViewById(R.id.clearRoute);
        databaseReference = FirebaseDatabase.getInstance().getReference("driverRoutes");

        clearRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapboxNavigation.setNavigationRoutes(Collections.emptyList());
                Toast.makeText(getContext(), "Route cleared", Toast.LENGTH_SHORT).show();
            }
        });

        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(requireContext())
                .withRouteLineResources(new RouteLineResources.Builder().build())
                .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER)
                .build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        speechApi = new MapboxSpeechApi(requireContext(), getString(R.string.mapbox_access_token), Locale.US.toLanguageTag());
        mapboxVoiceInstructionsPlayer = new MapboxVoiceInstructionsPlayer(requireContext(), Locale.US.toLanguageTag());

        NavigationOptions navigationOptions = new NavigationOptions.Builder(requireContext())
                .accessToken(getString(R.string.mapbox_access_token))
                .build();

        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);

        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver);

        MapboxSoundButton soundButton = view.findViewById(R.id.soundButton);
        soundButton.unmute();
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVoiceInstructionsMuted = !isVoiceInstructionsMuted;
                if (isVoiceInstructionsMuted) {
                    soundButton.muteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(0f));
                } else {
                    soundButton.unmuteAndExtend(1500L);
                    mapboxVoiceInstructionsPlayer.volume(new SpeechVolume(1f));
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            mapboxNavigation.startTripSession();
        }

        focusLocationBtn.hide();
        getGestures(mapView).addOnMoveListener(onMoveListener);

        setRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Please select a location in map", Toast.LENGTH_SHORT).show();

            }
        });

        mapView.getMapboxMap().loadStyleUri(Style.SATELLITE, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(20.0).build());
                LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
                locationComponentPlugin.setEnabled(true);
                locationComponentPlugin.setLocationProvider(navigationLocationProvider);
                getGestures(mapView).addOnMoveListener(onMoveListener);
                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                    @Override
                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                        locationComponentSettings.setEnabled(true);
                        locationComponentSettings.setPulsingEnabled(true);
                        return null;
                    }
                });

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.locationpin);
                AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, mapView);

                addOnMapClickListener(mapView.getMapboxMap(), new OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull Point destinationPoint) {
                        pointAnnotationManager.deleteAll();

                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                                .withTextAnchor(TextAnchor.CENTER)
                                .withIconImage(bitmap)
                                .withPoint(destinationPoint);
                        pointAnnotationManager.create(pointAnnotationOptions);

                        // Get last known location from navigationLocationProvider
                        Location currentLocation = navigationLocationProvider.getLastLocation();
                        if (currentLocation != null) {
                            Point originPoint = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());

                            setRoute.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    fetchRoute(originPoint, destinationPoint);
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Current location not available", Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                });

                focusLocationBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusLocation = true;
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        focusLocationBtn.hide();
                    }
                });
            }
        });
    }
    private void saveRouteToFirebase(Point origin, Point destination) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverRoutes");
        String passengerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (passengerId == null) {
            Log.e("FIREBASE", "User not authenticated. Cannot save route.");
            return;
        }

        Map<String, Object> routeData = new HashMap<>();
        routeData.put("originLat", origin.latitude());
        routeData.put("originLng", origin.longitude());
        routeData.put("destinationLat", destination.latitude());
        routeData.put("destinationLng", destination.longitude());
        routeData.put("timestamp", ServerValue.TIMESTAMP); // Optional: track when the route was updated

        // This line will overwrite any existing route for this user
        ref.child(passengerId).setValue(routeData)
                .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Route updated successfully in Firebase"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to update route in Firebase", e));
    }

    private void updateCamera(Point point, Double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(point)
                .zoom(18.0)
                .bearing(bearing)
                .pitch(45.0)
                .padding(new EdgeInsets(1000.0, 0.0, 0.0, 0.0))
                .build();

        getCamera(mapView).easeTo(cameraOptions, animationOptions);
    }

    @SuppressLint("MissingPermission")
    private void fetchRoute(Point originPoint, Point destinationPoint) {
        RouteOptions routeOptions = RouteOptions.builder()
                .coordinatesList(Arrays.asList(originPoint, destinationPoint))
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .bearingsList(Arrays.asList(
                        Bearing.builder()
                                .angle(0.0)
                                .degrees(45.0)
                                .build(),
                        null
                ))
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .steps(true)
                .voiceInstructions(true)
                .bannerInstructions(true)
                .language("en")
                .continueStraight(true)
                .build();

        setRoute.setEnabled(false);
        setRoute.setText("Fetching route...");

        mapboxNavigation.requestRoutes(routeOptions, new NavigationRouterCallback() {
            @Override
            public void onRoutesReady(@NonNull List<NavigationRoute> routes, @NonNull RouterOrigin routerOrigin) {
                mapboxNavigation.setNavigationRoutes(routes);
                focusLocationBtn.performClick();
                setRoute.setEnabled(true);
                setRoute.setText("Set route");

                // ⬇️ ADD THIS to save the route after successfully fetching it:
                saveRouteToFirebase(originPoint, destinationPoint);
            }

            @Override
            public void onFailure(@NonNull List<RouterFailure> reasons, @NonNull RouteOptions routeOptions) {
                for (RouterFailure failure : reasons) {
                    Log.e("ROUTE_FAILURE", failure.getMessage());
                }
                Toast.makeText(getContext(), "Route request failed", Toast.LENGTH_SHORT).show();
                setRoute.setEnabled(true);
                setRoute.setText("Set route");
            }

            @Override
            public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {
                Log.w("ROUTE_CANCELED", "Route request was canceled");
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Cancel everything safely
        if (mapboxNavigation != null) {
            mapboxNavigation.unregisterRoutesObserver(routesObserver);
            mapboxNavigation.unregisterLocationObserver(locationObserver);
            mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver);

            // Stop the trip session properly
            mapboxNavigation.stopTripSession();

            mapboxNavigation.onDestroy();
        }

        if (routeLineApi != null) {
            routeLineApi.cancel();
        }
        if (routeLineView != null) {
            routeLineView.cancel();
        }
        if (mapboxVoiceInstructionsPlayer != null) {
            mapboxVoiceInstructionsPlayer.shutdown();
        }
        if (speechApi != null) {
            speechApi.cancel();
        }

        if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll();
        }
    }
}