package com.android.getme.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.getme.Listeners.OngoingRideListener;
import com.android.getme.Models.OngoingResult;
import com.android.getme.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Random;

public class OngoingTabFragment extends Fragment {

    private LinearLayout ongoingLinLay;
    private LinearLayout driverTimeLinlay;
    private LinearLayout noOngoingLinlay;
    private TextView driverStatusTextView;
    private TextView driverNameTextView;
    private TextView makeTextView;
    private RatingBar ongoingRatingBar;
    private TextView ongoingAmountTextView;
    private TextView ongoingRideType;
    private TextView ongoingRatingTextView;
    private TextView ongoingFrom;
    private TextView ongoingTo;
    private TextView ongoingRideStatusTextView;
    private TextView ongoingRideTimeTextView;

    private RequestQueue queue;

    final private String BASEURL = "http://10.0.2.2:8000";
    private int custId;
    private String vehicleType;
    private String status;

    private OngoingRideListener listener;


    public OngoingTabFragment() {
        // Required empty public constructor
    }

    public static OngoingTabFragment newInstance(int custId, String vehicleType) {
        Bundle b = new Bundle();
        b.putInt("custId", custId);
        b.putString("vehicleType", vehicleType);
        OngoingTabFragment fragment = new OngoingTabFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            custId = getArguments().getInt("custId");
            vehicleType = getArguments().getString("vehicleType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ongoing_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViewComponents(view);

        ongoingLinLay.setVisibility(View.GONE);
        driverTimeLinlay.setVisibility(View.GONE);
        noOngoingLinlay.setVisibility(View.GONE);

        if(getContext() instanceof  OngoingRideListener) {
            listener = (OngoingRideListener) getContext();
        }

        ongoingLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.ongoingRideClicked(status);
            }
        });

        if(vehicleType != null && custId != 0) {
            fetchOngoingRide();
        }else {
            noOngoingLinlay.setVisibility(View.VISIBLE);
        }

    }

    private void fetchOngoingRide() {
        queue = Volley.newRequestQueue(getContext());

        String url = BASEURL + "/activity/ongoing?custId=" + custId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Gson gson = new Gson();
                        OngoingResult[] results = gson.fromJson(s, OngoingResult[].class);

                        if(results.length == 0) {
                            noOngoingLinlay.setVisibility(View.VISIBLE);
                            return;
                        }else{
                            OngoingResult result = results[results.length-1];

                            status = result.status;

                            if(result.status.equals("Waiting")) {
                                driverStatusTextView.setText("Driver En Route");
                                ongoingRideStatusTextView.setText("Driver is Coming To You");
                            }else if(result.status.equals("In Transit")) {
                                driverStatusTextView.setText("Going To Destination");
                                ongoingRideStatusTextView.setText("You are going to your destination");
                            }

                            ongoingAmountTextView.setText(result.amount + " VND");
                            ongoingRideType.setText(result.vehicleType);
                            ongoingFrom.setText(result.LocationFrom);
                            ongoingTo.setText(result.LocationTo);

                            Random rand = new Random();
                            int minutes = rand.nextInt(60);
                            String arrivalTime = "Estimated Arrival : " + (minutes + 2) + "minutes";
                            ongoingRideTimeTextView.setText(arrivalTime);

                            fetchDriver(result.driverId);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
                Toast.makeText(getContext(), "Couldn't fetch ongoing ride", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(request);
    }

    private void fetchDriver(int driverId) {
        String url = BASEURL + "/profile/driver?driverId=" + driverId;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try{
                    driverNameTextView.setText(jsonObject.getString("fullname"));
                    String makeAndLicense = jsonObject.getString("make") + " • "
                            + jsonObject.getString("license");
                    makeTextView.setText(makeAndLicense);
                    ongoingRatingBar.setRating(jsonObject.getInt("average_rating"));
                    ongoingRatingTextView.setText(Integer.toString(jsonObject.getInt("average_rating")));

                    ongoingLinLay.setVisibility(View.VISIBLE);
                    driverTimeLinlay.setVisibility(View.VISIBLE);
                    noOngoingLinlay.setVisibility(View.GONE);
                }catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                volleyError.printStackTrace();
            }
        });

        queue.add(request);
    }

    private void initializeViewComponents(@NonNull View view) {
        ongoingLinLay = view.findViewById(R.id.ongoingLinlay);
        driverTimeLinlay = view.findViewById(R.id.ongoingDriverTimeLinlay);
        noOngoingLinlay = view.findViewById(R.id.noOngoingLinlay);
        driverStatusTextView = view.findViewById(R.id.driverStatusTextView);
        driverNameTextView = view.findViewById(R.id.driverNameTextView);
        makeTextView = view.findViewById(R.id.makeTextView);
        ongoingRatingBar = view.findViewById(R.id.ongoingRatingBar);
        ongoingRatingTextView = view.findViewById(R.id.ongoingRatingTextView);
        ongoingAmountTextView = view.findViewById(R.id.ongoingAmountTextView);
        ongoingRideType = view.findViewById(R.id.ongoingRideType);
        ongoingFrom = view.findViewById(R.id.ongoingFrom);
        ongoingTo = view.findViewById(R.id.ongoingTo);
        ongoingRideStatusTextView = view.findViewById(R.id.ongoingRideStatusTextView);
        ongoingRideTimeTextView = view.findViewById(R.id.ongoingRideTimeTextView);
    }
}