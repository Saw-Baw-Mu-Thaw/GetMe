package com.android.getme.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.getme.Activities.DriverEarningActivity;
import com.android.getme.Activities.TripHistoryActivity;
import com.android.getme.R;

public class OfflineFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_offline, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the view relative to the Fragment's root view
        CardView earningDisplay = view.findViewById(R.id.displayEarning);

        if (earningDisplay != null) {
            earningDisplay.setOnClickListener(v -> {
                // Use getActivity() for the context
                Intent intent = new Intent(getActivity(), DriverEarningActivity.class);
                startActivity(intent);
            });
        }

        CardView tripHistoryDisplay = view.findViewById(R.id.driverTripHistory);

        if (tripHistoryDisplay != null) {
            tripHistoryDisplay.setOnClickListener(v -> {
                // Use getActivity() for the context
                Intent intent = new Intent(getActivity(), TripHistoryActivity.class);
                startActivity(intent);
            });
        }
    }
}