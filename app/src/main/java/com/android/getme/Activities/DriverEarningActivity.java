package com.android.getme.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.R;

import java.util.ArrayList;
import java.util.List;

public class DriverEarningActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TripsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_earning);



        // Hide default action bar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverEarningActivity.this, DriverDashboard.class);
                startActivity(intent);
            }
        });
        recyclerView = findViewById(R.id.recyclerViewTrips);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create Dummy Data
        List<Trip> tripList = new ArrayList<>();
        tripList.add(new Trip("Downtown → Airport", "2:30 PM • 8.2 km", "$24.50", "+$3.00 tip", true));
        tripList.add(new Trip("Mall → Central Station", "1:15 PM • 5.7 km", "$18.25", "+$2.50 tip", true));
        tripList.add(new Trip("Office Park → Hotel", "12:45 PM • 3.2 km", "$15.75", "No tip", false));
        tripList.add(new Trip("Suburb → City Center", "11:20 AM • 12.5 km", "$32.00", "+$5.00 tip", true));

        adapter = new TripsAdapter(tripList);
        recyclerView.setAdapter(adapter);
    }

    // --- Data Model ---
    public static class Trip {
        String route;
        String details;
        String price;
        String tip;
        boolean hasTip;

        public Trip(String route, String details, String price, String tip, boolean hasTip) {
            this.route = route;
            this.details = details;
            this.price = price;
            this.tip = tip;
            this.hasTip = hasTip;
        }
    }

    // --- Adapter ---
    public static class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripViewHolder> {

        private List<Trip> trips;

        public TripsAdapter(List<Trip> trips) {
            this.trips = trips;
        }

        @NonNull
        @Override
        public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.trip_item, parent, false);
            return new TripViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
            Trip trip = trips.get(position);
            holder.txtRoute.setText(trip.route);
            holder.txtDetails.setText(trip.details);
            holder.txtPrice.setText(trip.price);
            holder.txtTip.setText(trip.tip);

            if (trip.hasTip) {
                holder.txtTip.setTextColor(Color.parseColor("#10B981")); // Green
            } else {
                holder.txtTip.setTextColor(Color.parseColor("#9CA3AF")); // Gray
            }

            // In a real app, you would load different images here
            // holder.imgAvatar.setImageResource(...);
        }

        @Override
        public int getItemCount() {
            return trips.size();
        }

        static class TripViewHolder extends RecyclerView.ViewHolder {
            TextView txtRoute, txtDetails, txtPrice, txtTip;
            ImageView imgAvatar;

            public TripViewHolder(@NonNull View itemView) {
                super(itemView);
                txtRoute = itemView.findViewById(R.id.txtRoute);
                txtDetails = itemView.findViewById(R.id.txtDetails);
                txtPrice = itemView.findViewById(R.id.txtPrice);
                txtTip = itemView.findViewById(R.id.txtTip);
                imgAvatar = itemView.findViewById(R.id.imgAvatar);
            }
        }
    }
}