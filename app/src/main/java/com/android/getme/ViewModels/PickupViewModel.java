package com.android.getme.ViewModels;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.lifecycle.ViewModel;

import com.android.getme.Services.LocationSearchService;

import org.osmdroid.util.GeoPoint;

public class PickupViewModel extends ViewModel {

    public GeoPoint currLocation;

    public String vehicleType;
    public int custId;

    public GeoPoint pickupLocation;
    public String pickupName;
    public String pickupAddress;


}
