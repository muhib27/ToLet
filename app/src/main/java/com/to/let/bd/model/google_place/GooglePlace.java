package com.to.let.bd.model.google_place;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class GooglePlace {
    public String status;
    @Expose
    public String error_message;
    public ArrayList<GooglePlaceResult> results;

//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getError_message() {
//        return error_message;
//    }
//
//    public void setError_message(String error_message) {
//        this.error_message = error_message;
//    }
//
//    public ArrayList<GooglePlaceResult> getResults() {
//        return results;
//    }
//
//    public void setResults(ArrayList<GooglePlaceResult> results) {
//        this.results = results;
//    }
}
