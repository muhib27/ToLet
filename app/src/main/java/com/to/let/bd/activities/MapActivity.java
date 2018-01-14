package com.to.let.bd.activities;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends BaseMapActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

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
    }

    private static final String TAG = MapActivity.class.getSimpleName();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_map;
    }

    public static final String[] googlePlaceType = {"bank", "school", "department_store", "bus_station"};

    private double latitude, longitude;
    private String flatType;
    private AdInfo adInfo;
    private LatLng selectedLocation;

    @Override
    protected void onCreate() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
        if (adInfo == null)
            return;

        latitude = adInfo.latitude;
        longitude = adInfo.longitude;
        flatType = adInfo.flatType;
        flatRent = adInfo.flatRent;

        selectedLocation = new LatLng(latitude, longitude);
        initBottomNavigation();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
        GeoFire geoFire = new GeoFire(ref);

        geoFire.setLocation("firebase-hq", new GeoLocation(37.7853889, -122.4056973), new GeoFire.CompletionListener() {

            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    showLog("There was an error saving the location to GeoFire: " + error);
                } else {
                    showLog("Location saved on server successfully!");
                }
            }
        });

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(37.7832, -122.4056), 0.6);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation geoLocation) {

            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation geoLocation) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation geoLocation) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError databaseError) {

            }
        });
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String s, GeoLocation geoLocation) {

            }

            @Override
            public void onKeyExited(String s) {

            }

            @Override
            public void onKeyMoved(String s, GeoLocation geoLocation) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.map);
    }

    private GoogleMap googleMap;

    @Override
    protected void onMapReady2(GoogleMap googleMap) {
        this.googleMap = googleMap;
//        onNavigationItemSelected(bottomNavigationView.getMenu().findItem(R.id.actionSchool));
        bottomNavigationView.setSelectedItemId(R.id.actionSchool);

        this.googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                LatLng center = MapActivity.this.googleMap.getCameraPosition().target;
                showLog();
            }
        });
    }

    @Override
    protected void findLastKnownLocation(LatLng defaultLatLng) {

    }

    private BottomNavigationView bottomNavigationView;

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    private void initPlacesRequest(int type) {
        String location = latitude + "," + longitude;
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

    private long flatRent = 15000;

    private void addMarkerForSelectedFlat() {
        int resourceId = R.drawable.marker_purple_others;
        if (flatType.equalsIgnoreCase(getString(R.string.family))) {
            resourceId = R.drawable.marker_blue_family;
        } else if (flatType.equalsIgnoreCase(getString(R.string.mess))) {
            resourceId = R.drawable.marker_green_mess;
        } else if (flatType.equalsIgnoreCase(getString(R.string.sublet))) {
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

    @Override
    protected void onLoadLocationDetails(String fullAddress) {

    }
}
