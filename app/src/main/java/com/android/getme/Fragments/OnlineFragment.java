package com.android.getme.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.Others.VolleySingleton;
import com.android.getme.Adapters.RideRequestAdapter;
import com.android.getme.R;
import com.android.getme.ViewModels.RideRequestViewModel;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OnlineFragment extends Fragment {

    private RecyclerView recyclerView;
    private RideRequestAdapter adapter;
    private List<RideRequestViewModel> requests;


    private static final String BASE_URL = "http://10.0.2.2:8000";
    private int driverId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_online, container, false);

        SharedPreferences sp = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);
        driverId = sp.getInt("userId", -1);


        recyclerView = view.findViewById(R.id.rvRideRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        requests = new ArrayList<>();

        adapter = new RideRequestAdapter(requests, rideId -> {

            RideRequestViewModel selectedRide = findRideById(rideId);
            if (selectedRide != null) {
                acceptRide(selectedRide);
            }
        });

        recyclerView.setAdapter(adapter);
        fetchRideRequests();
        return view;
    }


    private RideRequestViewModel findRideById(int rideId) {
        for (RideRequestViewModel r : requests) {
            if (r.getRideId() == rideId) return r;
        }
        return null;
    }

    private void fetchRideRequests() {
        String url = BASE_URL + "/ride/requests";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> processResponse(response),
                error -> Log.e("OnlineFragment", "Error: " + error.getMessage())
        );
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    private void processResponse(JSONArray response) {
        requests.clear();
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);


                int rideId = obj.getInt("rideId");
                int custId = obj.optInt("custId");
                String pickup = obj.optString("LocationFrom", "Unknown");
                String dropoff = obj.optString("LocationTo", "Unknown");
                double amount = obj.optDouble("amount", 0.0);
                double distance = obj.optDouble("distance", 0.0);


                double pickupLat = obj.optDouble("pickupLat", 0.0);
                double pickupLng = obj.optDouble("pickupLong", 0.0);
                double dropoffLat = obj.optDouble("dropoffLat", 0.0);
                double dropoffLng = obj.optDouble("dropoffLong", 0.0);

                requests.add(new RideRequestViewModel(
                        rideId, "Customer #" + custId, "★ 5.0",
                         amount + " VND" , distance + " km",
                        pickup, dropoff,
                        pickupLat, pickupLng, dropoffLat, dropoffLng
                ));
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void acceptRide(RideRequestViewModel ride) {
        String url = BASE_URL + "/ride/accept/" + ride.getRideId() + "?driverId=" + driverId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    try {
                        String msg = response.getString("message");
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();


                        openMapFragment(ride);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Failed to accept", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }





    private void openMapFragment(RideRequestViewModel ride) {

        NavigationFragment navFragment = NavigationFragment.newInstance(
                ride.getRideId(),
                ride.getPickupLat(),
                ride.getPickupLng(),
                ride.getDropoffLat(),
                ride.getDropoffLng(),
                ride.getName(),
                ride.getRating(),
                ride.getPrice(),
                ride.getDistance(),
                ride.getPickupLocation(),
                ride.getDropoffLocation()
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, navFragment)
                .addToBackStack(null)
                .commit();
    }
}