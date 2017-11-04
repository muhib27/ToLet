package com.to.let.bd.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseImageUploadActivity;
import com.to.let.bd.common.WorkaroundMapFragment;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.AppSharedPrefs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NewAdActivity extends BaseImageUploadActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnMapClickListener,
        AdapterView.OnItemSelectedListener, View.OnFocusChangeListener, View.OnClickListener {

    private static final String TAG = NewAdActivity.class.getSimpleName();
    private GoogleMap googleMap;
    private CameraPosition mCameraPosition;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private LatLng mDefaultLatLng = new LatLng(23.8103, 90.4125);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveSavedInstanceState(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        initFirebase();
        initView();
        addRoomFaceType();
        setRentDate(getDefaultRentMonth());
        defaultCheck();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    private void retrieveSavedInstanceState(Bundle savedInstanceState) {
        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
    }

    private Button submitBtn;
    private TextView addressDetails;
    private ScrollView mapScrollView;
    private EditText emailAddress, mobileNumber;
    private LinearLayout flatAdditionalInfoLay;
    private EditText totalSpace, whichFloor, houseInfo, totalRent, totalUtility;
    private RadioGroup drawingDining, rentType, utilityBill;
    private TextInputLayout totalUtilityTIL;
    private TextView utilityBdt;

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.post_your_ad);
        }

        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(this);

        mapScrollView = (ScrollView) findViewById(R.id.mapScrollView);
        addressDetails = (TextView) findViewById(R.id.addressDetails);

        emailAddress = findViewById(R.id.emailAddress);
        mobileNumber = findViewById(R.id.mobileNumber);
        mobileNumber.setText(AppSharedPrefs.getMobileNumber());

        mobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeCallbacks(mobileNumberValidation);
                handler.postDelayed(mobileNumberValidation, AppConstants.textWatcherDelay);
            }
        });

        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                emailAddress.setOnFocusChangeListener(this);
            } else {
                String email = firebaseUser.getEmail();
                if (email == null || email.isEmpty()) {
                    emailAddress.setOnFocusChangeListener(this);
                } else {
                    emailAddress.setText(email);
                    emailAddress.setEnabled(false);
                }
            }
        }

        rentType = findViewById(R.id.rentType);
        drawingDining = findViewById(R.id.drawingDining);
        utilityBill = findViewById(R.id.utilityBill);

        roomNumberLay = findViewById(R.id.roomNumberLay);

//        bedRoom =  findViewById(R.id.bedRoom);
//        balcony =  findViewById(R.id.balcony);
//        bathRoom =  findViewById(R.id.bathRoom);
//        whichFacing =  findViewById(R.id.whichFacing);

        flatAdditionalInfoLay = findViewById(R.id.flatAdditionalInfoLay);
        houseInfo = findViewById(R.id.houseInfo);
        whichFloor = findViewById(R.id.whichFloor);
        totalSpace = findViewById(R.id.totalSpace);
        totalRent = findViewById(R.id.totalRent);

        totalUtilityTIL = findViewById(R.id.totalUtilityTIL);
        totalUtility = findViewById(R.id.totalUtility);
        utilityBdt = findViewById(R.id.utilityBdt);

        totalUtility.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (!(emailAddress.getText() == null || emailAddress.getText().toString().isEmpty()
                        || emailAddress.getText().toString().trim().isEmpty())) {
                    mobileNumber.requestFocus();
                    return true;
                }
                return false;
            }
        });

        waterCB = (CheckBox) findViewById(R.id.waterCB);
        gasCB = (CheckBox) findViewById(R.id.gasCB);
        electricityCB = (CheckBox) findViewById(R.id.electricityCB);
        liftCB = (CheckBox) findViewById(R.id.liftCB);
        generatorCB = (CheckBox) findViewById(R.id.generatorCB);
        securityGuardCB = (CheckBox) findViewById(R.id.securityGuardCB);

        rentDate = findViewById(R.id.rentDate);
        rentDate.setOnClickListener(this);
    }

    private CheckBox waterCB, gasCB, electricityCB, liftCB, generatorCB, securityGuardCB;
    private TextView rentDate;
    private LinearLayout roomNumberLay;

    private Handler handler = new Handler();
    private Runnable mobileNumberValidation = new Runnable() {
        @Override
        public void run() {
            mobileNumber.setError(null);
            AppConstants.isMobileNumberValid(NewAdActivity.this, mobileNumber);
        }
    };

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b)
            googleSignOut();
    }

    private void reloadFirebaseUser() {
        firebaseUser.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                closeProgressDialog();
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    String email = firebaseUser.getEmail();
                    emailAddress.setText(email);
                    emailAddress.setEnabled(false);
                }
            }
        });
    }

    // Google login
    private void googleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, AppConstants.GOOGLE_SIGN_IN);
    }

    // Google sign out
    private void googleSignOut() {
        if (mGoogleApiClient.isConnected())
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            googleLogin();
                        }
                    });
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == AppConstants.GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.getStatus().isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                showToast(getString(R.string.google_login_failed));
            }
        }
    }
    // [END onActivityResult]

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseLoginWithGoogle(credential);
    }

    private void firebaseLoginWithGoogle(AuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLog("signInWithCredential:onComplete:" + task.isSuccessful());
                        closeProgressDialog();

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        //showLog("task Result: " + task.getResult().toString());
                        if (!task.isSuccessful()) {
                            String message = task.getException().getMessage();
                            showToast(message);
                        } else {
                            setEmailAddress();
                        }
                    }
                });
    }

    private void setEmailAddress() {
        emailAddress.setText(firebaseUser.getEmail());
        emailAddress.setEnabled(false);
        validateInputtedData();
        updateUserInfo();
    }

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser firebaseUser;

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    showLog("onAuthStateChanged:signed_in:" + firebaseUser.getUid());
                } else {
                    showLog("onAuthStateChanged:signed_out");
                }
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

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
        // Inflate the layouts for the info window, roomFaceTitle and snippet.
        View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                (FrameLayout) findViewById(R.id.map), false);

        TextView title = infoWindow.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = infoWindow.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());

        return infoWindow;
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     * String fullAddress
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

        String fullAddress = getLocationBestApproximateResult(findSelectedLocationDetails(mDefaultLatLng.latitude, mDefaultLatLng.longitude), mDefaultLatLng);
        addressDetails.setText(fullAddress);

        addMarker(mDefaultLatLng, fullAddress);
    }

    private Marker selectedLocation;

    private void addMarker(LatLng latLng, String details) {
        googleMap.clear();
        selectedLocation = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .snippet(details)
                .title(getString(R.string.your_selected_location))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        selectedLocation.setTag(0);
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

    private final int maxAddressResult = 3;
    private double rentLatitude, rentLongitude;
    private String countryName;
    private String division;

    private List<Address> findSelectedLocationDetails(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addressList = new ArrayList<>();
        addressList.clear();

        geocoder = new Geocoder(this, Locale.ENGLISH);
        rentLatitude = latitude;
        rentLongitude = longitude;

        try {
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            addressList = geocoder.getFromLocation(latitude, longitude, maxAddressResult);
            //countryName = addressList.g

        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressList;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        final String fullAddress = getLocationBestApproximateResult(findSelectedLocationDetails(latLng.latitude, latLng.longitude), latLng);
        addressDetails.setText(fullAddress);
        addMarker(latLng, fullAddress);
//        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    }

    @Override
    public void onMapClick(LatLng latLng) {
        googleMap.clear();
        selectedLocation = null;
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
                division = address.getLocality();
            }

            if (address.getCountryName() != null && !result.contains(address.getCountryName())) {
                result = result + "," + address.getCountryName();
                countryName = address.getCountryName();
            }

            if (address.getAdminArea() != null && !result.contains(address.getAdminArea())) {
                result = result + "," + address.getAdminArea();
            }

            if (address.getFeatureName() != null && !result.contains(address.getFeatureName())) {
                result = result + "," + address.getAdminArea();
            }
        }

        return result;
    }

    private final String[] roomTypes = {"Bedroom", "Bathroom", "Balcony"};
    private final int[] roomArray = {0, 1, 2, 3, 4, 5};

    public void addRoomNumberView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < roomTypes.length; i++) {
            final View inflatedView = inflater.inflate(R.layout.row_perticular_view, roomNumberLay, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            inflatedView.setLayoutParams(layoutParams);
            final int pos = i;
            inflatedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showRoomNumberPopupMenu(inflatedView, roomTypes[pos]);
                }
            });
            int defaultSelection;
            if (i == 2) {
                defaultSelection = 1;
                updatePickerView(inflatedView, roomTypes[i], (roomArray[1] + " " + roomTypes[i]));
            } else {
                defaultSelection = 2;
                updatePickerView(inflatedView, roomTypes[i], (roomArray[defaultSelection] + " " + roomTypes[i] + "'s"));
            }

            familyRoom[i] = defaultSelection;
            roomNumberLay.addView(inflatedView);
        }
//        String[] totalBedRoom = {"1", "2", "3", "4", "5", "6", "7"};
////        String[] floor = {"Ground", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th"};
//        //String[] totalBalcony = {"1","2","3","4","5","6","7"};
//
//        ArrayAdapter<String> bedRoomAdapter, balconyAdapter, bathRoomAdapter, facingAdapter;
//        // Creating adapter for spinner
//        bedRoomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, totalBedRoom);
////        floorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, floor);
//        facingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, facing);
//
//
//        // Drop down layout style - list view with radio button
//        bedRoomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
////        floorAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
//        facingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // attaching data adapter to spinner
//        bedRoom.setAdapter(bedRoomAdapter);
//        balcony.setAdapter(bedRoomAdapter);
//        bathRoom.setAdapter(bedRoomAdapter);
//        //whichFloor.setAdapter(floorAdapter);
//        whichFacing.setAdapter(facingAdapter);
    }

    private void showRoomNumberPopupMenu(final View view, final String roomType) {
        PopupMenu popup = new PopupMenu(this, view);

        for (int room : roomArray) {
            String s;
            if (room <= 1) {
                if (room == 0 && roomType.equals(roomTypes[2])) {
                    s = "No " + roomType;
                } else {
                    if (room == 0) {
                        continue;
                    }
                    s = room + " " + roomType;
                }
            } else {
                s = room + " " + roomType + "'s";
            }
            popup.getMenu().add(s);
        }

        //popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String subtitle = String.valueOf(item.getTitle());
                String title = (subtitle.split(" "))[1].replace("'s", "");

                int roomNumber;
                if ((subtitle.split(" "))[0].equalsIgnoreCase("no")) {
                    roomNumber = 0;
                } else {
                    roomNumber = Integer.parseInt((subtitle.split(" "))[0]);
                }

                if (title.equalsIgnoreCase(roomTypes[0])) {
                    familyRoom[0] = roomNumber;
                } else if (title.equalsIgnoreCase(roomTypes[1])) {
                    familyRoom[1] = roomNumber;
                } else if (title.equalsIgnoreCase(roomTypes[2])) {
                    familyRoom[2] = roomNumber;
                }
                updatePickerView(view, title, subtitle);
                setCalculatedRent();
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    private void updatePickerView(View v, String title, String subTitle) {
        if (v instanceof ViewGroup) {
            ((TextView) v.findViewById(R.id.title)).setText(title);
            ((TextView) v.findViewById(R.id.subTitle)).setText(subTitle);
        }
    }

    private void addRoomFaceType() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View inflatedView = inflater.inflate(R.layout.row_perticular_view, roomNumberLay, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inflatedView.setLayoutParams(layoutParams);

        inflatedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFlatFacePopupMenu(inflatedView);
            }
        });

        int defaultSelection = 1;
        updatePickerView(inflatedView, getString(R.string.flat_face), roomFaceArray[defaultSelection] + " " + getString(R.string.facing) + " " + getString(R.string.flat));
        flatAdditionalInfoLay.addView(inflatedView);
        familyRoom[3] = defaultSelection;
        addRoomNumberView();
    }

    private final String[] roomFaceArray = {"North", "South", "East", "West"};

    private void showFlatFacePopupMenu(final View view) {
        PopupMenu popup = new PopupMenu(this, view);

        for (String face : roomFaceArray) {
            popup.getMenu().add(face + " " + getString(R.string.facing) + " " + getString(R.string.flat));
        }

        //popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String subTitle = String.valueOf(item.getTitle());
                String face = subTitle.split(" ")[0];
                for (int i = 0; i < roomFaceArray.length; i++) {
                    if (face.equalsIgnoreCase(roomFaceArray[i])) {
                        familyRoom[3] = i;
                        break;
                    }
                }
                updatePickerView(view, getString(R.string.flat_face), subTitle);
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {
        if (submitBtn == view) {
            submitAd();
        } else if (rentDate == view) {
            showDatePickerDialog();
        }
    }

    private DatabaseReference mDatabase;

    private void submitAd() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance().getReference();

        validateInputtedData();
    }

    private void validateInputtedData() {
        if (selectedLocation == null) {
            mapScrollView.smoothScrollTo(0, 0);
            showToast(getString(R.string.please_pick_a_location_into_the_map));
            return;
        }

        totalRent.setError(null);
        emailAddress.setError(null);
        mobileNumber.setError(null);

        if (totalRent == null) {
            showToast();
            return;
        } else if (totalRent.getText().length() == 0) {
            totalRent.setError(getString(R.string.error_field_required));
            totalRent.requestFocus();
            return;
        }

//        if (emailAddress == null) {
//            showToast();
//            return;
//        } else if (emailAddress.getText().length() == 0) {
//            emailAddress.setError(getString(R.string.error_field_required));
//            emailAddress.requestFocus();
//            return;
//        } else if (Patterns.EMAIL_ADDRESS.matcher(emailAddress.getText().toString()).matches()) {
//            emailAddress.setError(getString(R.string.error_field_required));
//            emailAddress.requestFocus();
//            return;
//        }

        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            googleSignOut();
            return;
        }

        if (!AppConstants.isMobileNumberValid(this, mobileNumber)) {
            return;
        }

        AppSharedPrefs.setMobileNumber(mobileNumber.getText().toString());
        viewSummaryDialog();
    }

    private void viewSummaryDialog() {
        final Dialog summaryDialog = new Dialog(this);
        summaryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        summaryDialog.setContentView(R.layout.dialog_ad_post_summary);
        Window window = summaryDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        }

        summaryDialog.show();

        TextView title = summaryDialog.findViewById(R.id.title);
        title.setText(getString(R.string.is_everything_ok));

        TextView roomDetails = summaryDialog.findViewById(R.id.roomDetails);
        TextView address = summaryDialog.findViewById(R.id.address);
        TextView totalRent = summaryDialog.findViewById(R.id.totalRent);

        String r = familyRoom[0] + " bedroom, " + familyRoom[1] + " bathroom, " + familyRoom[2] + " balcony.";
        roomDetails.setText(r);

        address.setText(addressDetails.getText());

        long totalR = Long.parseLong(this.totalRent.getText().toString());
        long totalU = utilityBill.getCheckedRadioButtonId() == R.id.utilityBillIncluded ? 0 :
                totalUtility.getText().length() > 0 ? Long.parseLong(totalUtility.getText().toString()) : 0;
        totalR = totalR + totalU;
        String tr = "Total Rent: " + String.valueOf(totalR) + " (include utility bill)";

        totalRent.setText(tr);

        summaryDialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                summaryDialog.dismiss();
                writeNewPost();
            }
        });

        summaryDialog.findViewById(R.id.noBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                summaryDialog.dismiss();
            }
        });

//        LinearLayout okBtnLay =  summaryDialog.findViewById(R.id.okBtnLay);
//        LinearLayout noBtnLay =  summaryDialog.findViewById(R.id.noBtnLay);
//
//        okBtnLay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                summaryDialog.dismiss();
//                writeNewPost();
//            }
//        });
//
//        noBtnLay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                summaryDialog.dismiss();
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        adId = null;
    }

    private String adId = null;

    private void writeNewPost() {
        showProgressDialog();
        adId = mDatabase.child(DBConstants.adList).push().getKey();
        final AdInfo adInfo = new AdInfo(adId, date[0], (date[1] + 1), date[2], rentLatitude, rentLongitude,
                addressDetails.getText().toString(), "", "", "", "", "",
                1, familyRoom[0], familyRoom[1], familyRoom[2], familyRoom[3], 1,
                houseInfo.getText().toString(),
                whichFloor.getText().length() == 0 ? -1 : Integer.parseInt(whichFloor.getText().toString()),
                drawingDining.getCheckedRadioButtonId() == R.id.drawingDiningYes ? 1 : 0,
                electricityCB.isChecked() ? 1 : 0, gasCB.isChecked() ? 1 : 0, waterCB.isChecked() ? 1 : 0,
                liftCB.isChecked() ? 1 : 0, generatorCB.isChecked() ? 1 : 0, securityGuardCB.isChecked() ? 1 : 0,
                totalSpace.getText().length() == 0 ? -1 : Long.parseLong(totalSpace.getText().toString()),
                Long.parseLong(totalRent.getText().toString()),
                utilityBill.getCheckedRadioButtonId() == R.id.utilityBillIncluded || totalUtility.getText().length() > 0 ?
                        Long.parseLong(totalUtility.getText().toString()) : 0,
                getUid());

        //AdInfo adInfo = new AdInfo(adId, getUid());
        HashMap<String, Object> adValues = adInfo.toMap();
        adValues.put(DBConstants.createdTime, ServerValue.TIMESTAMP);
        adValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);

//        showLog("server time: " + ServerValue.TIMESTAMP + " device time: " + System.currentTimeMillis());

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DBConstants.adList + "/" + adId, adValues);
        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    if (selectedLocation != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation.getPosition(), DEFAULT_ZOOM));
                        if (selectedLocation.isInfoWindowShown()) {
                            selectedLocation.hideInfoWindow();
                        }
                    }
                    if (googleMap.isMyLocationEnabled()) {
                        if (!(ActivityCompat.checkSelfPermission(NewAdActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewAdActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                            googleMap.setMyLocationEnabled(false);
                        }
                    }
                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                                @Override
                                public void onSnapshotReady(Bitmap bitmap) {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    uploadImage(AppConstants.adMapImageType, adId, byteArray);
                                }
                            });
                        }
                    });
                } else {
                    showToast(databaseError.getMessage());
                    closeProgressDialog();
                }
                showLog();
            }
        });
    }

    private void updateUserInfo() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance().getReference();

        if (firebaseUser == null) {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put(DBConstants.userId, firebaseUser.getUid());
        userValues.put(DBConstants.userEmail, firebaseUser.getEmail());
        userValues.put(DBConstants.userDisplayName, firebaseUser.getDisplayName());

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DBConstants.user + "/" + getUid(), userValues);
        mDatabase.updateChildren(childUpdates);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                date[0] = dayOfMonth;
                date[1] = monthOfYear;
                date[2] = year;
                String selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                setRentDate(selectedDate);
            }
        }, date[2], date[1], date[0]);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setRentDate(String date) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        try {
            Date newDate = dateFormatter.parse(date);
            dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            date = dateFormatter.format(newDate);

            long differenceTime = newDate.getTime() - System.currentTimeMillis();
            long elapsedDays = 0;
            if (differenceTime > 1) {
                elapsedDays = (differenceTime / (60 * 60 * 24 * 1000)) + 1;
            }

            if (elapsedDays <= 1) {
                date = "From " + date + "\n" + elapsedDays + " day remaining.";
            } else {
                date = "From " + date + "\n" + elapsedDays + " days remaining.";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            setRentDate(getDefaultRentMonth());
        }

        rentDate.setText(date);
    }

    private String getDefaultRentMonth() {
        Calendar calendar = Calendar.getInstance();
        int monthOfYear = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        if (monthOfYear > 11) {
            monthOfYear = 0;
            year++;
        }

        date[0] = 1;
        date[1] = monthOfYear;
        date[2] = year;

        return date[0] + "-" + (date[1] + 1) + "-" + date[2];
    }

    private int[] date = new int[3];//0=dayOfMonth, 1=monthOfYear, 2=year

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final int[] familyRoom = new int[4];//0=bedroom, 1=bathroom, 2=balcony, 3=flatFace

    public void defaultCheck() {
        rentType.check(R.id.family);
        drawingDining.check(R.id.drawingDiningYes);
        utilityBill.check(R.id.utilityBillNotIncluded);

        utilityBill.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.utilityBillIncluded) {
                    totalUtilityTIL.setVisibility(View.GONE);
                    utilityBdt.setVisibility(View.GONE);
                } else {
                    totalUtilityTIL.setVisibility(View.VISIBLE);
                    utilityBdt.setVisibility(View.VISIBLE);
                }
            }
        });

        drawingDining.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                setCalculatedRent();
            }
        });

        rentType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.family) {
                    changeAsFamilyType();
                } else if (checkedId == R.id.sublet) {
                    changeAsSubletType();
                } else if (checkedId == R.id.bachelor) {
                    changeAsBachelorType();
                } else {
                    changeAsOthersType();
                }
            }
        });

        setCalculatedRent();
    }

    private void changeAsFamilyType() {

    }

    private void changeAsSubletType() {

    }

    private void changeAsBachelorType() {

    }

    private void changeAsOthersType() {

    }

    private void setCalculatedRent() {
        long rentCal = (familyRoom[0] * singleBedRoomRent) + (familyRoom[1] * bathroomRent) + (familyRoom[2] * balconyRent);
        long spaceCal = (familyRoom[0] * singleBedRoomSpace) + (familyRoom[1] * bathroomSpace) + (familyRoom[2] * balconySpace);

        if (drawingDining.getCheckedRadioButtonId() == R.id.drawingDiningYes) {
            rentCal += drawingDiningRent;
            spaceCal += drawingDiningSpace;
        }

        totalRent.setText(String.valueOf(rentCal));
        totalSpace.setText(String.valueOf(spaceCal));
        totalUtility.setText(String.valueOf(waterBill + gasBill));
    }

    private final long singleBedRoomRent = 6000;//rent BDT
    private final long bathroomRent = 1500;//rent BDT
    private final long balconyRent = 1000;//rent BDT
    private final long drawingDiningRent = 8000;//rent BDT

    private final long singleBedRoomSpace = 210;//space sqrft
    private final long bathroomSpace = 60;//space sqrft
    private final long balconySpace = 50;//space sqrft
    private final long drawingDiningSpace = 280;//space sqrft

    private long waterBill = 800;
    private long gasBill = 900;

    @Override
    protected void imageUploadSuccess() {
        closeProgressDialog();
        showToast(getString(R.string.success));
        mediaAlertDialog();
    }

    @Override
    protected void imageUploadFailed() {
        closeProgressDialog();
    }

    private void mediaAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ad_posted_successfully);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage(R.string.your_ad_posted_successfully_would_u_like);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(MediaActivity.class);
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(AdListActivity.class);
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private <T> void startActivity(Class<T> var) {
        finish();
        Intent intent = new Intent(this, var);
        intent.putExtra(DBConstants.adId, adId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
