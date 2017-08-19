package com.to.let.bd.utils;

public class Urls {
    public static String getGoogleApiKey() {
        return "AIzaSyA_ZbTlK7bKUpCLzHB2QvHOyyMTh-tqlIA";
    }

    public static String getGoogleApiBaseUrl() {
        return "https://maps.googleapis.com";
    }

    public static String getGooglePlaceNearBy() {
        return getGoogleApiBaseUrl() + "/maps/api/place/nearbysearch/json?";
    }
}
