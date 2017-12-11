package com.to.let.bd.activities;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseMapActivity;
import com.to.let.bd.model.google_place.GooglePlace;
import com.to.let.bd.model.google_place.GooglePlaceResult;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.JsonUtils;
import com.to.let.bd.utils.retrofit.RetrofitConstants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends BaseMapActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_bank:
                    return true;
                case R.id.navigation_bazaar:
                    return true;
                case R.id.navigation_map:
                    return true;
                case R.id.navigation_bus_stand:
                    return true;
                case R.id.navigation_school:
                    return true;
            }
            return false;
        }
    };

    private static final String TAG = MapActivity.class.getSimpleName();
//    private GoogleMap googleMap;
//    private CameraPosition mCameraPosition;
//
//    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
//    private GoogleApiClient mGoogleApiClient;
//
//    // A default location (Sydney, Australia) and default zoom to use when location permission is
//    // not granted.
//    private LatLng mDefaultLatLng;
//    private static final int DEFAULT_ZOOM = 15;
//    private static final int PERMISSIONS_REQUEST_CODE = 1;
////    private boolean mLocationPermissionGranted;
//
//    // The geographical location where the device is currently located. That is, the last-known
//    // location retrieved by the Fused Location Provider.
//    private Location mLastKnownLocation;
//
//    // Keys for storing activity state.
//    private static final String KEY_CAMERA_POSITION = "camera_position";
//    private static final String KEY_LOCATION = "location";
//
//    private void retrieveSavedInstanceState(Bundle savedInstanceState) {
//        // Retrieve location and camera position from saved instance state.
//        if (savedInstanceState != null) {
//            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
//        }
//    }

    public static final String[] googlePlaceType = {"school", "department_store", "bank", "bus_station"};
    private int type = 0;
    private double latitude, longitude;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        retrieveSavedInstanceState(savedInstanceState);
//
//        // Retrieve the content view that renders the map.
//        setContentView(R.layout.activity_map);
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowTitleEnabled(true);
//            actionBar.setTitle(R.string.map);
//        }
//
//        // Build the Play services client for use by the Fused Location Provider and the Places API.
//        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */,
//                        this /* OnConnectionFailedListener */)
//                .addConnectionCallbacks(this)
//                .addApi(LocationServices.API)
//                .build();
//        mGoogleApiClient.connect();
//        initBottomNavigation();
//    }
//
//    /**
//     * Saves the state of the map when the activity is paused.
//     */
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        if (googleMap != null) {
//            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
//            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
//            super.onSaveInstanceState(outState);
//        }
//    }
//
//    /**
//     * Builds the map when the Google Play services client is successfully connected.
//     */
//    @Override
//    public void onConnected(Bundle connectionHint) {
//        // Build the map.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }
//
//    /**
//     * Handles failure to connect to the Google Play services client.
//     */
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult result) {
//        // Refer to the reference doc for ConnectionResult to see what error codes might
//        // be returned in onConnectionFailed.
//        showLog("Play services connection failed: ConnectionResult.getErrorCode() = "
//                + result.getErrorCode());
//    }
//
//    /**
//     * Handles suspension of the connection to the Google Play services client.
//     */
//    @Override
//    public void onConnectionSuspended(int cause) {
//        showLog("Play services connection suspended");
//    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate() {
        type = getIntent().getIntExtra(AppConstants.keyType, 0);
        latitude = getIntent().getDoubleExtra(DBConstants.latitude, AppConstants.defaultLatitude);
        longitude = getIntent().getDoubleExtra(DBConstants.longitude, AppConstants.defaultLongitude);

//        mDefaultLatLng = new LatLng(latitude, longitude);
        initBottomNavigation();
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.map);
    }

    @Override
    protected void setEmailAddress(boolean afterSuccessfulLogin) {

    }

    @Override
    protected void onLoadLocationDetails(String fullAddress) {

    }

    private GoogleMap googleMap;

    @Override
    protected void onMapReady2(GoogleMap googleMap) {
        this.googleMap = googleMap;

        initPlacesRequest();
    }

    private BottomNavigationView bottomNavigationView;

    private void initBottomNavigation() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.post(new Runnable() {
            @Override
            public void run() {
                bottomNavigationView.setSelectedItemId(R.id.navigation_map);
            }
        });
    }

//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        this.googleMap = googleMap;
//        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
//
//        initPlacesRequest();
//
//        requestLocationPermission();
////        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
////            @Override
////            public void onMapLoaded() {
////
////            }
////        });
//        // Use a custom info window adapter to handle multiple lines of text in the
//        // info window contents.
//        this.googleMap.setInfoWindowAdapter(this);
//
//        // Turn on the My Location layer and the related control on the map.
////        enableGoogleMapMyLocation();
//
//        // Get the current location of the device and set the position of the map.
////        gotoDeviceLocation();
//
//        this.googleMap.setOnMapLongClickListener(this);
//        this.googleMap.setOnMapClickListener(this);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.connect();
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.disconnect();
//        }
//    }

//    private void requestLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            enableGoogleMapMyLocation();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_CODE);
//        }
//    }

//    @Override
//    public View getInfoWindow(Marker marker) {
//        return null;
//    }
//
//    @Override
//    public View getInfoContents(Marker marker) {
//        // Inflate the layouts for the info window, roomFaceTitle and snippet.
//        View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
//                (FrameLayout) findViewById(R.id.map), false);
//
//        TextView title = ((TextView) infoWindow.findViewById(R.id.title));
//        title.setText(marker.getTitle());
//
//        TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
//        snippet.setText(marker.getSnippet());
//
//        return infoWindow;
//    }
//
//    /**
//     * Gets the current location of the device, and positions the map's camera.
//     */
//    private void gotoDeviceLocation() {
//        /*
//         * Get the best and most recent location of the device, which may be null in rare
//         * cases when a location is not available.
//         */
//        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        }
//
//        // Set the map's camera position to the current location of the device.
//        if (mCameraPosition != null) {
//            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
//        } else if (mLastKnownLocation != null) {
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
//        } else {
//            showLog("Current location is null. Using defaults.");
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLatLng, DEFAULT_ZOOM));
//            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
//        }
//    }
//
//    private LatLng getLatLng(Location location) {
//        return new LatLng(location.getLatitude(), location.getLongitude());
//    }
//
//    /**
//     * Handles the result of the request for location permissions.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        showLog("requestCode: " + requestCode);
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_CODE:
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    enableGoogleMapMyLocation();
//                }
//            default:
//                break;
//        }
//    }
//
    private void initPlacesRequest() {
        String location = latitude + "," + longitude;
        googlePlaceCall = RetrofitConstants.getGooglePlaces(location, googlePlaceType[type]);
        startRequest();
    }
//
//    /**
//     * Updates the map's UI settings based on whether the user has granted location permission.
//     */
//    private void enableGoogleMapMyLocation() {
//        if (googleMap == null) {
//            return;
//        }
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            googleMap.setMyLocationEnabled(false);
//            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
//            mLastKnownLocation = null;
//        } else {
//            googleMap.setMyLocationEnabled(true);
//            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//        }
//
//        gotoDeviceLocation();
//    }
//
//    private final int maxAddressResult = 3;
//
//    private List<Address> findCurrentLocationDetails(double latitude, double longitude) {
//        Geocoder geocoder;
//        List<Address> addressList = new ArrayList<>();
//        addressList.clear();
//
//        geocoder = new Geocoder(this, Locale.ENGLISH);
//
////        latitude = 23.7929279;
////        longitude = 90.4035839;
//
//        try {
//            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//            addressList = geocoder.getFromLocation(latitude, longitude, maxAddressResult);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return addressList;
//    }
//
//    @Override
//    public void onMapLongClick(LatLng latLng) {
////        googleMap.clear();
////        getLocationBestApproximateResult(findCurrentLocationDetails(latLng.latitude, latLng.longitude));
////        googleMap.addMarker(new MarkerOptions()
////                .position(latLng)
////                .snippet("your full address")
////                .title("You are here")
////                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
////
////        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
//    }
//
//    @Override
//    public void onMapClick(LatLng latLng) {
////        googleMap.clear();
//    }
//
//    private String getLocationBestApproximateResult(List<Address> addressList) {
//        if (addressList == null || addressList.isEmpty()) {
//            return null;
//        }
//
//        ArrayList<TestClass> testList = new ArrayList<>();
//        testList.clear();
//
//        String result = "";
//        String country = "";
//        for (Address address : addressList) {
//
//            int maxAddressLine = address.getMaxAddressLineIndex();
//            String fullAddressLine = "";
//            for (int i = 0; i < maxAddressLine; i++) {
//                if (i == 0) {
//                    fullAddressLine = address.getAddressLine(i);
//                } else {
//                    fullAddressLine = fullAddressLine + ", " + address.getAddressLine(i);
//                }
//            }
//            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String addressLine = address.getAddressLine(0);
//            String city = address.getLocality();
//            String state = address.getAdminArea();
////            String country = address.getCountryName();
//            String postalCode = address.getPostalCode();
//            // Only if available else return NULL
//            String knownName = address.getFeatureName();
//        }
//
//        return result;
//    }
//
//    private class TestClass {
//        private String name = "";
//        private int count = 0;
//    }
//
//    /**
//     * Demonstrates customizing the info window and/or its contents.
//     */
//    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
//
//        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
//        // "roomFaceTitle" and "snippet".
//        private final View mWindow;
//
//        private final View mContents;
//
//        CustomInfoWindowAdapter() {
//            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
//            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
//        }
//
//        @Override
//        public View getInfoWindow(Marker marker) {
////            if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_window) {
////                // This means that getInfoContents will be called.
////                return null;
////            }
//            render(marker, mWindow);
//            return mWindow;
//        }
//
//        @Override
//        public View getInfoContents(Marker marker) {
////            if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_contents) {
////                // This means that the default info contents will be used.
////                return null;
////            }
//            render(marker, mContents);
//            return mContents;
//        }
//
//        private void render(Marker marker, View view) {
//            int badge = R.drawable.ic_menu_send;
////            // Use the equals() method on a Marker to check for equals.  Do not use ==.
////            if (marker.equals(mBrisbane)) {
////                badge = R.drawable.badge_qld;
////            } else if (marker.equals(mAdelaide)) {
////                badge = R.drawable.badge_sa;
////            } else if (marker.equals(mSydney)) {
////                badge = R.drawable.badge_nsw;
////            } else if (marker.equals(mMelbourne)) {
////                badge = R.drawable.badge_victoria;
////            } else if (marker.equals(mPerth)) {
////                badge = R.drawable.badge_wa;
////            } else {
////                // Passing 0 to setImageResource will clear the image view.
////                badge = 0;
////            }
//            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
//
//            String title = marker.getTitle();
//            TextView titleUi = ((TextView) view.findViewById(R.id.title));
//            if (title != null) {
//                // Spannable string allows us to edit the formatting of the text.
//                SpannableString titleText = new SpannableString(title);
//                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
//                titleUi.setText(titleText);
//            } else {
//                titleUi.setText("");
//            }
//
//            String snippet = marker.getSnippet();
//            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
//            if (snippet != null && snippet.length() > 12) {
//                SpannableString snippetText = new SpannableString(snippet);
//                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
//                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
//                snippetUi.setText(snippetText);
//            } else {
//                snippetUi.setText("");
//            }
//        }
//    }

    private Call<GooglePlace> googlePlaceCall;

    private void startRequest() {
        googlePlaceCall.enqueue(new Callback<GooglePlace>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlace> call, @NonNull Response<GooglePlace> response) {
                GooglePlace googlePlace = response.body();
                addPlaceMarker(googlePlace);
                showLog();
            }

            @Override
            public void onFailure(@NonNull Call<GooglePlace> call, @NonNull Throwable t) {

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

        isCanceled = true;
    }

    private void addPlaceMarker(GooglePlace googlePlace) {
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

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        if (googlePlace.getStatus().equals("OK") || googlePlace.getStatus().equalsIgnoreCase("OK")) {
            for (GooglePlaceResult googlePlaceResult : googlePlace.getResults()) {
                if (googlePlaceResult.getGeometry().has(DBConstants.location)) {
                    JsonObject jsonObject = googlePlaceResult.getGeometry().getAsJsonObject(DBConstants.location);
                    if (jsonObject.has(JsonUtils.lat) && jsonObject.has(JsonUtils.lng)) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(jsonObject.get(JsonUtils.lat).getAsDouble(), jsonObject.get(JsonUtils.lng).getAsDouble()))
                                .snippet(googlePlaceResult.getVicinity())
                                .title(googlePlaceResult.getName())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_blue)));
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
}
