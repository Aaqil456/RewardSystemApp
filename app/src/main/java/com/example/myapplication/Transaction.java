package com.example.myapplication;

import com.google.firebase.Timestamp;

public class Transaction {
    private String productName;
    private double productPrice;
    private Timestamp transactionDate; // Represents the date of the transaction
    private String transactionId;      // New field for transaction ID (optional)

    // Empty constructor required for Firestore
    public Transaction() {
    }

    // Constructor with all fields
    public Transaction(String productName, double productPrice, Timestamp transactionDate, String transactionId) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.transactionDate = transactionDate;
        this.transactionId = transactionId;
    }

    // Getters and setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
