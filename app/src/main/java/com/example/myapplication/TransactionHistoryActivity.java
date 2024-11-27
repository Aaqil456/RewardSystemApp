package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        transactionList = new ArrayList<>();

        // Load transaction history
        loadTransactionHistory();

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    // Redirect to MainActivity
                    Intent mainIntent = new Intent(TransactionHistoryActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.navigation_history:
                    // stay in current activity TransactionHistoryActivity
                    break;
                case R.id.navigation_reward:
                    // Redirect to Reward activity
                    Intent rewardIntent = new Intent(TransactionHistoryActivity.this, RewardActivity.class);
                    startActivity(rewardIntent);
                    break;
                case R.id.navigation_profile:
                    // Redirect to ProfileActivity
                    Intent profileIntent = new Intent(TransactionHistoryActivity.this, ProfileActivity.class);
                    startActivity(profileIntent);
                    break;
            }
            return true;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Set the selected item in BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_history); // Set history as selected
    }


    private void loadTransactionHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Fetch transaction history from Firestore, ordered by transactionDate in descending order
            db.collection("txnHistory")
                    .document(userId)
                    .collection("transactions")
                    .orderBy("transactionDate", Query.Direction.DESCENDING) // Sort by date (latest first)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<String> documentIds = new ArrayList<>();
                            transactionList.clear(); // Clear the list to avoid duplicates
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Transaction transaction = document.toObject(Transaction.class);
                                transactionList.add(transaction);
                                documentIds.add(document.getId()); // Store the document ID
                            }

                            // Set up the adapter
                            transactionAdapter = new TransactionAdapter(this, transactionList, documentIds);
                            recyclerView.setAdapter(transactionAdapter);
                        } else {
                            Log.e("TransactionHistory", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }


}
