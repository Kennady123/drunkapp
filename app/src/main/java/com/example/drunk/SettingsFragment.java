package com.example.drunk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // Find logout preference
        Preference logoutPref = findPreference("logout");
        if (logoutPref != null) {
            // Change title text color to red
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

        // Handle other preferences
        Preference privacyPref = findPreference("privacy_policy");
        Preference termsPref = findPreference("terms_conditions");
        Preference aboutPref = findPreference("about_app");

        if (privacyPref != null) {
            privacyPref.setOnPreferenceClickListener(preference -> {
                // TODO: Handle Privacy Policy click
                return true;
            });
        }

        if (termsPref != null) {
            termsPref.setOnPreferenceClickListener(preference -> {
                // TODO: Handle Terms & Conditions click
                return true;
            });
        }

        if (aboutPref != null) {
            aboutPref.setOnPreferenceClickListener(preference -> {
                // TODO: Handle About App click
                return true;
            });
        }
    }
}
