package com.example.drunk;

public class UserLocation {
    public double lat;
    public double lng;
    public long lastUpdated;
    public String token;

    public UserLocation() {}  // needed for Firebase

    public UserLocation(double lat, double lng, long lastUpdated, String token) {
        this.lat = lat;
        this.lng = lng;
        this.lastUpdated = lastUpdated;
        this.token = token;
    }
}
