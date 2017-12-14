package com.to.let.bd.model;

import com.google.firebase.database.Exclude;
import com.to.let.bd.activities.SplashActivity;
import com.to.let.bd.utils.DBConstants;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdInfo implements Serializable {
    public String adId;

    public int startingMonth;
    public int startingDate;
    public int startingYear;

    public long startingFinalDate;

    public double latitude;
    public double longitude;

    public String fullAddress;

    public String country;
    public String division;
    public String district;
    public String subDistrict;
    public String knownAsArea;

    public long flatSpace;
    public long flatRent;
    public long othersFee;

    public long createdTime;
    public long modifiedTime;

    public ArrayList<ImageInfo> images;
    public ImageInfo map;

    public String houseNameOrNumber;
    public int floorNumber;
    public String flatFacing;
    public String flatDescription;

    public String flatType;

    public FamilyInfo familyInfo;
    public MessInfo messInfo;
    public SubletInfo subletInfo;
    public OthersInfo othersInfo;

    public String userId;
    public Map<String, Boolean> fav = new HashMap<>();
    public int favCount = 0;
    public Map<String, String> report = new HashMap<>();
    public int reportCount = 0;

    public String mobileNumber;
    public String emailAddress;

    public AdInfo() {

    }

    public AdInfo(String adId, String userId) {
        this.adId = adId;
        this.userId = userId;
    }

    public AdInfo(String adId, int startingMonth, int startingDate, int startingYear,
                  double latitude, double longitude,
                  String fullAddress, String country, String division, String district, String subDistrict, String knownAsArea,
                  long flatSpace, long flatRent, long othersFee,
                  String houseNameOrNumber, int floorNumber, String flatFacing, String flatDescription,
                  String flatType,
                  FamilyInfo familyInfo, MessInfo messInfo, SubletInfo subletInfo, OthersInfo othersInfo,
                  String userId, String mobileNumber, String emailAddress) {
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
        this.flatSpace = flatSpace;
        this.flatRent = flatRent;
        this.othersFee = othersFee;
        this.houseNameOrNumber = houseNameOrNumber;
        this.floorNumber = floorNumber;
        this.flatFacing = flatFacing;
        this.flatDescription = flatDescription;

        this.flatType = flatType;
        this.familyInfo = familyInfo;
        this.messInfo = messInfo;
        this.subletInfo = subletInfo;
        this.othersInfo = othersInfo;

        this.userId = userId;
        this.mobileNumber = mobileNumber;
        this.emailAddress = emailAddress;
    }

    //    public AdInfo(String adId, int startingDate, int startingMonth, int startingYear,
//                  double latitude, double longitude,
//                  String fullAddress, String country, String division, String district, String subDistrict, String knownAsArea,
//                  String flatType,
//                  String houseNameOrNumber, int floorNumber,
//                  long flatSpace, long flatRent, long othersFee, String userId) {
//        this.adId = adId;
//        this.startingMonth = startingMonth;
//        this.startingDate = startingDate;
//        this.startingYear = startingYear;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.fullAddress = fullAddress;
//        this.country = country;
//        this.division = division;
//        this.district = district;
//        this.subDistrict = subDistrict;
//        this.knownAsArea = knownAsArea;
//        this.flatType = flatType;
//        this.houseNameOrNumber = houseNameOrNumber;
//        this.floorNumber = floorNumber;
//        this.flatFacing = flatFacing;
//        this.flatSpace = flatSpace;
//
//        this.bedRoom = bedRoom;
//        this.bathroom = bathroom;
//        this.balcony = balcony;
//        this.kitchen = kitchen;
//        this.hasDrawingDining = hasDrawingDining;
//
//        this.electricity = electricity;
//        this.gasFacility = gasFacility;
//        this.water = water;
//        this.lift = lift;
//        this.generator = generator;
//        this.securityGuard = securityGuard;
//
//        this.flatRent = flatRent;
//        this.othersFee = othersFee;
//        this.userId = userId;
//    }

//    public String getAdId() {
//        return adId;
//    }
//
//    public void setAdId(String adId) {
//        this.adId = adId;
//    }
//
//    public int getStartingMonth() {
//        return startingMonth;
//    }
//
//    public void setStartingMonth(int startingMonth) {
//        this.startingMonth = startingMonth;
//    }
//
//    public int getStartingDate() {
//        return startingDate;
//    }
//
//    public void setStartingDate(int startingDate) {
//        this.startingDate = startingDate;
//    }
//
//    public int getStartingYear() {
//        return startingYear;
//    }
//
//    public void setStartingYear(int startingYear) {
//        this.startingYear = startingYear;
//    }
//
//    public long getStartingFinalDate() {
//        return startingFinalDate;
//    }
//
//    public void setStartingFinalDate(long startingFinalDate) {
//        this.startingFinalDate = startingFinalDate;
//    }
//
//    public double getLatitude() {
//        return latitude;
//    }
//
//    public void setLatitude(double latitude) {
//        this.latitude = latitude;
//    }
//
//    public double getLongitude() {
//        return longitude;
//    }
//
//    public void setLongitude(double longitude) {
//        this.longitude = longitude;
//    }
//
//    public String getFullAddress() {
//        return fullAddress;
//    }
//
//    public void setFullAddress(String fullAddress) {
//        this.fullAddress = fullAddress;
//    }
//
//    public String getCountry() {
//        return country;
//    }
//
//    public void setCountry(String country) {
//        this.country = country;
//    }
//
//    public String getDivision() {
//        return division;
//    }
//
//    public void setDivision(String division) {
//        this.division = division;
//    }
//
//    public String getDistrict() {
//        return district;
//    }
//
//    public void setDistrict(String district) {
//        this.district = district;
//    }
//
//    public String getSubDistrict() {
//        return subDistrict;
//    }
//
//    public void setSubDistrict(String subDistrict) {
//        this.subDistrict = subDistrict;
//    }
//
//    public String getKnownAsArea() {
//        return knownAsArea;
//    }
//
//    public void setKnownAsArea(String knownAsArea) {
//        this.knownAsArea = knownAsArea;
//    }
//
//    public long getFlatRent() {
//        return flatRent;
//    }
//
//    public void setFlatRent(long flatRent) {
//        this.flatRent = flatRent;
//    }
//
//    public long getOthersFee() {
//        return othersFee;
//    }
//
//    public void setOthersFee(long othersFee) {
//        this.othersFee = othersFee;
//    }
//
//    public long getCreatedTime() {
//        return createdTime;
//    }
//
//    public void setCreatedTime(long createdTime) {
//        this.createdTime = createdTime;
//    }
//
//    public long getModifiedTime() {
//        return modifiedTime;
//    }
//
//    public void setModifiedTime(long modifiedTime) {
//        this.modifiedTime = modifiedTime;
//    }
//
//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//    public ArrayList<ImageInfo> getImages() {
//        return images;
//    }
//
//    public void setImages(ArrayList<ImageInfo> images) {
//        this.images = images;
//    }
//
//    public ImageInfo getMap() {
//        return map;
//    }
//
//    public void setMap(ImageInfo map) {
//        this.map = map;
//    }
//
//    public void setFlatType(String flatType) {
//        this.flatType = flatType;
//    }
//
//    public String getHouseNameOrNumber() {
//        return houseNameOrNumber;
//    }
//
//    public void setHouseNameOrNumber(String houseNameOrNumber) {
//        this.houseNameOrNumber = houseNameOrNumber;
//    }
//
//    public int getFloorNumber() {
//        return floorNumber;
//    }
//
//    public void setFloorNumber(int floorNumber) {
//        this.floorNumber = floorNumber;
//    }
//
//    public String getFlatFacing() {
//        return flatFacing;
//    }
//
//    public void setFlatFacing(String flatFacing) {
//        this.flatFacing = flatFacing;
//    }
//
//    public long getFlatSpace() {
//        return flatSpace;
//    }
//
//    public void setFlatSpace(long flatSpace) {
//        this.flatSpace = flatSpace;
//    }
//
//    public String getFlatDescription() {
//        return flatDescription;
//    }
//
//    public void setFlatDescription(String flatDescription) {
//        this.flatDescription = flatDescription;
//    }
//
//    public String getFlatType() {
//        return flatType;
//    }
//
//    public FamilyInfo getFamilyInfo() {
//        return familyInfo;
//    }
//
//    public void setFamilyInfo(FamilyInfo familyInfo) {
//        this.familyInfo = familyInfo;
//    }
//
//    public MessInfo getMessInfo() {
//        return messInfo;
//    }
//
//    public void setMessInfo(MessInfo messInfo) {
//        this.messInfo = messInfo;
//    }
//
//    public SubletInfo getSubletInfo() {
//        return subletInfo;
//    }
//
//    public void setSubletInfo(SubletInfo subletInfo) {
//        this.subletInfo = subletInfo;
//    }
//
//    public OthersInfo getOthersInfo() {
//        return othersInfo;
//    }
//
//    public void setOthersInfo(OthersInfo othersInfo) {
//        this.othersInfo = othersInfo;
//    }

    // [START post_to_map]
    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBConstants.adId, adId);
        result.put(DBConstants.startingDate, startingDate);
        result.put(DBConstants.startingMonth, startingMonth);
        result.put(DBConstants.startingYear, startingYear);

        startingFinalDate = Long.parseLong(startingYear
                + SplashActivity.formatterTwoDigit.format(startingMonth)
                + SplashActivity.formatterTwoDigit.format(startingDate));

        result.put(DBConstants.startingFinalDate, startingFinalDate);

        result.put(DBConstants.latitude, latitude);
        result.put(DBConstants.longitude, longitude);

        result.put(DBConstants.fullAddress, fullAddress);
        result.put(DBConstants.country, country);
        result.put(DBConstants.division, division);
        result.put(DBConstants.district, district);
        result.put(DBConstants.subDistrict, subDistrict);
        result.put(DBConstants.knownAsArea, knownAsArea);

        result.put(DBConstants.flatSpace, flatSpace);
        result.put(DBConstants.flatRent, flatRent);
        result.put(DBConstants.othersFee, othersFee);

        result.put(DBConstants.houseNameOrNumber, houseNameOrNumber);
        result.put(DBConstants.floorNumber, floorNumber);
        result.put(DBConstants.flatFacing, flatFacing);
        result.put(DBConstants.flatDescription, flatDescription);

        result.put(DBConstants.flatType, flatType);

        result.put(DBConstants.familyInfo, familyInfo);
        result.put(DBConstants.messInfo, messInfo);
        result.put(DBConstants.subletInfo, subletInfo);
        result.put(DBConstants.othersInfo, othersInfo);

        result.put(DBConstants.userId, userId);
        result.put(DBConstants.mobileNumber, mobileNumber);
        result.put(DBConstants.emailAddress, emailAddress);
        return result;
    }
    // [END post_to_map]
}
