package com.example.drunk;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private static final int REQ_NOTIF = 101;
    private static final int REQ_FINE = 102;
    private static final String PREF_NAME = "APP_PREF";
    private static final String DEVICE_ID_KEY = "device_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) f = new HomeFragment();
            else if (id == R.id.nav_alert) f = new AlertFragment();
            else if (id == R.id.nav_settings) f = new SettingsFragment();
            else if (id == R.id.nav_profile) f = new ProfileFragment();
            return loadFragment(f);
        });

        requestNotificationPermissionIfNeeded();
        requestFineLocationPermission();
        fetchAndSaveFCMToken();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment == null) return false;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboard_fragment_container, fragment)
                .commit();
        return true;
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
        }
    }

    private void requestFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_FINE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_FINE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQ_FINE) {
            Toast.makeText(this, "Location permission required.", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchAndSaveFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) return;
                    String token = task.getResult();

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    String userId;
                    SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        // ✅ Real user is signed in
                        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        prefs.edit().putBoolean("IS_LOGGED_IN", true).apply();
                    } else {
                        // ❌ Do NOT mark logged in here, just generate device ID
                        userId = prefs.getString(DEVICE_ID_KEY, null);
                        if (userId == null) {
                            userId = UUID.randomUUID().toString();
                            prefs.edit().putString(DEVICE_ID_KEY, userId).apply();
                        }
                        // Leave IS_LOGGED_IN = false
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("timestamp", System.currentTimeMillis());

                    firestore.collection("tokens").document(userId).set(data);
                });
    }
}
