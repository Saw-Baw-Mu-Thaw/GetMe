package com.android.getme.ViewModels;

public class RideRequestViewModel {
    private int rideId;
    private String name;
    private String rating;
    private String price;
    private String distance;
    private String pickupLocation;
    private String dropoffLocation;


    private double pickupLat;
    private double pickupLng;
    private double dropoffLat;
    private double dropoffLng;

    public RideRequestViewModel(int rideId, String name, String rating, String price, String distance,
                                String pickupLocation, String dropoffLocation,
                                double pickupLat, double pickupLng, double dropoffLat, double dropoffLng) {
        this.rideId = rideId;
        this.name = name;
        this.rating = rating;
        this.price = price;
        this.distance = distance;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;

        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.dropoffLat = dropoffLat;
        this.dropoffLng = dropoffLng;
    }


    public int getRideId() { return rideId; }
    public String getName() { return name; }
    public String getRating() { return rating; }
    public String getPrice() { return price; }
    public String getDistance() { return distance; }
    public String getPickupLocation() { return pickupLocation; }
    public String getDropoffLocation() { return dropoffLocation; }

    public double getPickupLat() { return pickupLat; }
    public double getPickupLng() { return pickupLng; }
    public double getDropoffLat() { return dropoffLat; }
    public double getDropoffLng() { return dropoffLng; }
}