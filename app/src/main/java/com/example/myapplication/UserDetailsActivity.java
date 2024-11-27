package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserDetailsActivity extends AppCompatActivity {

    private EditText nameEditText, ageEditText;
    private Button submitButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        nameEditText = findViewById(R.id.name_edit_text);
        ageEditText = findViewById(R.id.age_edit_text);
        submitButton = findViewById(R.id.submit_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        submitButton.setOnClickListener(v -> submitDetails());
    }

    private void submitDetails() {
        String name = nameEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr)) {
            Toast.makeText(UserDetailsActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Create a map to store user details
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("name", name);
            userDetails.put("age", age);
            userDetails.put("rewardPoints", 10); // Give 10 reward points to first-time users
            userDetails.put("userMoney", 0.0); // Initialize wallet with $0.00
            // Save user details to Firestore
            db.collection("users").document(userId)
                    .set(userDetails)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UserDetailsActivity.this, "Details submitted!", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(UserDetailsActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(UserDetailsActivity.this, "Failed to submit details", Toast.LENGTH_SHORT).show());
        }
    }
}
