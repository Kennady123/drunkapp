package com.example.drunk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "alert_channel";
    private static final String PREF_NAME = "ALERT_PREF";
    private static final String ALERT_LIST_KEY = "alert_list";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage rm) {
        super.onMessageReceived(rm);

        String title = "Alert";
        String message = "New alert received";
        String lat = "";
        String lon = "";

        // Prefer data payload
        if (!rm.getData().isEmpty()) {
            title = rm.getData().get("title") != null ? rm.getData().get("title") : title;
            message = rm.getData().get("message") != null ? rm.getData().get("message") : message;
            lat = rm.getData().get("lat") != null ? rm.getData().get("lat") : "";
            lon = rm.getData().get("lon") != null ? rm.getData().get("lon") : "";
        } else if (rm.getNotification() != null) {
            if (rm.getNotification().getTitle() != null) title = rm.getNotification().getTitle();
            if (rm.getNotification().getBody() != null) message = rm.getNotification().getBody();
        }

        String ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

        saveAlertToPrefs(title, message, lat, lon, ts);
        showNotification(title, message);
    }

    private void saveAlertToPrefs(String title, String message, String lat, String lon, String timestamp) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String alertJson = prefs.getString(ALERT_LIST_KEY, "[]");

        try {
            JSONArray alerts = new JSONArray(alertJson);
            JSONObject newAlert = new JSONObject();
            newAlert.put("title", title);
            newAlert.put("message", message);
            newAlert.put("lat", lat);
            newAlert.put("lon", lon);
            newAlert.put("timestamp", timestamp);
            alerts.put(newAlert);

            prefs.edit().putString(ALERT_LIST_KEY, alerts.toString()).apply();
        } catch (Exception e) {
            Log.e("FCM", "Failed to save alert", e);
        }
    }

    private void showNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager mgr = getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(ch);
        }

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int flags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManagerCompat.from(this).notify(
                (int) System.currentTimeMillis(),
                b.build()
        );
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        String userId;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            userId = prefs.getString("device_id", null);
            if (userId == null) {
                userId = UUID.randomUUID().toString();
                prefs.edit().putString("device_id", userId).apply();
            }
        }

        final String finalUserId = userId; // ✅ Make it effectively final

        DatabaseReference locRef = FirebaseDatabase.getInstance(
                        "https://drunkalertapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("locations")
                .child(finalUserId);

        locRef.child("fcmToken").setValue(token)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved for " + finalUserId))
                .addOnFailureListener(e -> Log.e("FCM", "Failed to save token", e));
    }}

