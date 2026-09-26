package com.example.attendanceapp.domain.model;

public class WorkLocation {
    private final String locationId;
    private final String addressName;     // "Văn phòng Tầng 3"
    private final double latitude;
    private final double longitude;
    private final int allowedRadiusMeters; // Bán kính cho phép checkin (m)

    public WorkLocation(String locationId, String addressName, double latitude,
                        double longitude, int allowedRadiusMeters) {
        this.locationId = locationId;
        this.addressName = addressName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.allowedRadiusMeters = allowedRadiusMeters;
    }

    public String getLocationId() { return locationId; }
    public String getAddressName() { return addressName; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getAllowedRadiusMeters() { return allowedRadiusMeters; }
}