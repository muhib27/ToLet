package com.to.let.bd.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.SmartToLetConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class AdInfo {
    private String adId;
    private int startingMonth;
    private int startingDate;
    private int startingYear;

    private double latitude;
    private double longitude;

    private String fullAddress;

    private String country;
    private String division;
    private String district;
    private String subDistrict;
    private String knownAsArea;

    private int flatType;
    private int bedRoom;
    private int bathroom;
    private int balcony;

    private int kitchen;
    private String houseNameOrNumber;
    private int floorNumber;
    private int flatFacing;

    private long flatSpace;
    private int hasDrawingDining;
    private int electricity;
    private int gasFacility;
    private int water;
    private int lift;
    private int generator;
    private int securityGuard;

    private long flatRent;
    private long othersFee;

    private String userId;

    private ArrayList<ImageInfo> images;
    private ImageInfo map;

    public AdInfo() {

    }

    public AdInfo(String adId, String userId) {
        this.adId = adId;
        this.startingMonth = 12;
        this.startingDate = 1;
        this.startingYear = 2017;
        this.latitude = 23.43;
        this.longitude = 90.34;
        this.fullAddress = "";
        this.country = "";
        this.division = "";
        this.district = "";
        this.subDistrict = "";
        this.knownAsArea = "";
        this.flatType = 1;
        this.houseNameOrNumber = "";
        this.floorNumber = 2;
        this.flatFacing = 1;
        this.flatSpace = 1200;
        this.bedRoom = 2;
        this.bathroom = 3;
        this.balcony = 2;
        this.kitchen = 1;
        this.hasDrawingDining = 1;
        this.electricity = -1;
        this.gasFacility = 1;
        this.water = 1000;
        this.lift = 0;
        this.generator = 1;
        this.flatRent = 20000;
        this.othersFee = 3000;

        this.userId = userId;
    }

    public AdInfo(String adId, int startingDate, int startingMonth, int startingYear, double latitude, double longitude,
                  String fullAddress, String country, String division, String district, String subDistrict, String knownAsArea,
                  int flatType, int bedRoom, int bathroom, int balcony, int flatFacing, int kitchen,
                  String houseNameOrNumber, int floorNumber, int hasDrawingDining,
                  int electricity, int gasFacility, int water, int lift, int generator, int securityGuard,
                  long flatSpace, long flatRent, long othersFee, String userId) {
        this.adId = adId;
        this.startingMonth = startingMonth;
        this.startingDate = startingDate;
        this.startingYear = startingYear;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fullAddress = fullAddress;
        this.country = country;
        this.division = division;
        this.district = district;
        this.subDistrict = subDistrict;
        this.knownAsArea = knownAsArea;
        this.flatType = flatType;
        this.houseNameOrNumber = houseNameOrNumber;
        this.floorNumber = floorNumber;
        this.flatFacing = flatFacing;
        this.flatSpace = flatSpace;
        this.bedRoom = bedRoom;
        this.bathroom = bathroom;
        this.balcony = balcony;
        this.kitchen = kitchen;
        this.hasDrawingDining = hasDrawingDining;
        this.electricity = electricity;
        this.gasFacility = gasFacility;
        this.water = water;
        this.lift = lift;
        this.generator = generator;
        this.securityGuard = securityGuard;
        this.flatRent = flatRent;
        this.othersFee = othersFee;
        this.userId = userId;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public int getStartingMonth() {
        return startingMonth;
    }

    public void setStartingMonth(int startingMonth) {
        this.startingMonth = startingMonth;
    }

    public int getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(int startingDate) {
        this.startingDate = startingDate;
    }

    public int getStartingYear() {
        return startingYear;
    }

    public void setStartingYear(int startingYear) {
        this.startingYear = startingYear;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getSubDistrict() {
        return subDistrict;
    }

    public void setSubDistrict(String subDistrict) {
        this.subDistrict = subDistrict;
    }

    public String getKnownAsArea() {
        return knownAsArea;
    }

    public void setKnownAsArea(String knownAsArea) {
        this.knownAsArea = knownAsArea;
    }

    public int getFlatType() {
        return flatType;
    }

    public void setFlatType(int flatType) {
        this.flatType = flatType;
    }

    public String getHouseNameOrNumber() {
        return houseNameOrNumber;
    }

    public void setHouseNameOrNumber(String houseNameOrNumber) {
        this.houseNameOrNumber = houseNameOrNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public int getFlatFacing() {
        return flatFacing;
    }

    public void setFlatFacing(int flatFacing) {
        this.flatFacing = flatFacing;
    }

    public long getFlatSpace() {
        return flatSpace;
    }

    public void setFlatSpace(long flatSpace) {
        this.flatSpace = flatSpace;
    }

    public int getBedRoom() {
        return bedRoom;
    }

    public void setBedRoom(int bedRoom) {
        this.bedRoom = bedRoom;
    }

    public int getBathroom() {
        return bathroom;
    }

    public void setBathroom(int bathroom) {
        this.bathroom = bathroom;
    }

    public int getBalcony() {
        return balcony;
    }

    public void setBalcony(int balcony) {
        this.balcony = balcony;
    }

    public int getKitchen() {
        return kitchen;
    }

    public void setKitchen(int kitchen) {
        this.kitchen = kitchen;
    }

    public int isHasDrawingDining() {
        return hasDrawingDining;
    }

    public void setHasDrawingDining(int hasDrawingDining) {
        this.hasDrawingDining = hasDrawingDining;
    }

    public int getElectricity() {
        return electricity;
    }

    public void setElectricity(int electricity) {
        this.electricity = electricity;
    }

    public int getGasFacility() {
        return gasFacility;
    }

    public void setGasFacility(int gasFacility) {
        this.gasFacility = gasFacility;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getLift() {
        return lift;
    }

    public void setLift(int lift) {
        this.lift = lift;
    }

    public int getGenerator() {
        return generator;
    }

    public void setGenerator(int generator) {
        this.generator = generator;
    }

    public int getSecurityGuard() {
        return securityGuard;
    }

    public void setSecurityGuard(int securityGuard) {
        this.securityGuard = securityGuard;
    }

    public long getFlatRent() {
        return flatRent;
    }

    public void setFlatRent(long flatRent) {
        this.flatRent = flatRent;
    }

    public long getOthersFee() {
        return othersFee;
    }

    public void setOthersFee(long othersFee) {
        this.othersFee = othersFee;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<ImageInfo> getImages() {
        return images;
    }

    public void setImages(ArrayList<ImageInfo> images) {
        this.images = images;
    }

    public ImageInfo getMap() {
        return map;
    }

    public void setMap(ImageInfo map) {
        this.map = map;
    }

    // [START post_to_map]
    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBConstants.adId, adId);
        result.put(DBConstants.startingDate, startingDate);
        result.put(DBConstants.startingMonth, startingMonth);
        result.put(DBConstants.startingYear, startingYear);

        result.put(DBConstants.latitude, latitude);
        result.put(DBConstants.longitude, longitude);

        result.put(DBConstants.fullAddress, fullAddress);
        result.put(DBConstants.country, country);
        result.put(DBConstants.division, division);
        result.put(DBConstants.district, district);
        result.put(DBConstants.subDistrict, subDistrict);
        result.put(DBConstants.knownAsArea, knownAsArea);

        result.put(DBConstants.flatType, flatType);
        result.put(DBConstants.bedRoom, bedRoom);
        result.put(DBConstants.bathroom, bathroom);
        result.put(DBConstants.balcony, balcony);
        result.put(DBConstants.flatFacing, flatFacing);
        result.put(DBConstants.kitchen, kitchen);

        result.put(DBConstants.houseNameOrNumber, houseNameOrNumber);
        result.put(DBConstants.floorNumber, floorNumber);
        result.put(DBConstants.hasDrawingDining, hasDrawingDining);

        result.put(DBConstants.electricity, electricity);
        result.put(DBConstants.gasFacility, gasFacility);
        result.put(DBConstants.water, water);
        result.put(DBConstants.lift, lift);
        result.put(DBConstants.generator, generator);
        result.put(DBConstants.securityGuard, securityGuard);

        result.put(DBConstants.flatSpace, flatSpace);
        result.put(DBConstants.flatRent, flatRent);
        result.put(DBConstants.othersFee, othersFee);
        result.put(DBConstants.userId, userId);
        result.put(SmartToLetConstants.keyTimestamp, ServerValue.TIMESTAMP);
        return result;
    }
    // [END post_to_map]
}
