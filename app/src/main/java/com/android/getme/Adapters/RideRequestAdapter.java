package com.android.getme.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.R;
import com.android.getme.ViewModels.RideRequestViewModel;

import java.util.List;

public class RideRequestAdapter extends RecyclerView.Adapter<RideRequestAdapter.ViewHolder> {

    private List<RideRequestViewModel> rideList;
    private OnRideActionListener listener;


    public interface OnRideActionListener {
        void onAcceptClick(int rideId);
    }



    public RideRequestAdapter(List<RideRequestViewModel> rideList, OnRideActionListener listener) {
        this.rideList = rideList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item_online, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideRequestViewModel ride = rideList.get(position);
        holder.tvName.setText(ride.getName());
        holder.tvRating.setText(ride.getRating());
        holder.tvPrice.setText(ride.getPrice());
        holder.tvDistance.setText(ride.getDistance());
        holder.tvPickup.setText(ride.getPickupLocation());
        holder.tvDropoff.setText(ride.getDropoffLocation());


        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptClick(ride.getRideId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRating, tvPrice, tvDistance, tvPickup, tvDropoff;
        Button btnAccept;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRiderName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvPickup = itemView.findViewById(R.id.tvPickup);
            tvDropoff = itemView.findViewById(R.id.tvDropoff);


            btnAccept = itemView.findViewById(R.id.btnAccept);
        }
    }
}