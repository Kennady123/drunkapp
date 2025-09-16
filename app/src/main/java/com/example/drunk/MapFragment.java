package com.example.drunk;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private TextView tvLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String> finePermissionLauncher;

    private DatabaseReference dbRefLocations;
    private String userId;
    private boolean isFirstCameraMove = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mapView = root.findViewById(R.id.mapView);
        tvLocation = root.findViewById(R.id.tvLocation);

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        MapsInitializer.initialize(requireContext().getApplicationContext());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Firebase DB reference
        dbRefLocations = FirebaseDatabase.getInstance(
                        "https://drunkalertapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("locations");

        // User ID: Firebase UID or device UUID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            SharedPreferences prefs = requireContext().getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);
            userId = prefs.getString("device_id", null);
            if (userId == null) {
                userId = UUID.randomUUID().toString();
                prefs.edit().putString("device_id", userId).apply();
            }
        }

        // Permission launcher
        finePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted && googleMap != null) {
                        enableMyLocation();
                        startLocationUpdates();
                    }
                });

        // FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::saveTokenToFirebase);

        return root;
    }

    private void saveTokenToFirebase(String token) {
        if (token == null || userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", token);
        dbRefLocations.child(userId).updateChildren(data);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Check permission and start updates
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            startLocationUpdates();
        } else {
            finePermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableMyLocation() {
        if (googleMap != null &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void startLocationUpdates() {
        // New LocationRequest API
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null && googleMap != null) {
                    LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());

                    if (isFirstCameraMove) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 15f));
                        isFirstCameraMove = false;
                    }

                    tvLocation.setText(String.format(Locale.getDefault(),
                            "Lat: %.5f\nLng: %.5f",
                            loc.getLatitude(),
                            loc.getLongitude()));

                    saveLocationToFirebase(loc);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
        }
    }

    private void saveLocationToFirebase(Location location) {
        if (location == null || userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("latitude", location.getLatitude());
        data.put("longitude", location.getLongitude());
        data.put("last_updated", ServerValue.TIMESTAMP);

        dbRefLocations.child(userId).updateChildren(data);
    }

    // MapView lifecycle
    @Override public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override public void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); if (fusedLocationClient != null && locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback); }
    @Override public void onStop() { super.onStop(); if (fusedLocationClient != null && locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback); }
    @Override public void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
}
