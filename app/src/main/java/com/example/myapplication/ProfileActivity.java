package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // Code for picking an image
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView walletBalanceTextView, UsernameTV,AgeTv, RewardPointTV;
    private Button topUpButton;
    private Button logoutButton; // Logout button to log out of the profile
    private Button changeProfilePicButton; // Button for changing profile picture
    private FirebaseStorage storage; // Firebase Storage reference
    private ImageView profileImageView; // ImageView to display the profile picture
    private Uri imageUri; // Uri of the selected image


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance(); // Initialize Firebase Storage

        //profile view
        UsernameTV=findViewById(R.id.tv_user_name);
        AgeTv= findViewById(R.id.tv_user_age);
        RewardPointTV = findViewById(R.id.tv_reward_points);
        walletBalanceTextView = findViewById(R.id.wallet_balance);

        profileImageView = findViewById(R.id.profile_image_view); // Make sure you have an ImageView in your layout


        //button
        topUpButton = findViewById(R.id.top_up_button);
        logoutButton = findViewById(R.id.btn_logout); // Initialize the logout button

        changeProfilePicButton = findViewById(R.id.change_profile_pic_button); // Initialize change profile picture button

        profileInfo(); // Load current wallet balance

        // Handle the top-up button click to show a popup dialog
        topUpButton.setOnClickListener(v -> showTopUpDialog());

        // Handle logout button click
        logoutButton.setOnClickListener(v -> logout());

        // Handle change profile picture button click
        changeProfilePicButton.setOnClickListener(v -> selectImage());

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    // Redirect to MainActivity
                    Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.navigation_history:
                    // Redirect TransactionHistoryActivity
                    Intent historyIntent = new Intent(ProfileActivity.this, TransactionHistoryActivity.class);
                    startActivity(historyIntent);
                    break;
                case R.id.navigation_reward:
                    // Redirect to Reward activity
                    Intent rewardIntent = new Intent(ProfileActivity.this, RewardActivity.class);
                    startActivity(rewardIntent);
                    break;
                case R.id.navigation_profile:
                    // stay in current activity ProfileActivity
                    break;
            }
            return true;
        });

        loadProfileImage();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Set the selected item in BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile); // Set profile as selected
    }

    // Method to select an image from the gallery
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*"); // Set the type of data to filter by
        intent.setAction(Intent.ACTION_GET_CONTENT); // Allow the user to pick an image
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handle the result of the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Get the Uri of the selected image
            try {
                // Set the selected image to the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);

                // Upload the image to Firebase Storage
                uploadImageToFirestore(imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to upload the selected image to Firebase Storage
    private void uploadImageToFirestore(Uri imageUri) {
        if (imageUri != null) {
            // Get the current user
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                // Create a reference to Firebase Storage
                StorageReference storageRef = storage.getReference("profile_images/" + user.getUid() + ".jpg");

                // Start the upload task
                UploadTask uploadTask = storageRef.putFile(imageUri);

                // Attach listeners to the upload task to listen for success and failure
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL and store it in Firestore
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        // Save the image URL in Firestore
                        saveImageUrlToFirestore(downloadUrl.toString());
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    Log.e("Upload Image", "Error: " + e.getMessage());
                });
            }
        }
    }

    // Method to save the image URL to Firestore
    private void saveImageUrlToFirestore(String imageUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId)
                    .update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Profile image updated successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile image URL", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    //load profile image
    private void loadProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Fetch user's profile image URL from Firestore
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            if (imageUrl != null) {
                                // Use Glide to load the image into the ImageView
                                Glide.with(this)
                                        .load(imageUrl)
                                        .into(profileImageView);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Load profile info from Firestore
    private void profileInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Fetch user's current wallet balance from Firestore
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double userMoney = documentSnapshot.getDouble("userMoney");
                            String userName = documentSnapshot.getString("name");
                            Double userAgeDouble = documentSnapshot.getDouble("age");
                            Double rewardPoint = documentSnapshot.getDouble("rewardPoints");
                            int userAge = 0; // Default value in case of null

                            if (userAgeDouble != null) {
                                userAge = userAgeDouble.intValue(); // Convert Double to int
                            }

                            if (userMoney != null) {
                                walletBalanceTextView.setText("Balance: RM" + userMoney);
                                UsernameTV.setText("Name: "+userName);
                                AgeTv.setText("Age: "+userAge );
                                RewardPointTV.setText("Reward Points: "+ rewardPoint);
                            } else {
                                walletBalanceTextView.setText("Balance: RM" + userMoney);
                                UsernameTV.setText("Name: "+userName);
                                AgeTv.setText("Age: "+userAge );
                                RewardPointTV.setText("Reward Points: "+ rewardPoint);
                                walletBalanceTextView.setText("Balance: RM0.00");
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to load wallet balance", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Function to show the top-up dialog
    private void showTopUpDialog() {
        // Create and inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_topup, null);

        // Set up the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Initialize the views in the custom layout
        TextView titleTopUp = dialogView.findViewById(R.id.title_topup);
        EditText topUpAmountEditText = dialogView.findViewById(R.id.topup_amount);
        Button confirmButton = dialogView.findViewById(R.id.topup_button);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle the Confirm button
        confirmButton.setOnClickListener(v -> {
            String topUpAmountString = topUpAmountEditText.getText().toString().trim();
            if (!topUpAmountString.isEmpty()) {
                try {
                    double topUpAmount = Double.parseDouble(topUpAmountString);
                    topUpWallet(topUpAmount);
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(ProfileActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Top up the user's wallet by the specified amount
    private void topUpWallet(double amount) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Increment the wallet balance in Firestore
            db.collection("users").document(userId)
                    .update("userMoney", FieldValue.increment(amount))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Wallet topped up with RM" + amount, Toast.LENGTH_SHORT).show();
                        profileInfo(); // Reload the balance after topping up
                        loadProfileImage();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to top up wallet", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Log the user out
    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
        startActivity(intent);
        finish(); // Ensure the user cannot go back to the profile without logging in again
    }


}
