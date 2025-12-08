package com.android.getme.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.getme.Others.VolleySingleton;
import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

public class NavigationFragment extends Fragment {

    private TextView tvStatus;
    private Button btnArrived;


    private int rideId;
    private double pickupLat, pickupLng, dropoffLat, dropoffLng;
    private boolean isDropoffPhase = false;


    private final double driverLat = 10.740897;
    private final double driverLng = 106.695322;

    private static final String BASE_URL = "http://10.0.2.2:8000";

    public NavigationFragment() { }

    public static NavigationFragment newInstance(int rideId,
                                                 double sLat, double sLng, double eLat, double eLng,
                                                 String name, String rating, String price,
                                                 String distance, String pickup, String dropoff) {
        NavigationFragment fragment = new NavigationFragment();
        Bundle args = new Bundle();
        args.putInt("rideId", rideId);
        args.putDouble("startLat", sLat); args.putDouble("startLng", sLng);
        args.putDouble("endLat", eLat); args.putDouble("endLng", eLng);
        args.putString("name", name); args.putString("rating", rating);
        args.putString("price", price); args.putString("distance", distance);
        args.putString("pickup", pickup); args.putString("dropoff", dropoff);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStatus = view.findViewById(R.id.tvNavStatus);
        btnArrived = view.findViewById(R.id.btnArrived);

        if (getArguments() != null) {
            rideId = getArguments().getInt("rideId");


            pickupLat = getArguments().getDouble("startLat");
            pickupLng = getArguments().getDouble("startLng");

            dropoffLat = getArguments().getDouble("endLat");
            dropoffLng = getArguments().getDouble("endLng");


            ((TextView)view.findViewById(R.id.tvNavName)).setText(getArguments().getString("name"));
            ((TextView)view.findViewById(R.id.tvNavRating)).setText(getArguments().getString("rating"));
            ((TextView)view.findViewById(R.id.tvNavPickup)).setText(getArguments().getString("pickup"));
            ((TextView)view.findViewById(R.id.tvNavDropoff)).setText(getArguments().getString("dropoff"));
            ((TextView)view.findViewById(R.id.tvNavPrice)).setText(getArguments().getString("price"));
            ((TextView)view.findViewById(R.id.tvNavDistance)).setText(getArguments().getString("distance"));


            loadMap(driverLat, driverLng, pickupLat, pickupLng);
        }

        if (btnArrived != null) {
            btnArrived.setEnabled(false);
            btnArrived.setAlpha(0.5f);
            btnArrived.setText("Heading to Pickup...");
        }

        view.findViewById(R.id.btnBackNav).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void loadMap(double sLat, double sLng, double eLat, double eLng) {
        AnimatedMapFragment mapFragment = AnimatedMapFragment.newInstance(sLat, sLng, eLat, eLng);
        getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();
    }

    public void updateStatusText(String newText, String colorHex) {
        if (tvStatus != null) {
            tvStatus.setText(newText);
            try { tvStatus.setTextColor(Color.parseColor(colorHex)); } catch (Exception e) {}
        }
    }


    public void enableArrivalButton() {
        if (btnArrived == null) return;

        btnArrived.setEnabled(true);
        btnArrived.setAlpha(1.0f);

        if (!isDropoffPhase) {

            btnArrived.setText("Start Ride");
            btnArrived.setBackgroundColor(Color.parseColor("#2563EB"));
            btnArrived.setOnClickListener(v -> callArrivalEndpoint());
        } else {

            btnArrived.setText("Complete Ride");
            btnArrived.setBackgroundColor(Color.parseColor("#10B981"));
            btnArrived.setOnClickListener(v -> callCompleteRideEndpoint());
        }
    }


    private void callArrivalEndpoint() {
        String url = BASE_URL + "/ride/arrival/" + rideId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        String msg = response.getString("message");
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();


                        startDropoffPhase();

                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(getContext(), "Failed to mark arrival", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }


    private void callCompleteRideEndpoint() {
        String url = BASE_URL + "/ride/complete/" + rideId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        String msg = response.getString("message");
                        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();


                        requireActivity().getSupportFragmentManager().popBackStack();

                    } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> Toast.makeText(getContext(), "Failed to complete ride", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }


    private void startDropoffPhase() {
        isDropoffPhase = true;

        updateStatusText("Heading to Destination", "#EF4444");

        btnArrived.setEnabled(false);
        btnArrived.setAlpha(0.5f);
        btnArrived.setText("Driving to Dropoff...");


        loadMap(pickupLat, pickupLng, dropoffLat, dropoffLng);
    }
}