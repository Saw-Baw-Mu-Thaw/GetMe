package com.android.getme.ViewModels;


import androidx.lifecycle.ViewModel;

import org.osmdroid.util.GeoPoint;

public class DropoffViewModel extends PickupViewModel {

    public GeoPoint dropoffLocation;
    public String dropoffName;
    public String dropoffAddress;
}
