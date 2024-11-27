package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private List<String> documentIds; // Store Firestore document IDs
    private Context context;

    public TransactionAdapter(Context context, List<Transaction> transactionList, List<String> documentIds) {
        this.context = context;
        this.transactionList = transactionList;
        this.documentIds = documentIds;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        String documentId = documentIds.get(position);

        holder.productNameTextView.setText(transaction.getProductName());
        holder.productPriceTextView.setText("Price: $" + transaction.getProductPrice());

        // Format the transaction date
        Timestamp timestamp = transaction.getTransactionDate();
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(date);
        holder.transactionDateTextView.setText("Date: " + formattedDate);

        // Set up the "View" button click listener
        holder.viewButton.setOnClickListener(v -> showTransactionDetails(transaction, documentId, formattedDate));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    // Function to show the transaction details in a popup
    private void showTransactionDetails(Transaction transaction, String transactionId, String formattedDate) {
        // Create and inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_receipt, null);

        // Set up the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        // Initialize the views in the custom layout
        TextView titleReceipt = dialogView.findViewById(R.id.title_receipt);
        TextView receiptProductName = dialogView.findViewById(R.id.receipt_product_name);
        TextView receiptPrice = dialogView.findViewById(R.id.receipt_price);
        TextView receiptDate = dialogView.findViewById(R.id.receipt_date);
        TextView receiptTransactionId = dialogView.findViewById(R.id.receipt_transaction_id);
        Button closeButton = dialogView.findViewById(R.id.close_button);

        // Get the last 4 digits of the transaction ID
        String lastFourDigits = transactionId.length() > 4
                ? transactionId.substring(transactionId.length() - 4)
                : transactionId;

        // Populate the receipt fields
        receiptProductName.setText("Product Name: " + transaction.getProductName());
        receiptPrice.setText("Price: $" + transaction.getProductPrice());
        receiptDate.setText("Transaction Date: " + formattedDate);
        receiptTransactionId.setText("Transaction ID: " + lastFourDigits);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Handle the Close button
        closeButton.setOnClickListener(v -> dialog.dismiss());
    }


    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView productNameTextView, productPriceTextView, transactionDateTextView;
        public Button viewButton;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.product_name);
            productPriceTextView = itemView.findViewById(R.id.product_price);
            transactionDateTextView = itemView.findViewById(R.id.transaction_date);
            viewButton = itemView.findViewById(R.id.view_button); // Reference to the "View" button
        }
    }
}
