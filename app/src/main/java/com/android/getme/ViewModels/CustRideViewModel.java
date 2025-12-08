package com.android.getme.ViewModels;


import androidx.lifecycle.ViewModel;

import org.osmdroid.util.GeoPoint;

import java.sql.Date;

public class CustRideViewModel extends ViewModel {



    public int rideId;
    public int custId;
    public int driverId;
    public String vehicleType;
    public String payment;
    public int amount;
    public String locationFrom;
    public GeoPoint pickup;
    public String locationTo;
    public GeoPoint dropoff;
    public double distance;
    public int duration;
    public String status;
    public Date date;

}
