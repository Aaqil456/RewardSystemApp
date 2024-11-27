package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardActivity extends AppCompatActivity implements RewardAdapter.OnClaimClickListener {

    private RecyclerView rewardsRecyclerView, claimedRewardsRecyclerView;
    private RewardAdapter rewardAdapter;
    private ClaimedRewardAdapter claimedRewardAdapter;
    private List<Reward> rewardList;
    private List<ClaimedReward> claimedRewardList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView rewardPointsTextView;
    private double userRewardPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        rewardPointsTextView = findViewById(R.id.reward_points_textview);
        rewardsRecyclerView = findViewById(R.id.rewardsRecyclerView);
        claimedRewardsRecyclerView = findViewById(R.id.claimedRewardsRecyclerView);

        rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        claimedRewardsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load user reward points and reward items
        loadRewardPoints();
        loadRewards();
        loadClaimedRewards(); // Load claimed rewards

        rewardAdapter = new RewardAdapter(rewardList, this, this);
        rewardsRecyclerView.setAdapter(rewardAdapter);

        claimedRewardList = new ArrayList<>();
        claimedRewardAdapter = new ClaimedRewardAdapter(this,claimedRewardList);
        claimedRewardsRecyclerView.setAdapter(claimedRewardAdapter);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(RewardActivity.this, MainActivity.class));
                    break;
                case R.id.navigation_history:
                    startActivity(new Intent(RewardActivity.this, TransactionHistoryActivity.class));
                    break;
                case R.id.navigation_reward:
                    break;
                case R.id.navigation_profile:
                    startActivity(new Intent(RewardActivity.this, ProfileActivity.class));
                    break;
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_reward);
    }

    private void loadRewardPoints() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double points = documentSnapshot.getDouble("rewardPoints");
                            userRewardPoints = points != null ? points : 0;
                            rewardPointsTextView.setText("Reward Points: " + userRewardPoints);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(RewardActivity.this, "Failed to load reward points", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadRewards() {
        rewardList = new ArrayList<>();
        rewardList.add(new Reward("10% Discount", 50));
        rewardList.add(new Reward("Free Coffee", 100));
        rewardList.add(new Reward("Gift Voucher", 200));
        rewardList.add(new Reward("40% Discount", 150));
        rewardList.add(new Reward("Free Latte", 120));
        rewardList.add(new Reward("Free Milkshake", 200));
    }

    private void loadClaimedRewards() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).collection("claimedRewards")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        claimedRewardList.clear();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String name = document.getString("name");
                            int pointsRequired = document.getLong("pointsRequired").intValue();

                            // Retrieve claimedDate as a Timestamp and format it
                            Timestamp claimedTimestamp = document.getTimestamp("claimedDate");
                            String claimedDate = claimedTimestamp != null ? claimedTimestamp.toDate().toString() : "Unknown date";

                            claimedRewardList.add(new ClaimedReward(name, pointsRequired, claimedDate));
                        }
                        claimedRewardAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(RewardActivity.this, "Failed to load claimed rewards", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onClaimClick(Reward reward) {
        if (userRewardPoints >= reward.getPointsRequired()) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                db.collection("users").document(userId)
                        .update("rewardPoints", FieldValue.increment(-reward.getPointsRequired()))
                        .addOnSuccessListener(aVoid -> {
                            userRewardPoints -= reward.getPointsRequired();
                            rewardPointsTextView.setText("Reward Points: " + userRewardPoints);

                            // Add to Firestore as claimed reward
                            Map<String, Object> claimedReward = new HashMap<>();
                            claimedReward.put("name", reward.getName());
                            claimedReward.put("pointsRequired", reward.getPointsRequired());
                            claimedReward.put("claimedDate", FieldValue.serverTimestamp());

                            db.collection("users").document(userId).collection("claimedRewards")
                                    .add(claimedReward)
                                    .addOnSuccessListener(documentReference -> {
                                        // Add locally and update the adapter
                                        claimedRewardList.add(new ClaimedReward(reward.getName(), reward.getPointsRequired(), "Just Now"));
                                        claimedRewardAdapter.notifyItemInserted(claimedRewardList.size() - 1);
                                        Toast.makeText(RewardActivity.this, reward.getName() + " claimed!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RewardActivity.this, "Failed to save claimed reward", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(RewardActivity.this, "Failed to claim reward", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(this, "Insufficient reward points to claim this reward", Toast.LENGTH_SHORT).show();
        }
    }
}
