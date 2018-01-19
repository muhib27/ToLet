package com.to.let.bd.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseMapActivity;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.model.google_place.GooglePlace;
import com.to.let.bd.model.google_place.GooglePlaceResult;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.JsonUtils;
import com.to.let.bd.utils.retrofit.RetrofitConstants;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends BaseMapActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MapActivity.class.getSimpleName();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionBank:
                showNearByBanks();
                return true;
            case R.id.actionSchool:
                showNearBySchools();
                return true;
            case R.id.actionMap:
                showOnlySelectedItem();
                return true;
            case R.id.actionDepartmentStore:
                showNearByStores();
                return true;
            case R.id.actionBusStand:
                showNearByBusStands();
                return true;
        }
        return false;
    }

    private void showNearByStores() {
        showOnlySelectedItem();
        initPlacesRequest(2);
    }

    private void showNearByBusStands() {
        showOnlySelectedItem();
        initPlacesRequest(3);
    }

    private void showNearBySchools() {
        showOnlySelectedItem();
        initPlacesRequest(1);
    }

    private void showNearByBanks() {
        showOnlySelectedItem();
        initPlacesRequest(0);
    }

    private void showOnlySelectedItem() {
        if (googleMap == null)
            return;
        googleMap.clear();
        addMarkerForSelectedFlat();
        addMarkerForNearbyFlat();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_map;
    }

    public static final String[] googlePlaceType = {"bank", "school", "department_store", "bus_station"};

    //    private double latitude, longitude;
    private String flatType;
    private AdInfo adInfo;
    private LatLng selectedLocation;

    @Override
    protected void onCreate() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
        flatType = bundle.getString(DBConstants.flatType);

        if (adInfo == null)
            return;

        selectedLocation = new LatLng(adInfo.latitude, adInfo.longitude);
        initBottomNavigation();
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.google_map_with_nearby);
    }

    private HashMap<String, GeoLocation> nearbySimilar = new HashMap<>();
    private GoogleMap googleMap;

    @Override
    protected void onMapReady2(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() == null) {
                    showSimpleDialog(R.string.something_wrong_please_report_a_bug);
                    return;
                }
                showProgressDialog();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference
                        .child(DBConstants.adList)
                        .child(flatType)
                        .child(marker.getTag().toString())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                closeProgressDialog();
                                AdInfo adInfo = dataSnapshot.getValue(AdInfo.class);
                                if (adInfo != null)
                                    startAdDetailsActivity(adInfo);
                                else
                                    showSimpleDialog(R.string.something_wrong_please_report_a_bug);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                closeProgressDialog();
                                showSimpleDialog(R.string.something_wrong_please_report_a_bug);
                            }
                        });
            }
        });
    }

//    private void startAdDetailsActivity() {
//        Intent adDetailsIntent = new Intent(this, AdDetailsActivity.class);
//        adDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(adDetailsIntent);
//    }

    private void loadData() {
        if (adInfo == null)
            return;

        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(adInfo.latitude, adInfo.longitude), DEFAULT_ZOOM));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DBConstants.geoFire + "/" + flatType);
        GeoFire geoFire = new GeoFire(ref);

        final double radius = 5.0;
        nearbySimilar.clear();
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(adInfo.latitude, adInfo.longitude), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation geoLocation) {
                showLog("onKeyEntered" + key);
                showLog("onKeyEntered" + geoLocation.toString());
                nearbySimilar.put(key, geoLocation);
            }

            @Override
            public void onKeyExited(String key) {
                showLog("onKeyExited" + key);
            }

            @Override
            public void onKeyMoved(String s, GeoLocation geoLocation) {
                showLog("onKeyMoved" + s);
                showLog("onKeyMoved" + geoLocation.toString());
            }

            @Override
            public void onGeoQueryReady() {
                showLog("onGeoQueryReady" + System.currentTimeMillis());
                bottomNavigationView.setSelectedItemId(R.id.actionMap);
            }

            @Override
            public void onGeoQueryError(DatabaseError databaseError) {
                showLog("onGeoQueryError" + databaseError.getMessage());
                bottomNavigationView.setSelectedItemId(R.id.actionMap);
            }
        });
    }

    @Override
    protected void findLastKnownLocation(LatLng defaultLatLng) {
        loadData();
    }

    private BottomNavigationView bottomNavigationView;

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    private void initPlacesRequest(int type) {
        String location = adInfo.latitude + "," + adInfo.longitude;
        googlePlaceCall = RetrofitConstants.getGooglePlaces(location, googlePlaceType[type]);
        startRequest(type);
    }

    private Call<GooglePlace> googlePlaceCall;

    private void startRequest(final int requestType) {
        showProgressDialog();
        googlePlaceCall.enqueue(new Callback<GooglePlace>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlace> call, @NonNull Response<GooglePlace> response) {
                closeProgressDialog();
                GooglePlace googlePlace = response.body();
                addPlaceMarker(googlePlace, requestType);
                showLog();
            }

            @Override
            public void onFailure(@NonNull Call<GooglePlace> call, @NonNull Throwable t) {
                if (!isCanceled)
                    closeProgressDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        isCanceled = false;
    }

    private boolean isCanceled;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (googlePlaceCall != null)
            googlePlaceCall.cancel();
        isCanceled = true;
    }

//    private long flatRent = 15000;

    private void addMarkerForSelectedFlat() {
        int resourceId = R.drawable.marker_purple_others;
        if (flatType.equals(DBConstants.keyFamily)) {
            resourceId = R.drawable.marker_blue_family;
        } else if (flatType.equals(DBConstants.keyMess)) {
            resourceId = R.drawable.marker_green_mess;
        } else if (flatType.equals(DBConstants.keySublet)) {
            resourceId = R.drawable.marker_merun_sublet;
        }

//        String markerValue = "৳ " + AppConstants.rentFormatter(flatRent);
//        if ((flatRent / 1000) > 0) {
//            markerValue = "৳ " + (flatRent / 1000) + "K";
//        }
//        Bitmap bitmap = AppConstants.writeOnDrawable(this, resourceId, markerValue);

        googleMap.addMarker(new MarkerOptions()
                .position(selectedLocation)
                .icon(BitmapDescriptorFactory.fromResource(resourceId)));
    }

    private void addPlaceMarker(GooglePlace googlePlace, int requestType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isDestroyed()) {
                return;
            }
        }

        if (isCanceled) {
            return;
        }

        if (googleMap == null || googlePlace == null) {
            return;
        }

        int resourceId = R.drawable.marker_school;
        if (requestType == 0) {//bank
            resourceId = R.drawable.marker_bank;
        } else if (requestType == 1) {//school
            resourceId = R.drawable.marker_school;
        } else if (requestType == 2) {//department_store
            resourceId = R.drawable.marker_dep_store;
        } else if (requestType == 3) {//bus_station
            resourceId = R.drawable.marker_bus_stand;
        }

        if (googlePlace.status.equalsIgnoreCase(getString(R.string.ok))) {
            for (GooglePlaceResult googlePlaceResult : googlePlace.results) {
                if (googlePlaceResult.geometry.has(DBConstants.location)) {
                    JsonObject jsonObject = googlePlaceResult.geometry.getAsJsonObject(DBConstants.location);
                    if (jsonObject.has(JsonUtils.lat) && jsonObject.has(JsonUtils.lng)) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(jsonObject.get(JsonUtils.lat).getAsDouble(), jsonObject.get(JsonUtils.lng).getAsDouble()))
                                .snippet(googlePlaceResult.vicinity)
                                .title(googlePlaceResult.name)
                                .icon(BitmapDescriptorFactory.fromResource(resourceId)));
                    }
                }
            }
        }
    }

    private void addMarkerForNearbyFlat() {
        int resourceId = R.drawable.marker_others_nearby;
        switch (flatType) {
            case DBConstants.keyFamily:
                resourceId = R.drawable.marker_family_nearby;
                break;
            case DBConstants.keyMess:
                resourceId = R.drawable.marker_mess_nearby;
                break;
            case DBConstants.keySublet:
                resourceId = R.drawable.marker_sublet_nearby;
                break;
        }

        for (String key : nearbySimilar.keySet()) {
            if (key.equals(adInfo.adId))
                continue;
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(nearbySimilar.get(key).latitude, nearbySimilar.get(key).longitude))
                    .snippet("tap here for see details about this flat")
                    .title("Please tap here")
                    .icon(BitmapDescriptorFactory.fromResource(resourceId)));
            marker.setTag(key);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.shareAction:
                shareAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setEmailAddress(boolean afterSuccessfulLogin) {

    }
}
