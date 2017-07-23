package com.to.let.bd.activities;

import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.common.WorkaroundMapFragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewPostActivity extends BaseActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnMapClickListener,
        AdapterView.OnItemSelectedListener,View.OnClickListener {

    private static final String TAG = NewPostActivity.class.getSimpleName();
    private GoogleMap googleMap;
    private CameraPosition mCameraPosition;
    private EditText totalSpace;
    private Spinner bedRoom, balcony, bathRoom, whichFloor, whichFacing;
    ArrayAdapter<String> bedRoomAdapter, balconyAdapter, bathRoomAdapter, floorAdapter, facingAdapter;

    private DatePickerDialog rentFromDialog;
    private SimpleDateFormat dateFormatter;
    Calendar newCalendar = Calendar.getInstance();
    private TextView setDate;
    private RadioGroup drawingDining;
    private RadioButton drawingDiningYes, drawingDiningNo, checkedRadioButton;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;


    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private LatLng mDefaultLatLng = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
//    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private void retrieveSavedInstanceState(Bundle savedInstanceState) {
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        retrieveSavedInstanceState(savedInstanceState);

        setContentView(R.layout.activity_new_post);
        initialize();
        addItemsOnSpinner();
        setDateTimeField();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mapScrollView = (ScrollView) findViewById(R.id.mapScrollView);
        addressDetails = (TextView) findViewById(R.id.addressDetails);

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void initialize(){

        drawingDining = (RadioGroup)findViewById(R.id.drawingDining);
        drawingDiningYes = (RadioButton)findViewById(R.id.drawingDiningYes);
        drawingDiningNo = (RadioButton)findViewById(R.id.drawingDiningNo);
        totalSpace = (EditText)findViewById(R.id.total_space);
        totalSpace.setText("1000");
        bedRoom = (Spinner)findViewById(R.id.bedRoom);
        balcony = (Spinner)findViewById(R.id.balcony);
        bathRoom = (Spinner)findViewById(R.id.bathRoom);
        whichFacing = (Spinner)findViewById(R.id.whichFacing);
        whichFloor = (Spinner)findViewById(R.id.whichFloor);

        setDate = (TextView)findViewById(R.id.setDate);

        bedRoom.setOnItemSelectedListener(this);
        balcony.setOnItemSelectedListener(this);
        bathRoom.setOnItemSelectedListener(this);
        whichFloor.setOnItemSelectedListener(this);
        whichFacing.setOnItemSelectedListener(this);

        checkedRadioButton = (RadioButton)drawingDining.findViewById(drawingDining.getCheckedRadioButtonId());
        drawingDining.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked)
                {
                    Toast.makeText(getApplicationContext(), "Checked:" + checkedRadioButton.getText(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private TextView addressDetails;
    private ScrollView mapScrollView;

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (googleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        showLog("Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        showLog("Play services connection suspended");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
//        if (this.googleMap == null) {
//            this.googleMap = ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//
//
//        }

        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        mapScrollView = (ScrollView) findViewById(R.id.mapScrollView); //parent scrollview in xml, give your scrollview id value

        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        mapScrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });

        requestLocationPermission();

//        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//
//            }
//        });
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.googleMap.setInfoWindowAdapter(this);

        // Turn on the My Location layer and the related control on the map.
//        enableGoogleMapMyLocation();

        // Get the current location of the device and set the position of the map.
//        gotoDeviceLocation();

        this.googleMap.setOnMapLongClickListener(this);
        this.googleMap.setOnMapClickListener(this);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableGoogleMapMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Inflate the layouts for the info window, title and snippet.
        View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                (FrameLayout) findViewById(R.id.map), false);

        TextView title = ((TextView) infoWindow.findViewById(R.id.title));
        title.setText(marker.getTitle());

        TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
        snippet.setText(marker.getSnippet());

        return infoWindow;
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void gotoDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (!(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            mDefaultLatLng = new LatLng(mCameraPosition.target.latitude, mCameraPosition.target.longitude);
        } else if (mLastKnownLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLatLng(mLastKnownLocation), DEFAULT_ZOOM));
            mDefaultLatLng = getLatLng(mLastKnownLocation);
        } else {
            showLog("Current location is null. Using defaults.");
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLatLng, DEFAULT_ZOOM));
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        String fullAddress = getLocationBestApproximateResult(findCurrentLocationDetails(mDefaultLatLng.latitude, mDefaultLatLng.longitude), mDefaultLatLng);
        addressDetails.setText(fullAddress);
    }

    private LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        showLog("requestCode: " + requestCode);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableGoogleMapMyLocation();
                }
            default:
                break;
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void enableGoogleMapMyLocation() {
        if (googleMap == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        } else {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        gotoDeviceLocation();
    }

    private final int maxAddressResult = 5;

    private List<Address> findCurrentLocationDetails(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addressList = new ArrayList<>();
        addressList.clear();

        geocoder = new Geocoder(this, Locale.ENGLISH);

//        latitude = 23.7929279;
//        longitude = 90.4035839;

        try {
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            addressList = geocoder.getFromLocation(latitude, longitude, maxAddressResult);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressList;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        googleMap.clear();
        String fullAddress = getLocationBestApproximateResult(findCurrentLocationDetails(latLng.latitude, latLng.longitude), latLng);
        addressDetails.setText(fullAddress);
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .snippet(fullAddress)
                .title("You tap here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

//        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    }

    @Override
    public void onMapClick(LatLng latLng) {
        this.googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                googleMap.clear();
            }
        });
    }

    private String getLocationBestApproximateResult(List<Address> addressList, LatLng latLng) {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }

        String result = "";
        for (Address address : addressList) {
            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            if (address.getAddressLine(0) != null) {
                result = address.getAddressLine(0);
            }
            if (address.getLocality() != null && !result.contains(address.getLocality())) {
                result = result + "," + address.getLocality();
            }

            if (address.getCountryName() != null && !result.contains(address.getCountryName())) {
                result = result + "," + address.getCountryName();
            }

            if (address.getAdminArea() != null && !result.contains(address.getAdminArea())) {
                result = result + "," + address.getAdminArea();
            }
//            String postalCode = address.getPostalCode();
//            // Only if available else return NULL
//            String knownName = address.getFeatureName();
        }

        return result;
    }

    public void addItemsOnSpinner() {
        String[] totalBedRoom = {"1","2","3","4","5","6","7"};
        String[] floor = {"Ground","1st", "2nd", "3rd", "4th","5th", "6th", "7th", "8th","9th","10th" };
        String[] facing = {"South","East", "West", "North"};
        //String[] totalBalcony = {"1","2","3","4","5","6","7"};

        // Creating adapter for spinner
        bedRoomAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, totalBedRoom);
        floorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, floor);
        facingAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, facing);



        // Drop down layout style - list view with radio button
        bedRoomAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        floorAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        facingAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

        // attaching data adapter to spinner
        bedRoom.setAdapter(bedRoomAdapter);
        balcony.setAdapter(bedRoomAdapter);
        bathRoom.setAdapter(bedRoomAdapter);
        whichFloor.setAdapter(floorAdapter);
        whichFacing.setAdapter(facingAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setDate: {
                rentFromDialog.show();
            }
            break;
        }
    }

    /**
     * Demonstrates customizing the info window and/or its contents.
     */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
//            if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_window) {
//                // This means that getInfoContents will be called.
//                return null;
//            }
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
//            if (mOptions.getCheckedRadioButtonId() != R.id.custom_info_contents) {
//                // This means that the default info contents will be used.
//                return null;
//            }
            render(marker, mContents);
            return mContents;
        }

        private void render(Marker marker, View view) {
            int badge = R.drawable.ic_menu_send;
//            // Use the equals() method on a Marker to check for equals.  Do not use ==.
//            if (marker.equals(mBrisbane)) {
//                badge = R.drawable.badge_qld;
//            } else if (marker.equals(mAdelaide)) {
//                badge = R.drawable.badge_sa;
//            } else if (marker.equals(mSydney)) {
//                badge = R.drawable.badge_nsw;
//            } else if (marker.equals(mMelbourne)) {
//                badge = R.drawable.badge_victoria;
//            } else if (marker.equals(mPerth)) {
//                badge = R.drawable.badge_wa;
//            } else {
//                // Passing 0 to setImageResource will clear the image view.
//                badge = 0;
//            }
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }
    int fromYear, fromMonth, fromDay;
    private void setDateTimeField() {
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        setDate.setOnClickListener(this);
        final Calendar newFromDate = Calendar.getInstance();
        rentFromDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {


                fromYear = year;
                fromMonth = monthOfYear;
                fromDay = dayOfMonth;
                newFromDate.set(year, monthOfYear, dayOfMonth);
                if (newFromDate.getTimeInMillis() < newCalendar.getTimeInMillis())
                    setDate.setText(dateFormatter.format(newCalendar.getTime()));
                else
                    setDate.setText(dateFormatter.format(newFromDate.getTime()));

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        rentFromDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

}
