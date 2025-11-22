package com.android.getme.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.Others.DummyData;
import com.android.getme.R;
import com.android.getme.ViewModels.CustHistoryViewModel;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

public class CustHistoryAdapter extends RecyclerView.Adapter<CustHistoryAdapter.ViewHolder> {

    List<DummyData.RideHistory> rideHistories;
    Context context;

    public CustHistoryAdapter(Context context, List<DummyData.RideHistory> histories) {
        rideHistories = histories;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DummyData.RideHistory curr = rideHistories.get(position);

        if(curr.status.equals("Cancelled")) {
            holder.historyRideStatusImgView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.cancel_24px));
            holder.historyRideStatusTextView.setTextColor(Color.RED);
        }else {
            holder.historyRideStatusImgView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.check_circle_24px));
        }
        holder.historyRideStatusTextView.setText(curr.status);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLL d, hh:mm a");
        holder.historyRideDateTextView.setText(formatter.format(curr.date));

        holder.historyFromTextView.setText(curr.LocationFrom);
        holder.historyToTextView.setText(curr.LocationTo);

        String distance = curr.distance + " km";
        holder.historyDistance.setText(distance);

        String amount = "$" + curr.amount;
        holder.historyAmount.setText(amount);
    }

    @Override
    public int getItemCount() {
        return rideHistories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView historyRideStatusImgView;
        TextView historyRideStatusTextView;
        TextView historyRideDateTextView;
        TextView historyFromTextView;
        TextView historyToTextView;
        TextView historyDistance;
        TextView historyAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            historyRideStatusImgView = itemView.findViewById(R.id.historyRideStatusImgView);
            historyRideStatusTextView = itemView.findViewById(R.id.historyRideStatusTextView);
            historyRideDateTextView = itemView.findViewById(R.id.historyRideDateTextView);
            historyFromTextView = itemView.findViewById(R.id.historyFromTextView);
            historyToTextView = itemView.findViewById(R.id.historyToTextView);
            historyDistance = itemView.findViewById(R.id.historyDistance);
            historyAmount = itemView.findViewById(R.id.historyAmount);
        }
    }
}
