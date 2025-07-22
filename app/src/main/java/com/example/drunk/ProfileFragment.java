package com.example.drunk;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextInputEditText editName, editEmail, editPhone, editPoliceId, editRank, editStation;
    private Button btnSave;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // View Bindings
        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        editPoliceId = view.findViewById(R.id.editPoliceId);
        editRank = view.findViewById(R.id.editRank);
        editStation = view.findViewById(R.id.editStation);
        btnSave = view.findViewById(R.id.btnSave);

        // Load existing data
        loadUserData();

        // Save button action
        btnSave.setOnClickListener(v -> saveUserData());

        return view;
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        editName.setText(snapshot.getString("name"));
                        editEmail.setText(snapshot.getString("email"));
                        editPhone.setText(snapshot.getString("phone"));
                        editPoliceId.setText(snapshot.getString("policeId"));
                        editRank.setText(snapshot.getString("rank"));
                        editStation.setText(snapshot.getString("station"));
                    } else {
                        Toast.makeText(getContext(), "No profile data found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserData() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String policeId = editPoliceId.getText().toString().trim();
        String rank = editRank.getText().toString().trim();
        String station = editStation.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(policeId) || TextUtils.isEmpty(rank) || TextUtils.isEmpty(station)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("policeId", policeId);
        data.put("rank", rank);
        data.put("station", station);

        String uid = auth.getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("users").document(uid);

        userRef.set(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show());
    }
}
