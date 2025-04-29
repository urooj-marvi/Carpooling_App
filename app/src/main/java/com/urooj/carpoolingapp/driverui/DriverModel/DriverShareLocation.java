package com.urooj.carpoolingapp.driverui.DriverModel;

public class DriverShareLocation {
         private String id;
        private String name;
        private double latitude;
        private double longitude;

        public DriverShareLocation() {
            // Default constructor required for calls to DataSnapshot.getValue(ShareLocation.class)
        }

        public DriverShareLocation(String id, String name, double latitude, double longitude) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        // Setters
        public void setId(String id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
}

