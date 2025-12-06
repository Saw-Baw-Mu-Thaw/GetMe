package com.android.getme.ViewModels;

public class HistoryRideViewModel {
    // Matches database columns
    int rideId;
    String locationFrom;
    String locationTo;
    String date;      // stored as datetime in DB
    double amount;    // stored as int/decimal in DB
    String duration;  // stored as int in DB, converted to String for UI
    String distance;  // stored as decimal in DB
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