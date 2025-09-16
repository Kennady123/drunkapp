package com.example.drunk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlertFragment extends Fragment {

    private static final String PREF_NAME = "ALERT_PREF";
    private static final String ALERT_LIST_KEY = "alert_list";

    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        container = view.findViewById(R.id.alertContainer);
        loadAlerts();
    }

    private void loadAlerts() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, 0);
        String alertJson = prefs.getString(ALERT_LIST_KEY, "[]");

        try {
            JSONArray alerts = new JSONArray(alertJson);
            JSONArray recentAlerts = new JSONArray();

            long now = System.currentTimeMillis();
            long oneDayMillis = 24 * 60 * 60 * 1000;

            container.removeAllViews();

            for (int i = 0; i < alerts.length(); i++) {
                JSONObject alert = alerts.getJSONObject(i);

                String timestamp = alert.getString("timestamp");
                long alertTime = parseTimestampToMillis(timestamp);

                if ((now - alertTime) <= oneDayMillis) {
                    recentAlerts.put(alert);

                    View alertItem = LayoutInflater.from(getContext())
                            .inflate(R.layout.item_alert, container, false);

                    ((TextView) alertItem.findViewById(R.id.alertTitle))
                            .setText(alert.getString("title"));
                    ((TextView) alertItem.findViewById(R.id.alertMessage))
                            .setText(alert.getString("message"));
                    ((TextView) alertItem.findViewById(R.id.alertTimestamp))
                            .setText(timestamp);

                    // New: show latitude and longitude if available
                    TextView latView = alertItem.findViewById(R.id.alertLat);
                    TextView lonView = alertItem.findViewById(R.id.alertLon);

                    if (alert.has("lat") && alert.has("lon")) {
                        latView.setText("Lat: " + alert.getString("lat"));
                        lonView.setText("Lon: " + alert.getString("lon"));
                    } else {
                        latView.setText("Lat: N/A");
                        lonView.setText("Lon: N/A");
                    }

                    container.addView(alertItem);
                }
            }

            prefs.edit().putString(ALERT_LIST_KEY, recentAlerts.toString()).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long parseTimestampToMillis(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);
            return date != null ? date.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
