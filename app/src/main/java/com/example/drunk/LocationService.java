package com.example.drunk;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "location_channel";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForeground(1, createNotification()); // 🔔 Keep service alive
        startLocationUpdates();
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Updates",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Location")
                .setContentText("Your location is being updated in background")
                .setSmallIcon(R.drawable.ic_notification)
                .build();
    }

    private void startLocationUpdates() {
        LocationRequest request = LocationRequest.create()
                .setInterval(10000) // 10 sec
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    updateUserLocation(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
        }
    }

    private void updateUserLocation(Location location) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;

                    String token = task.getResult();
                    String userId = FirebaseAuth.getInstance().getUid();
                    if (userId == null) userId = "testUser";

                    FirebaseDatabase database = FirebaseDatabase.getInstance(
                            "https://drunkalertapp-default-rtdb.asia-southeast1.firebasedatabase.app/"
                    );

                    DatabaseReference ref = database.getReference("users").child(userId);
                    UserLocation userLocation = new UserLocation(
                            token,
                            location.getLatitude(),
                            location.getLongitude(),
                            System.currentTimeMillis()
                    );

                    ref.setValue(userLocation);
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class UserLocation {
        public String token;
        public double lat;
        public double lng;
        public long lastUpdated;

        public UserLocation() {}
        public UserLocation(String token, double lat, double lng, long lastUpdated) {
            this.token = token;
            this.lat = lat;
            this.lng = lng;
            this.lastUpdated = lastUpdated;
        }
    }
}
