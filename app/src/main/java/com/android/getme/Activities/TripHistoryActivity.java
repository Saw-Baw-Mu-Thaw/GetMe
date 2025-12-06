package com.android.getme.Activities;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TripAdapter adapter;
    private List<Object> items;

    // TODO: Change '2' to the actual logged-in driver's ID dynamically
    private static final int DRIVER_ID = 2;

    // NOTE: '10.0.2.2' is the localhost alias for Android Emulator.
    // Use your computer's actual IP (e.g., 192.168.1.x) if testing on a real phone.
    // Based on your swagger, the endpoint uses a query parameter.
    // 10.0.2.2 points to your computer's "localhost"
    private static final String API_URL = "http://10.0.2.2:8000/trip/history?driverId=" + DRIVER_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);

        // 1. Setup UI
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Closes activity to go back

        recyclerView = findViewById(R.id.recyclerViewTrips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        items = new ArrayList<>();
        adapter = new TripAdapter(items);
        recyclerView.setAdapter(adapter);

        // 2. Fetch Data from API
        fetchRideHistory();
    }

    private void fetchRideHistory() {
        Log.d("TripHistory", "Fetching URL: " + API_URL);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                API_URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        processResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMsg = (error.getMessage() != null) ? error.getMessage() : "Unknown Connection Error";
                        Toast.makeText(TripHistoryActivity.this, "Failed to load history: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("TripHistory", "Volley Error: " + error.toString());
                    }
                }
        );

        // Use the VolleySingleton you provided
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest);
    }

    private void processResponse(JSONArray response) {
        items.clear();
        items.add("Ride History"); // Add Header

        try {
            if (response.length() == 0) {
                items.add("No trips found.");
                // You might want to handle an empty state UI here
            }

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                // --- DATA MAPPING ---
                // Mapping DB schema columns to variables
                // Use 'optString' to prevent crashes if a field is null
                String from = obj.optString("LocationFrom", "Unknown");
                String to = obj.optString("LocationTo", "Unknown");

                // Combine From/To for the card Title
                String title = from + " to " + to;

                String date = obj.optString("date", "N/A"); // e.g. "2025-12-05 14:00"

                double amount = obj.optDouble("amount", 0.00);
                String price = "$" + String.format("%.2f", amount);

                // Assuming duration/distance are numbers or strings in JSON
                String duration = obj.optString("duration", "0") + " min";
                String distance = obj.optString("distance", "0") + " km";
                String status = obj.optString("status", "Completed");

                // Create Trip object
                Trip trip = new Trip(title, date, price, duration, distance, status);
                items.add(trip);
            }
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("TripHistory", "JSON Parse Error", e);
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    // --- MODEL ---
    static class Trip {
        String title, date, price, duration, distance, status;

        public Trip(String title, String date, String price, String duration, String distance, String status) {
            this.title = title;
            this.date = date;
            this.price = price;
            this.duration = duration;
            this.distance = distance;
            this.status = status;
        }
    }

    // --- ADAPTER ---
    static class TripAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_TRIP = 1;
        private final List<Object> items;

        public TripAdapter(List<Object> items) { this.items = items; }

        @Override
        public int getItemViewType(int position) {
            return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_TRIP;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                TextView headerView = new TextView(parent.getContext());
                headerView.setPadding(48, 48, 48, 16);
                headerView.setTextSize(18);
                headerView.setTypeface(null, android.graphics.Typeface.BOLD);
                headerView.setTextColor(0xFF1A1D1E);
                return new HeaderViewHolder(headerView);
            } else {
                View view = inflater.inflate(R.layout.item_trip_card, parent, false);
                return new TripViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                ((HeaderViewHolder) holder).title.setText((String) items.get(position));
            } else {
                Trip trip = (Trip) items.get(position);
                TripViewHolder h = (TripViewHolder) holder;

                h.tvTitle.setText(trip.title);
                h.tvDate.setText(trip.date);
                h.tvPrice.setText(trip.price);
                h.tvDuration.setText(trip.duration);
                h.tvDistance.setText(trip.distance);

                // Handle status color if needed
                h.tvStatus.setText(trip.status);
                if(trip.status.equalsIgnoreCase("Cancelled")){
                    h.tvStatus.setTextColor(0xFFFF0000); // Red for cancelled
                    h.tvStatus.setBackgroundResource(R.drawable.bg_status_badge); // Ensure you have a badge drawable or remove this
                }
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            HeaderViewHolder(View v) { super(v); title = (TextView) v; }
        }

        static class TripViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDate, tvPrice, tvDuration, tvDistance, tvStatus;

            TripViewHolder(View itemView) {
                super(itemView);
                // IDs match your provided item_trip_card.xml
                tvTitle = itemView.findViewById(R.id.tvTripTitle);
                tvDate = itemView.findViewById(R.id.tvTripDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvDuration = itemView.findViewById(R.id.tvDuration);
                tvDistance = itemView.findViewById(R.id.tvDistance);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}