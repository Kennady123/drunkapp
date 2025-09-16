package com.example.drunk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // 🔴 Logout
        Preference logoutPref = findPreference("logout");
        if (logoutPref != null) {
            SpannableString redTitle = new SpannableString("Logout");
            redTitle.setSpan(new ForegroundColorSpan(Color.RED), 0, redTitle.length(), 0);
            logoutPref.setTitle(redTitle);

            logoutPref.setOnPreferenceClickListener(preference -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            });
        }

        // 📖 Privacy Policy - Open URL in browser
        Preference privacyPref = findPreference("privacy_policy");
        if (privacyPref != null) {
            privacyPref.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://yourdomain.com/privacy-policy"));
                startActivity(browserIntent);
                return true;
            });
        }

        // 📜 Terms & Conditions - Open URL in browser
        Preference termsPref = findPreference("terms_conditions");
        if (termsPref != null) {
            termsPref.setOnPreferenceClickListener(preference -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://yourdomain.com/terms-and-conditions"));
                startActivity(browserIntent);
                return true;
            });
        }

        // 🧾 About - Show dialog
        Preference aboutPref = findPreference("about_app");
        if (aboutPref != null) {
            aboutPref.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("About")
                        .setMessage("App version 1.0\nDeveloped by YourName")
                        .setPositiveButton("OK", null)
                        .show();
                return true;
            });
        }

        // 🌙 Theme Switching
        ListPreference themePref = findPreference("theme");
        if (themePref != null) {
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String selectedTheme = newValue.toString();
                int mode = AppCompatDelegate.MODE_NIGHT_NO;
                if ("dark".equals(selectedTheme)) {
                    mode = AppCompatDelegate.MODE_NIGHT_YES;
                } else if ("system".equals(selectedTheme)) {
                    mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }
                AppCompatDelegate.setDefaultNightMode(mode);
                return true;
            });
        }

        // 📍 Location Access
        SwitchPreferenceCompat locationPref = findPreference("location_access");
        if (locationPref != null) {
            locationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isEnabled = (Boolean) newValue;
                if (isEnabled) {
                    if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
                    }
                }
                return true;
            });
        }
    }
}
