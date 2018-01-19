package com.to.let.bd.activities;

import android.os.Bundle;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseMapActivity;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;

public class TestActivity extends BaseMapActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_test;
    }

    @Override
    protected void onCreate() {
        initPlace();
        getAdInfo();
    }

    private AdInfo adInfo;

    private void getAdInfo() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
    }

    private void initPlace() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.placeAutocomplete);

        autocompleteFragment.setHint(getString(R.string.please_type_here_for_search));
//        autocompleteFragment.setBoundsBias(new LatLngBounds(
//                new LatLng(23.668679, 90.502397),
//                new LatLng(23.898677, 90.326661)));
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("BD")
                .build();

        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                showLog("Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                showLog("An error occurred: " + status);
            }
        });
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.post_your_ad);
    }

    @Override
    protected void setEmailAddress(boolean afterSuccessfulLogin) {

    }

    @Override
    protected void onMapReady2(GoogleMap googleMap) {

    }

    @Override
    protected void findLastKnownLocation(LatLng defaultLatLng) {

    }
}
