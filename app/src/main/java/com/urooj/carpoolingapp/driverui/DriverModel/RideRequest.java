package com.urooj.carpoolingapp.driverui.DriverModel;

public class RideRequest {
    private String requestId;
    private String passengerId;
    private String driverId;
    private String status;
    private long timestamp;

    public RideRequest() {
        // Required empty constructor for Firebase
    }

    public RideRequest(String requestId, String passengerId, String driverId, String status, long timestamp) {
        this.requestId = requestId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}