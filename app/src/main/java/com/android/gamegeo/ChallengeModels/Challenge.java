package com.android.gamegeo.ChallengeModels;

public class Challenge {
    private double latitude;
    private double longitude;
    private String id;

    public Challenge(double latitude, double longitude, String id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getId() {
        return id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setId(String id) {
        this.id = id;
    }
}
