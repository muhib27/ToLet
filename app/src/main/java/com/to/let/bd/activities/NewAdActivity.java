package com.to.let.bd.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.common.WorkaroundMapFragment;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.SmartToLetConstants;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class NewAdActivity extends BaseActivity
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
    //    private Spinner bedRoom, balcony, bathRoom, whichFacing;
    private CheckBox waterCB, gasCB, electricityCB, liftCB, generatorCB, securityGuardCB;

    private TextView rentDate;
    private LinearLayout roomNumberLay;
    private boolean drawiningDiningExist, waterExist, gazExist, electricityExist, liftExist, generatorExist;

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private LatLng mDefaultLatLng = new LatLng(23.8103, 90.4125);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
//    private boolean mLocationPermissionGranted;

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
        addRoomNumberView();
        setRentDate(getDefaultRentMonth());
        //defaultSetup("family");

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

    private FloatingActionButton fab;
    private TextView addressDetails;
    private ScrollView mapScrollView;
    private EditText emailAddress, mobileNumber;
    private String drawingAndDining = "yes";
    private int flatType = 1;
    private LinearLayout flatAdditionalInfoLay;
    private EditText totalSpace, whichFloor, houseInfo, totalRent, totalUtility;
    private RadioGroup drawingDining, rentType, utilityBill;
    private TextInputLayout totalUtilityTIL;

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.post_your_add);
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mapScrollView = (ScrollView) findViewById(R.id.mapScrollView);
        addressDetails = (TextView) findViewById(R.id.addressDetails);

        emailAddress = (EditText) findViewById(R.id.emailAddress);
        mobileNumber = (EditText) findViewById(R.id.mobileNumber);
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

        rentType = (RadioGroup) findViewById(R.id.rentType);
        drawingDining = (RadioGroup) findViewById(R.id.drawingDining);
        utilityBill = (RadioGroup) findViewById(R.id.utilityBill);

        roomNumberLay = (LinearLayout) findViewById(R.id.roomNumberLay);

//        bedRoom = (Spinner) findViewById(R.id.bedRoom);
//        balcony = (Spinner) findViewById(R.id.balcony);
//        bathRoom = (Spinner) findViewById(R.id.bathRoom);
//        whichFacing = (Spinner) findViewById(R.id.whichFacing);

        flatAdditionalInfoLay = (LinearLayout) findViewById(R.id.flatAdditionalInfoLay);
        houseInfo = (EditText) findViewById(R.id.houseInfo);
        whichFloor = (EditText) findViewById(R.id.whichFloor);
        totalSpace = (EditText) findViewById(R.id.totalSpace);
        totalRent = (EditText) findViewById(R.id.totalRent);

        totalUtilityTIL = (TextInputLayout) findViewById(R.id.totalUtilityTIL);
        totalUtility = (EditText) findViewById(R.id.totalUtility);

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

        rentDate = (TextView) findViewById(R.id.rentDate);
        rentDate.setOnClickListener(this);

        defaultCheck();
    }

    private Handler handler = new Handler();
    private Runnable mobileNumberValidation = new Runnable() {
        @Override
        public void run() {
            mobileNumber.setError(null);
            if (!AppConstants.isMobileNumberValid(mobileNumber.getText().toString())) {
                mobileNumber.setError(getString(R.string.error_valid_mobile_number));
            }
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

    String rentTypeSelected = "family";

    public void defaultCheck() {
        rentType.check(R.id.family);
        drawingDining.check(R.id.drawingDiningYes);
        utilityBill.check(R.id.utilityBillNotIncluded);

        utilityBill.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.utilityBillIncluded) {
                    totalUtilityTIL.setVisibility(View.GONE);
                } else {
                    totalUtilityTIL.setVisibility(View.VISIBLE);
                }
            }
        });


//        ((RadioButton) findViewById(rentType.getCheckedRadioButtonId())).setChecked(true);
//        rentType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                // This will get the radio button that has changed in its check state
//                RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
//                // This puts the value (true/false) into the variable
//                boolean isChecked = checkedRadioButton.isChecked();
//                // If the radio button that has changed in check state is now checked...
//                if (isChecked) {
//                    switch (checkedRadioButton.getId()) {
//                        case R.id.family:
//                            rentTypeSelected = "family";
//                            flatType = 1;
//                            defaultSetup(rentTypeSelected);
//
//                            break;
//                        case R.id.sublet:
//                            rentTypeSelected = "sublet";
//                            flatType = 2;
//                            defaultSetup(rentTypeSelected);
//                            break;
//                        case R.id.bachelor:
//                            rentTypeSelected = "bachelor";
//                            flatType = 3;
//                            defaultSetup(rentTypeSelected);
//                            break;
//                        case R.id.others:
//                            rentTypeSelected = "others";
//                            flatType = 4;
//                            defaultSetup(rentTypeSelected);
//                            break;
//                    }
//                }
//            }
//        });
    }

    public void defaultSetup(String type) {
        if (type.equals("family")) {
//            bedRoom.setSelection(1);
//            balcony.setSelection(1);
//            bathRoom.setSelection(1);
            waterCB.setChecked(true);
            gasCB.setChecked(true);
            electricityCB.setChecked(true);
        } else {
//            bedRoom.setSelection(0);
//            balcony.setSelection(0);
//            bathRoom.setSelection(0);
            waterCB.setChecked(true);
            gasCB.setChecked(true);
            electricityCB.setChecked(true);
        }
    }

    // Google login
    private void googleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SmartToLetConstants.GOOGLE_SIGN_IN);
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
        if (requestCode == SmartToLetConstants.GOOGLE_SIGN_IN) {
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
        updateUserInfo();
    }

//    private void linkedCredential(final AuthCredential credential) {
//        showProgressDialog();
//        firebaseUser.linkWithCredential(credential).addOnCompleteListener(this,
//                new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            setEmailAddress();
//                            closeProgressDialog();
//                        } else {
//                            try {
//                                String message = task.getException().getMessage();
//                                if (message != null && (message.toLowerCase().equals(SmartToLetConstants.firebaseAccountConflictMessage1.toLowerCase()) ||
//                                        message.toLowerCase().contains(SmartToLetConstants.firebaseAccountConflictMessage2.toLowerCase()))) {
//                                    firebaseLoginWithGoogle(credential);
//                                } else {
//                                    showToast(message);
//                                    closeProgressDialog();
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                closeProgressDialog();
//                            }
//                        }
//                    }
//                });
//    }

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
        // Inflate the layouts for the info window, roomFaceTitle and snippet.
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
     * String fullAddress
     */
    String fullAddress;

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

        fullAddress = getLocationBestApproximateResult(findCurrentLocationDetails(mDefaultLatLng.latitude, mDefaultLatLng.longitude), mDefaultLatLng);
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
    double longti;
    double lat;
    private String countryName;
    private String division;

    private List<Address> findCurrentLocationDetails(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addressList = new ArrayList<>();
        addressList.clear();

        geocoder = new Geocoder(this, Locale.ENGLISH);
//
//        latitude = 23.7929279;
//        longitude = 90.4035839;
        longti = longitude;
        lat = latitude;

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
                division = address.getLocality();
            }

            if (address.getCountryName() != null && !result.contains(address.getCountryName())) {
                result = result + "," + address.getCountryName();
                countryName = address.getCountryName();
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

    private final String[] roomTypes = {"Bedroom", "Bathroom", "Balcony"};

    public void addRoomNumberView() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < roomTypes.length; i++) {
            final View inflatedView = inflater.inflate(R.layout.single_room_number_lay, roomNumberLay, false);
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
            if (i == 2) {
                updatePickerView(inflatedView, roomTypes[i], (roomArray[1] + " " + roomTypes[i]));
            } else {
                updatePickerView(inflatedView, roomTypes[i], (roomArray[2] + " " + roomTypes[i] + "'s"));
            }
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

    private final int[] roomArray = {0, 1, 2, 3, 4, 5};

    private void showRoomNumberPopupMenu(final View view, final String roomType) {
        PopupMenu popup = new PopupMenu(this, view);

        for (int room : roomArray) {
            String s = "";
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
                String details = String.valueOf(item.getTitle());
                updatePickerView(view, (details.split(" "))[1].replace("'s", ""), details);
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
        final View inflatedView = inflater.inflate(R.layout.single_room_number_lay, roomNumberLay, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inflatedView.setLayoutParams(layoutParams);

        inflatedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFlatFacePopupMenu(inflatedView);
            }
        });

        updatePickerView(inflatedView, getString(R.string.flat_face), roomFaceArray[1] + " " + getString(R.string.facing) + " " + getString(R.string.flat));
        flatAdditionalInfoLay.addView(inflatedView);
    }

    private final String[] roomFaceArray = {"North", "South", "East", "West"};

    private void showFlatFacePopupMenu(final View view) {
        PopupMenu popup = new PopupMenu(this, view);

        for (String room : roomFaceArray) {
            String s = room + " " + getString(R.string.facing) + " " + getString(R.string.flat);
            popup.getMenu().add(s);
        }

        //popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                updatePickerView(view, getString(R.string.flat_face), String.valueOf(item.getTitle()));
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
        if (fab == view) {
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
        writeNewPost();
    }

    private void validateInputtedData() {

    }

    private void writeNewPost() {
        String adId = mDatabase.child(DBConstants.adList).push().getKey();

        houseInfo.getText();
        whichFloor.getText();
//        whichFacing.getSelectedItem();
//        bedRoom.getSelectedItem();
//        balcony.getSelectedItem();
//
//        if (!dateIsSet) {
//            Toast.makeText(this, "set date", Toast.LENGTH_SHORT).show();
//            return;
//        }

        //String rentFromSt =
//
//        AdInfo adInfo = new AdInfo(adId, fromMonth, fromDay, fromYear, lat, longti, fullAddress, countryName,
//                division, division, division, division, flatType, houseInfo.getText().toString(), Integer.parseInt(whichFloor.getText().toString()),
//                whichFacing.getSelectedItem().toString(), Integer.parseInt(totalSpace.getText().toString()), Integer.parseInt(bedRoom.getSelectedItem().toString()), Integer.parseInt(bathRoom.getSelectedItem().toString()),
//                Integer.parseInt(balcony.getSelectedItem().toString()), 1, drawiningDiningExist, electricityCB.isChecked(), gasCB.isChecked(), waterCB.isChecked(), liftCB.isChecked(), generatorCB.isChecked(),
//                Integer.parseInt(totalRent.getText().toString()), 3000, getUid());
//        //adInfo.settHouseNameOrNumber(houseInfo.getText().toString());

//        Toast.makeText(this, whichFacingSt + drawingAndDining, Toast.LENGTH_SHORT).show();
//        //AdInfo adInfo = new AdInfo(adId, getUid());
//        HashMap<String, Object> adValues = adInfo.toMap();
//        HashMap<String, Object> childUpdates = new HashMap<>();
//        childUpdates.put("/" + DBConstants.adList + "/" + adId, adValues);
//        mDatabase.updateChildren(childUpdates);
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


    /**
     * Demonstrates customizing the info window and/or its contents.
     */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "roomFaceTitle" and "snippet".
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

    private int[] date = new int[3];

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
}
