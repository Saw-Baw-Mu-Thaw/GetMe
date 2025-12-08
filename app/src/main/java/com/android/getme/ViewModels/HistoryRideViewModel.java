package com.android.getme.ViewModels;

public class HistoryRideViewModel {

    int rideId;
    String locationFrom;
    String locationTo;
    String date;
    double amount;
    String duration;
    String distance;
    String status;

    public HistoryRideViewModel(int rideId, String from, String to, String date, double amount, String duration, String distance, String status) {
        this.rideId = rideId;
        this.locationFrom = from;
        this.locationTo = to;
        this.date = date;
        this.amount = amount;
        this.duration = duration;
        this.distance = distance;
        this.status = status;
    }
}