package com.android.getme.ViewModels;

public class RideRequestViewModel {
    String name;
    String rating;
    String price;
    String distance;
    String pickupLocation;
    String dropoffLocation;

    public RideRequestViewModel(String name, String rating, String price, String distance, String pickupLocation, String dropoffLocation) {
        this.name = name;
        this.rating = rating;
        this.price = price;
        this.distance = distance;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }
}
