package com.urooj.carpoolingapp.passengerui.PassengerModel;

public class DriverModel {
    private double latitude;
    private double longitude;
    private String name;

    public DriverModel() {
        // Default constructor required for Firebase
    }

    public DriverModel(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }
}
