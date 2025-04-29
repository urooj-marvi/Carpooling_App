package com.urooj.carpoolingapp.passengerui.PassengerModel;
public class DriverData {
    private String name;
    private String vehicle;
    private String route;
    private double latitude;
    private double longitude;
    private boolean isAvailable;

    // Constructor, getters, and setters
    public DriverData(String name, String vehicle, String route, double latitude, double longitude, boolean isAvailable) {
        this.name = name;
        this.vehicle = vehicle;
        this.route = route;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAvailable = isAvailable;
    }

    public String getName() {
        return name;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getRoute() {
        return route;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}