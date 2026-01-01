package com.example.amarhisab;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CalculatorHistoryAdapter extends RecyclerView.Adapter<CalculatorHistoryAdapter.HistoryViewHolder> {

    private ArrayList<CalculatorActivity.CalculatorHistory> historyList;
    private AlertDialog parentDialog;

    public CalculatorHistoryAdapter(ArrayList<CalculatorActivity.CalculatorHistory> historyList, AlertDialog parentDialog) {
        this.historyList = historyList;
        this.parentDialog = parentDialog;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calculator_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        CalculatorActivity.CalculatorHistory history = historyList.get(position);

        holder.tvExpression.setText(history.expression);
        holder.tvResult.setText("= " + history.result);
        holder.tvTime.setText(history.timestamp);

        // Animate item appearance
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 50)
                .start();
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpression, tvResult, tvTime;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpression = itemView.findViewById(R.id.tvHistoryExpression);
            tvResult = itemView.findViewById(R.id.tvHistoryResult);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
        }
    }
}