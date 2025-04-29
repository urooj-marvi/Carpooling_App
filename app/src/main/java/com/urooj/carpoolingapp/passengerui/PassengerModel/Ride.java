package com.urooj.carpoolingapp.passengerui.PassengerModel;
public class Ride {
    public String driverId, startingPlace, destinationPlace;
    public double startingLat, startingLng, destinationLat, destinationLng;
    public int availableSeats;

    public Ride() {} // Required for Firebase

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getStartingPlace() {
        return startingPlace;
    }

    public void setStartingPlace(String startingPlace) {
        this.startingPlace = startingPlace;
    }

    public String getDestinationPlace() {
        return destinationPlace;
    }

    public void setDestinationPlace(String destinationPlace) {
        this.destinationPlace = destinationPlace;
    }

    public double getStartingLat() {
        return startingLat;
    }

    public void setStartingLat(double startingLat) {
        this.startingLat = startingLat;
    }

    public double getStartingLng() {
        return startingLng;
    }

    public void setStartingLng(double startingLng) {
        this.startingLng = startingLng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Ride(String driverId, String startingPlace, double startingLat, double startingLng,
                String destinationPlace, double destinationLat, double destinationLng, int availableSeats) {
        this.driverId = driverId;
        this.startingPlace = startingPlace;
        this.startingLat = startingLat;
        this.startingLng = startingLng;
        this.destinationPlace = destinationPlace;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.availableSeats = availableSeats;
    }
    
}
