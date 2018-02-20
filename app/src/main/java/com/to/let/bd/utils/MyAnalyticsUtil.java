package com.to.let.bd.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.to.let.bd.common.BaseActivity;

public class MyAnalyticsUtil {
    private FirebaseAnalytics firebaseAnalytics;

    public MyAnalyticsUtil(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    private static final String keyFavItem = "favItem";
    private static final String keyAdId = "adId";
    private static final String keyUserId = "userId";
    private static final String keyFavourite = "favourite";

    public void favItem(String adId, boolean isFav) {
        Bundle bundle = new Bundle();
        bundle.putString(keyAdId, adId);
        bundle.putBoolean(keyFavourite, isFav);
        bundle.putString(keyUserId, BaseActivity.getUid());
        firebaseAnalytics.logEvent(keyFavItem, bundle);
    }

    private static final String keyAdDetailsEvent = "adDetailsEvent";

    public void adDetailsEvent(String adId) {
        Bundle bundle = new Bundle();
        bundle.putString(keyAdId, adId);
        bundle.putString(keyUserId, BaseActivity.getUid());
        firebaseAnalytics.logEvent(keyAdDetailsEvent, bundle);
    }

    public static final String keySortEvent = "sortEvent";
    public static final String keyShareEvent = "shareEvent";
    public static final String keyBookingEvent = "bookingEvent";
    public static final String keyCallEvent = "callEvent";
    public static final String keyEmailEvent = "emailEvent";
    public static final String keyLogoutEvent = "logoutEvent";
    public static final String keySmartListEvent = "smartListEvent";
    public static final String keyNearestAdEvent = "nearestAdEvent";
    public static final String keyShowMapFromAdDetailsEvent = "showMapFromAdDetailsEvent";
    public static final String keyMapFilterEvent = "mapFilterEvent";
    public static final String keyGeoQueryFailed = "geoQueryFailed";
    public static final String keyGeoQuerySucceed = "geoQuerySucceed";
    public static final String keyFavFailed = "favFailed";
    public static final String keyGoogleLogin = "googleLogin";
    public static final String keyFirebaseLogin = "firebaseLogin";
    public static final String keyEditAdEvent = "editAdEvent";
    public static final String keyNewAdEvent = "newAdEvent";
    public static final String keySubmitTryAdEvent = "submitTryAdEvent";
    public static final String keyAdDeleteEvent = "adDeleteEvent";
    public static final String keyAdRepublishEvent = "adRepublishEvent";
    public static final String keyWantToAddMediaEvent = "wantToAddMediaEvent";
    public static final String keyStoragePermissionEvent = "keyStoragePermissionEvent";
    public static final String keySubmitTryMediaEvent = "submitTryMediaEvent";
    public static final String keyMediaUploadEvent = "mediaUploadEvent";
    public static final String keyMediaDeleteEvent = "mediaDeleteEvent";
    public static final String keyPhoneNumberVerificationEvent = "phoneNumberVerificationEvent";
    public static final String keyPhoneNumberAddedEvent = "phoneNumberAddedEvent";
    public static final String keyAdLoadedFailedEvent = "adLoadedFailedEvent";
    public static final String keyFirebaseDatabaseQueryRefEvent = "firebaseDatabaseQueryRefEvent";
    public static final String keyNoNetworkEvent = "keyNoNetworkEvent";

    private static final String keyEventValue = "eventValue";

    public void sendEvent(String eventKey, String eventValue) {
        Bundle bundle = new Bundle();
        bundle.putString(keyEventValue, eventValue);
        bundle.putString(keyUserId, BaseActivity.getUid());
        firebaseAnalytics.logEvent(eventKey, bundle);
    }

    public static final String searchTypeNormal = "searchTypeNormal";
    public static final String searchTypeGoogleMap = "searchTypeGoogleMap";
    public static final String placePickerMapView = "placePickerMapView";
    public static final String placePickerNewAdView = "placePickerNewAdView";
    public static final String keySearchType = "searchType";
    public static final String keySearchLat = "searchLat";
    public static final String keySearchLng = "searchLng";
    public static final String keySearchName = "searchName";

    public static final String keyFromDateTime = "fromDateTime";
    public static final String keyToDateTime = "toDateTime";
    public static final String keyRentMinLong = "rentMinLong";
    public static final String keyRentMaxLong = "rentMaxLong";

    private static final String keySearch = "search";

    public void searchEvent(Bundle bundle) {
        firebaseAnalytics.logEvent(keySearch, bundle);
    }
}
