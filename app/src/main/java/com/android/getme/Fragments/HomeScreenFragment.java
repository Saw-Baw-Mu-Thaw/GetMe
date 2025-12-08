package com.android.getme.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.getme.Listeners.CustHomeFragListener;
import com.android.getme.R;
import com.android.getme.ViewModels.CustRideViewModel;

public class HomeScreenFragment extends Fragment {

    CustRideViewModel custRideViewModel;
    CustHomeFragListener listener;
    LinearLayout searchRideLinLay;
    LinearLayout rideStandardLinLay;
    LinearLayout rideEconomyLinLay;
    LinearLayout rideBikeLinLay;

    public HomeScreenFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        custRideViewModel = viewModelProvider.get(CustRideViewModel.class);

        if(getContext() instanceof CustHomeFragListener) {
            listener = (CustHomeFragListener) getContext();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        searchRideLinLay = view.findViewById(R.id.searchRideLinLay);
        rideStandardLinLay = view.findViewById(R.id.rideStandardLinlay);
        rideEconomyLinLay = view.findViewById(R.id.rideEconomyLinlay);
        rideBikeLinLay = view.findViewById(R.id.rideBikeLinlay);

        searchRideLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBookRideClicked("None");
            }
        });

        rideStandardLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBookRideClicked("Standard");
            }
        });

        rideEconomyLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBookRideClicked("Economy");
            }
        });

        rideBikeLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBookRideClicked("Bike");
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
}