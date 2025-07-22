package com.example.drunk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AlertFragment extends Fragment {

    private TextView alertMessageTextView, alertTimestampTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        alertMessageTextView = view.findViewById(R.id.alertMessage);
        alertTimestampTextView = view.findViewById(R.id.alertTimestamp);

        // Firebase reference
        DatabaseReference alertsRef = FirebaseDatabase.getInstance()
                .getReference("alerts");

        alertsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String message = snapshot.child("message").getValue(String.class);
                    String timestamp = snapshot.child("timestamp").getValue(String.class);

                    // 🔽 ADD THESE
                    Log.d("FirebaseAlert", "message: " + message + ", time: " + timestamp);
                    Toast.makeText(getContext(), "Fetched alert from Firebase", Toast.LENGTH_SHORT).show();

                    alertMessageTextView.setText("Alert: " + message);
                    alertTimestampTextView.setText("Time: " + timestamp);
                } else {
                    Toast.makeText(getContext(), "No alert data found", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
