package com.example.myapplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.ViewHolder> {

    private List<Reward> rewardList;
    private Context context;
    private OnClaimClickListener listener;

    public interface OnClaimClickListener {
        void onClaimClick(Reward reward);
    }

    public RewardAdapter(List<Reward> rewardList, Context context, OnClaimClickListener listener) {
        this.rewardList = rewardList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reward reward = rewardList.get(position);
        holder.rewardNameTextView.setText(reward.getName());
        holder.pointsRequiredTextView.setText("Points: " + reward.getPointsRequired());

        holder.claimButton.setOnClickListener(v -> listener.onClaimClick(reward));
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rewardNameTextView, pointsRequiredTextView;
        Button claimButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rewardNameTextView = itemView.findViewById(R.id.reward_name);
            pointsRequiredTextView = itemView.findViewById(R.id.points_required);
            claimButton = itemView.findViewById(R.id.claim_button);
        }
    }
}
