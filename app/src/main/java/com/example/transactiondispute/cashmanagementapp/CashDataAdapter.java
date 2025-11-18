package com.example.transactiondispute.cashmanagementapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.transactiondispute.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CashDataAdapter extends RecyclerView.Adapter<CashDataAdapter.ViewHolder> {
    private List<CashData> cashDataList;
    private OnItemClickListener onItemClickListener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public CashDataAdapter(List<CashData> cashDataList) {
        this.cashDataList = cashDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cash_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CashData data = cashDataList.get(position);
        
        holder.tvDate.setText("Date: " + data.getDate());
        holder.tvIndentAmount.setText("Indent: ₹" + data.getIndentAmount());
        holder.tv500Notes.setText("500 Notes: " + data.getSum500Notes());
        holder.tv200Notes.setText("200 Notes: " + data.getSum200Notes());
        holder.tv100Notes.setText("100 Notes: " + data.getSum100Notes());
        holder.tvEodReceived.setText("EOD Received: " + data.getEodReceivedFromPurge());
        holder.tvEod500.setText("EOD 500: " + data.getEod500Notes());
        holder.tvEod200.setText("EOD 200: " + data.getEod200Notes());
        holder.tvEod100.setText("EOD 100: " + data.getEod100Notes());
        holder.tvLoadingTime.setText("Load Time: " + data.getLoadingTime());
        holder.tvLoadAmount.setText("Load Amount: ₹" + data.getSumLoadAmount());
        holder.tvEodAmount.setText("EOD Amount: ₹" + data.getSumEodAmount());
        holder.tvDueAmount.setText("Due EOD: ₹" + data.getDueEodAmount());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return cashDataList.size();
    }

    public void updateData(List<CashData> newData) {
        this.cashDataList = newData;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvIndentAmount, tv500Notes, tv200Notes, tv100Notes;
        TextView tvEodReceived, tvEod500, tvEod200, tvEod100, tvLoadingTime;
        TextView tvLoadAmount, tvEodAmount, tvDueAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvDate = itemView.findViewById(R.id.tvDate);
            tvIndentAmount = itemView.findViewById(R.id.tvIndentAmount);
            tv500Notes = itemView.findViewById(R.id.tv500Notes);
            tv200Notes = itemView.findViewById(R.id.tv200Notes);
            tv100Notes = itemView.findViewById(R.id.tv100Notes);
            tvEodReceived = itemView.findViewById(R.id.tvEodReceived);
            tvEod500 = itemView.findViewById(R.id.tvEod500);
            tvEod200 = itemView.findViewById(R.id.tvEod200);
            tvEod100 = itemView.findViewById(R.id.tvEod100);
            tvLoadingTime = itemView.findViewById(R.id.tvLoadingTime);
            tvLoadAmount = itemView.findViewById(R.id.tvLoadAmount);
            tvEodAmount = itemView.findViewById(R.id.tvEodAmount);
            tvDueAmount = itemView.findViewById(R.id.tvDueAmount);
        }
    }
}