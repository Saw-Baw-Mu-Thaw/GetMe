package com.android.getme.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.getme.Adapters.RideRequestAdapter;
import com.android.getme.R;
import com.android.getme.ViewModels.RideRequestViewModel;

import java.util.ArrayList;
import java.util.List;

public class OnlineFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_online, container, false);

        // Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.rvRideRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create Dummy Data
        List<RideRequestViewModel> requests = new ArrayList<>();
        requests.add(new RideRequestViewModel("Sarah Johnson", "★ 4.8", "$12.50", "2.1 km", "Downtown Mall", "Central Station"));
        requests.add(new RideRequestViewModel("Mike Chen", "★ 4.6", "$8.75", "1.5 km", "Tech Park", "Airport Terminal 2"));
        requests.add(new RideRequestViewModel("Emily Davis", "★ 4.9", "$22.00", "5.4 km", "City Center", "North Suburbs"));

        // Set Adapter
        RideRequestAdapter adapter = new RideRequestAdapter(requests);
        recyclerView.setAdapter(adapter);

        return view;
    }
}