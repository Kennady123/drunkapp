package com.example.drunk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText fullName, policeId, email, phone, rank, station, password;
    private Button btnSignUp, btnGoToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullName = findViewById(R.id.fullName);
        policeId = findViewById(R.id.policeId);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        rank = findViewById(R.id.rank);
        station = findViewById(R.id.station);
        password = findViewById(R.id.password);

        btnSignUp = findViewById(R.id.buttonSignUp);
        btnGoToLogin = findViewById(R.id.buttonGoToLogin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnSignUp.setOnClickListener(v -> {
            String name = fullName.getText().toString().trim();
            String id = policeId.getText().toString().trim();
            String emailVal = email.getText().toString().trim();
            String phoneVal = phone.getText().toString().trim();
            String rankVal = rank.getText().toString().trim();
            String stationVal = station.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (name.isEmpty() || id.isEmpty() || emailVal.isEmpty() || phoneVal.isEmpty() ||
                    rankVal.isEmpty() || stationVal.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔐 Create account with FirebaseAuth
            auth.createUserWithEmailAndPassword(emailVal, pass)
                    .addOnSuccessListener(authResult -> {
                        String uid = authResult.getUser().getUid();

                        // 📄 Save user data in Firestore
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("policeId", id);
                        userMap.put("email", emailVal);
                        userMap.put("phone", phoneVal);
                        userMap.put("rank", rankVal);
                        userMap.put("station", stationVal);

                        db.collection("users").document(uid)
                                .set(userMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Auth error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        });
    }
}
