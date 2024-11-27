package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnBuyClickListener {

    private static final int QR_SCAN_REQUEST_CODE = 1001; // Added this line
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button ScanBtn, topUpButton;
    private TextView walletBalanceTextView, RewardPointTV;
    //image switcher
    private int[] images = {
            R.drawable.app_icon,
            R.drawable.product1_image,
            R.drawable.product2_image,
            R.drawable.product3_image,
            R.drawable.product4_image,
            R.drawable.product5_image,
            R.drawable.product6_image
    };
    private int currentIndex = 0;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize variables
        walletBalanceTextView = findViewById(R.id.wallet_balance);
        RewardPointTV = findViewById(R.id.tv_reward_points);
        topUpButton = findViewById(R.id.top_up_button);
        ScanBtn = findViewById(R.id.scan_to_buy_button);
        ImageSwitcher imageSwitcher = findViewById(R.id.imageSwitcher);

        // Set the factory for the ImageSwitcher
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(MainActivity.this);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // Prevent stretching
                imageView.setAdjustViewBounds(true); // Maintain aspect ratio
                return imageView;
            }
        });


        // Start the slideshow
        startSlideshow(imageSwitcher);



        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Populate product list and load wallet balance
        productList = new ArrayList<>();
        walletInfo();
        loadProducts();

        // Set up RecyclerView and adapter
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(productList, this, this);
        recyclerView.setAdapter(productAdapter);

        // Setup BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    // Stay in current activity
                    break;
                case R.id.navigation_history:
                    startActivity(new Intent(MainActivity.this, TransactionHistoryActivity.class));
                    break;
                case R.id.navigation_reward:
                    startActivity(new Intent(MainActivity.this, RewardActivity.class));
                    break;
                case R.id.navigation_profile:
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    break;
            }
            return true;
        });

        // Set up button listeners
        topUpButton.setOnClickListener(v -> showTopUpDialog());
        ScanBtn.setOnClickListener(v -> startQrScannerAndBuyRandomProduct());
    }

    private void startSlideshow(ImageSwitcher imageSwitcher) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageSwitcher.setImageResource(images[currentIndex]);
                currentIndex = (currentIndex + 1) % images.length;
                handler.postDelayed(this, 1000); // Change image every 3 seconds
            }
        }, 0);
    }



    private void loadProducts() {
        // Example products (you can replace this with dynamic data)
        productList.add(new Product("Coffee", 4.0, R.drawable.product1_image)); // Replace with actual image resources
        productList.add(new Product("Lemon Tea", 5.0, R.drawable.product2_image)); // Replace with actual image resources
        productList.add(new Product("Sparkling Fruit", 10.0, R.drawable.product3_image)); // Replace with actual image resources
        productList.add(new Product("Cold Brew Coffee", 12.0, R.drawable.product4_image)); // Replace with actual image resources
        productList.add(new Product("Herbal Iced Tea", 8.0, R.drawable.product5_image)); // Replace with actual image resources
        productList.add(new Product("Matcha Latte", 7.0, R.drawable.product6_image)); // Replace with actual image resources

        // Add more products as needed
    }

    @Override
    public void onBuyClick(Product product) {
        // Directly complete the purchase without QR scanning
        completePurchase(product);
    }

    private void startQrScannerAndBuyRandomProduct() {
        // Start QR Scanner activity
        Intent intent = new Intent(this, QRCodeScannerActivity.class);
        startActivityForResult(intent, QR_SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                // Select a random product from the list
                Product randomProduct = getRandomProduct();
                if (randomProduct != null) {
                    completePurchase(randomProduct);
                } else {
                    Toast.makeText(this, "No products available for purchase.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "QR scan failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private Product getRandomProduct() {
        if (productList.isEmpty()) return null;
        Random random = new Random();
        int index = random.nextInt(productList.size());
        return productList.get(index);

    }

    private void completePurchase(Product product) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            double productPrice = product.getPrice();

            // Check user's wallet balance and proceed with purchase if funds are sufficient
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double userMoney = documentSnapshot.getDouble("userMoney");

                            if (userMoney != null && userMoney >= productPrice) {
                                db.collection("users").document(userId)
                                        .update("userMoney", FieldValue.increment(-productPrice))
                                        .addOnSuccessListener(aVoid -> {
                                            db.collection("users").document(userId)
                                                    .update("rewardPoints", FieldValue.increment(productPrice))
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        saveTransactionHistory(userId, product);
                                                        Toast.makeText(MainActivity.this, "Purchase successful! Price deducted: $" + productPrice, Toast.LENGTH_SHORT).show();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to update points", Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to deduct from wallet", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(MainActivity.this, "Insufficient funds in wallet", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            walletInfo(); // Reload wallet balance
        }
    }

    private void saveTransactionHistory(String userId, Product product) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("productName", product.getName());
        transaction.put("productPrice", product.getPrice());
        transaction.put("transactionDate", Timestamp.now());

        db.collection("txnHistory").document(userId).collection("transactions").add(transaction)
                .addOnSuccessListener(documentReference -> Toast.makeText(MainActivity.this, "Transaction saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to save transaction", Toast.LENGTH_SHORT).show());
    }

    // Load wallet balance info
    private void walletInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double userMoney = documentSnapshot.getDouble("userMoney");
                            Double rewardPoint = documentSnapshot.getDouble("rewardPoints");
                            walletBalanceTextView.setText("Balance: RM" + userMoney);
                            RewardPointTV.setText("Reward Points: " + rewardPoint);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to load wallet balance", Toast.LENGTH_SHORT).show());
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
                    Toast.makeText(MainActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void topUpWallet(double amount) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            db.collection("users").document(userId)
                    .update("userMoney", FieldValue.increment(amount))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Wallet topped up with RM" + amount, Toast.LENGTH_SHORT).show();
                        walletInfo(); // Reload the balance after topping up
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to top up wallet", Toast.LENGTH_SHORT).show());
        }
    }
}
