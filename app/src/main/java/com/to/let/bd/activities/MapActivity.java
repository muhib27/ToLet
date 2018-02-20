package com.to.let.bd.activities;

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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import com.to.let.bd.utils.MyAnalyticsUtil;
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

    private String flatType;
    private AdInfo adInfo;

    @Override
    protected void onCreate() {
        initBottomNavigation();
        initPlace();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
            flatType = bundle.getString(DBConstants.flatType);
        }

        if (flatType == null || flatType.isEmpty())
            flatType = DBConstants.keyMess;

        invalidateOptionsMenu();
        if (adInfo != null) {
            selectedCenterLatLng = new LatLng(adInfo.latitude, adInfo.longitude);
        }
    }

    private void initPlace() {
        findViewById(R.id.searchThisArea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCenterLatLng = googleMap.getCameraPosition().target;

                Bundle bundle = new Bundle();
                bundle.putString(MyAnalyticsUtil.keySearchType, MyAnalyticsUtil.searchTypeGoogleMap);
                bundle.putDouble(MyAnalyticsUtil.keySearchLat, selectedCenterLatLng.latitude);
                bundle.putDouble(MyAnalyticsUtil.keySearchLng, selectedCenterLatLng.longitude);
                myAnalyticsUtil.searchEvent(bundle);

                loadNearestData();
            }
        });

        PlaceAutocompleteFragment placeAutocomplete = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.placeAutocomplete);
        placeAutocomplete.setHint(getString(R.string.please_type_here_for_search));
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("BD")
                .build();
        placeAutocomplete.setFilter(typeFilter);
        placeAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
                selectedCenterLatLng = place.getLatLng();
                loadNearestData();

                Bundle bundle = new Bundle();
                bundle.putString(MyAnalyticsUtil.keySearchType, MyAnalyticsUtil.placePickerMapView);
                bundle.putDouble(MyAnalyticsUtil.keySearchLat, selectedCenterLatLng.latitude);
                bundle.putDouble(MyAnalyticsUtil.keySearchLng, selectedCenterLatLng.longitude);
                bundle.putCharSequence(MyAnalyticsUtil.keySearchName, place.getName());
                myAnalyticsUtil.searchEvent(bundle);
            }

            @Override
            public void onError(Status status) {
                showLog("An error occurred: " + status);
            }
        });
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

        this.googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                selectedCenterLatLng = MapActivity.this.googleMap.getCameraPosition().target;
            }
        });

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MapActivity.this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        });

        this.googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() == null) {
                    return;
                }

                if (marker.getTag() instanceof AdInfo) {
                    startAdDetailsActivity((AdInfo) marker.getTag());
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

    private LatLng selectedCenterLatLng;

    private void loadNearestData() {
        if (adInfo == null && selectedCenterLatLng == null)
            return;

        showProgressDialog();
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(selectedCenterLatLng.latitude, selectedCenterLatLng.longitude), DEFAULT_ZOOM));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DBConstants.geoFire + "/" + flatType);
        GeoFire geoFire = new GeoFire(ref);

        final double radius = 5.0;
        nearbySimilar.clear();
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(selectedCenterLatLng.latitude, selectedCenterLatLng.longitude), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation geoLocation) {
                nearbySimilar.put(key, geoLocation);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String s, GeoLocation geoLocation) {

            }

            @Override
            public void onGeoQueryReady() {
                bottomNavigationView.setSelectedItemId(R.id.actionMap);
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyGeoQuerySucceed, String.valueOf(nearbySimilar.size()));
                closeProgressDialog();
            }

            @Override
            public void onGeoQueryError(DatabaseError databaseError) {
                bottomNavigationView.setSelectedItemId(R.id.actionMap);
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyGeoQueryFailed, databaseError.getDetails());
            }
        });
    }

    @Override
    protected void findLastKnownLocation(LatLng defaultLatLng) {
        if (adInfo == null)
            selectedCenterLatLng = defaultLatLng;
        else
            selectedCenterLatLng = new LatLng(adInfo.latitude, adInfo.longitude);
        loadNearestData();
    }

    private BottomNavigationView bottomNavigationView;

    private void initBottomNavigation() {
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    private void initPlacesRequest(int type) {
        String location = selectedCenterLatLng.latitude + "," + selectedCenterLatLng.longitude;
        googlePlaceCall = RetrofitConstants.getGooglePlaces(location, googlePlaceType[type]);
        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMapFilterEvent, googlePlaceType[type]);
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

    private void addMarkerForSelectedFlat() {
        if (adInfo == null)
            return;
        else {
            if (adInfo.familyInfo != null && !flatType.equals(DBConstants.keyFamily)) {
                return;
            } else if (adInfo.messInfo != null && !flatType.equals(DBConstants.keyMess)) {
                return;
            } else if (adInfo.subletInfo != null && !flatType.equals(DBConstants.keySublet)) {
                return;
            } else if (adInfo.othersInfo != null && !flatType.equals(DBConstants.keyOthers)) {
                return;
            }
        }

        int resourceId = R.drawable.marker_purple_others;
        switch (flatType) {
            case DBConstants.keyFamily:
                resourceId = R.drawable.marker_blue_family;
                break;
            case DBConstants.keyMess:
                resourceId = R.drawable.marker_green_mess;
                break;
            case DBConstants.keySublet:
                resourceId = R.drawable.marker_merun_sublet;
                break;
        }

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(adInfo.latitude, adInfo.longitude))
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
            if (adInfo != null && key.equals(adInfo.adId))
                continue;
            final Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(nearbySimilar.get(key).latitude, nearbySimilar.get(key).longitude))
                    .title("!!!Please tap here!!!")
                    .snippet("Please tap here for see details")
                    .icon(BitmapDescriptorFactory.fromResource(resourceId)));

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference
                    .child(DBConstants.adList)
                    .child(flatType)
                    .child(key)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            AdInfo adInfo = dataSnapshot.getValue(AdInfo.class);
                            if (adInfo != null) {
                                marker.setTitle(AppConstants.mapMarkerTitle(MapActivity.this, adInfo));
                                marker.setTag(adInfo);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            marker.setTag(key);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem filterByFamily = menu.findItem(R.id.filterByFamily);
        final MenuItem filterByMess = menu.findItem(R.id.filterByMess);
        final MenuItem filterBySublet = menu.findItem(R.id.filterBySublet);
        final MenuItem filterByOthers = menu.findItem(R.id.filterByOthers);

        switch (flatType) {
            case DBConstants.keyFamily:
                filterByFamily.setChecked(true);
                break;
            case DBConstants.keyMess:
                filterByMess.setChecked(true);
                break;
            case DBConstants.keySublet:
                filterBySublet.setChecked(true);
                break;
            default:
                filterByOthers.setChecked(true);

        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.filterByFamily:
                flatType = DBConstants.keyFamily;
                invalidateOptionsMenu();
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMapFilterEvent, flatType);
                loadNearestData();
                return true;
            case R.id.filterByMess:
                flatType = DBConstants.keyMess;
                invalidateOptionsMenu();
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMapFilterEvent, flatType);
                loadNearestData();
                return true;
            case R.id.filterBySublet:
                flatType = DBConstants.keySublet;
                invalidateOptionsMenu();
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMapFilterEvent, flatType);
                loadNearestData();
                return true;
            case R.id.filterByOthers:
                flatType = DBConstants.keyOthers;
                invalidateOptionsMenu();
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMapFilterEvent, flatType);
                loadNearestData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setEmailAddress(boolean afterSuccessfulLogin) {

    }
}
