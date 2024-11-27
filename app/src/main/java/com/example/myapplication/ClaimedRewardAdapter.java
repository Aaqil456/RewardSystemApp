package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClaimedRewardAdapter extends RecyclerView.Adapter<ClaimedRewardAdapter.ViewHolder> {

    private List<ClaimedReward> claimedRewardList;
    private Context context;

    // Constructor
    public ClaimedRewardAdapter(Context context, List<ClaimedReward> claimedRewardList) {
        this.context = context;
        this.claimedRewardList = claimedRewardList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_claimed_reward, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClaimedReward claimedReward = claimedRewardList.get(position);

        // Set the reward name and claimed date
        holder.claimedRewardName.setText(claimedReward.getName());
        holder.claimedRewardDate.setText(claimedReward.getClaimedDate());

        // Set the "Show QR" button listener
        holder.showQRButton.setOnClickListener(v -> {
            // Show the QR code in a popup
            showQRCodeDialog();
        });
    }

    @Override
    public int getItemCount() {
        return claimedRewardList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView claimedRewardName, claimedRewardDate;
        Button showQRButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            claimedRewardName = itemView.findViewById(R.id.claimed_reward_name);
            claimedRewardDate = itemView.findViewById(R.id.claimed_reward_date);
            showQRButton = itemView.findViewById(R.id.claimed_reward_show_qr_button);
        }
    }

    // Method to show the QR code in a dialog
    private void showQRCodeDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_qr_code, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        ImageView qrImageView = dialogView.findViewById(R.id.qr_image);
        Button closeButton = dialogView.findViewById(R.id.close_button);

        qrImageView.setImageResource(R.drawable.placeholder_qr_code);

        AlertDialog dialog = builder.create();
        dialog.show();

        closeButton.setOnClickListener(v -> dialog.dismiss());
    }

}
