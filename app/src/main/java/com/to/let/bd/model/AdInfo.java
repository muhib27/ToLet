package com.to.let.bd.model;

import com.google.firebase.database.Exclude;
import com.to.let.bd.activities.SplashActivity;
import com.to.let.bd.utils.AppConstants;
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

    public boolean isActive = true;

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

    // [START post_to_map]
    @Exclude
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(DBConstants.adId, adId);
        result.put(DBConstants.startingDate, startingDate);
        result.put(DBConstants.startingMonth, startingMonth);
        result.put(DBConstants.startingYear, startingYear);

        startingFinalDate = Long.parseLong(startingYear
                + AppConstants.twoDigitIntFormatter(startingMonth)
                + AppConstants.twoDigitIntFormatter(startingDate));

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

        result.put(DBConstants.isActive, true);
        return result;
    }
    // [END post_to_map]
}
