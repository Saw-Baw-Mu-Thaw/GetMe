package com.android.getme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DriverEarningActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TripsAdapter adapter;
    private List<Trip> tripList;

    // UI References
    private TextView txtAmount;
    private TextView txtTotalTrips;

    private int driverId;
    // Base URL for Emulator (10.0.2.2)
    private static final String BASE_URL = "http://10.0.2.2:8000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_earning);

        SharedPreferences sp = getSharedPreferences("SESSION", MODE_PRIVATE);
        driverId = sp.getInt("userId", -1);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // --- 1. Initialize UI ---
        ImageView btnBack = findViewById(R.id.btnBack);
        txtAmount = findViewById(R.id.txtAmount);
        txtTotalTrips = findViewById(R.id.txtTotalTrips); // Ensure ID exists in XML
        recyclerView = findViewById(R.id.recyclerViewTrips);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(DriverEarningActivity.this, DriverDashboard.class);
            startActivity(intent);
        });

        // --- 2. Setup RecyclerView ---
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false); // Important for nesting in ScrollView

        tripList = new ArrayList<>();
        adapter = new TripsAdapter(tripList);
        recyclerView.setAdapter(adapter);

        // --- 3. Fetch Data ---
        fetchEarningsData(); // Get Summary (Total $)
        fetchRecentTrips();  // Get List (Recycler View)
    }

    /**
     * Fetches summary stats: Total Earnings and Trip Count
     * Endpoint: /earnings
     */
    private void fetchEarningsData() {
        String url = BASE_URL + "/earnings?driverId=" + driverId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        int totalTrips = response.getInt("total_trips");
                        int totalEarnings = response.getInt("total_earnings");

                        txtAmount.setText("$" + totalEarnings);
                        if(txtTotalTrips != null) {
                            txtTotalTrips.setText(String.valueOf(totalTrips));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Earnings", "Error fetching summary: " + error.getMessage())
        );

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Fetches the list of trips to populate the RecyclerView
     * Endpoint: /trip/history
     * Logic adapted from TripHistoryActivity.java
     */
    private void fetchRecentTrips() {
        String url = BASE_URL + "/trip/history?driverId=" + driverId;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    tripList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);

                            // Parse Data
                            String from = obj.optString("LocationFrom", "Unknown");
                            String to = obj.optString("LocationTo", "Unknown");
                            String date = obj.optString("date", "N/A");
                            double amount = obj.optDouble("amount", 0.00);
                            String distance = obj.optString("distance", "0") + " km";
                            String status = obj.optString("status", "Completed");

                            // Create View Model
                            String route = from + " → " + to;
                            String priceStr = "$" + String.format("%.2f", amount);
                            // Combine Date and Distance for the 'Details' line
                            String details = date + " • " + distance;

                            // We use 'status' for the tip field since tip isn't in this specific API
                            Trip trip = new Trip(route, details, priceStr, status, status.equalsIgnoreCase("Completed"));
                            tripList.add(trip);
                        }
                        adapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("Earnings", "JSON Parse Error", e);
                    }
                },
                error -> {
                    Toast.makeText(DriverEarningActivity.this, "Failed to load trips", Toast.LENGTH_SHORT).show();
                    Log.e("Earnings", "Volley Error: " + error.toString());
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    // --- Data Model ---
    public static class Trip {
        String route;
        String details;
        String price;
        String statusLabel; // Maps to txtTip in XML
        boolean isPositiveStatus;

        public Trip(String route, String details, String price, String statusLabel, boolean isPositiveStatus) {
            this.route = route;
            this.details = details;
            this.price = price;
            this.statusLabel = statusLabel;
            this.isPositiveStatus = isPositiveStatus;
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
            holder.txtTip.setText(trip.statusLabel);

            // Color coding status
            if (trip.isPositiveStatus) {
                holder.txtTip.setTextColor(Color.parseColor("#10B981")); // Green
            } else {
                holder.txtTip.setTextColor(Color.parseColor("#EF4444")); // Red for Cancelled/Etc
            }
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
                txtTip = itemView.findViewById(R.id.txtTip); // Using this for Status
                imgAvatar = itemView.findViewById(R.id.imgAvatar);
            }
        }
    }
}