package com.to.let.bd.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseMapActivity;
import com.to.let.bd.common.WorkaroundMapFragment;
import com.to.let.bd.fragments.FamilyFlatAd;
import com.to.let.bd.fragments.MessFlatAd;
import com.to.let.bd.fragments.OthersFlatAd;
import com.to.let.bd.fragments.SubletFlatAd;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.model.FamilyInfo;
import com.to.let.bd.model.MessInfo;
import com.to.let.bd.model.OthersInfo;
import com.to.let.bd.model.SubletInfo;
import com.to.let.bd.utils.ActivityUtils;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.AppSharedPrefs;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.FirebaseAuthError;
import com.to.let.bd.utils.GoogleApiHelper;
import com.to.let.bd.utils.PhoneNumberUtils;
import com.to.let.bd.utils.UploadImageService;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NewAdActivity2 extends BaseMapActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final String TAG = NewAdActivity2.class.getSimpleName();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_new_post2;
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.post_your_ad);
    }

    @Override
    protected void setEmailAddress(boolean afterSuccessfulLogin) {
        initEmail();
        emailAddress.setText(firebaseUser.getEmail());
        emailAddress.setEnabled(false);

        if (afterSuccessfulLogin) {
            validateInputtedData();
            updateUserInfo();
        }
    }

    @Override
    protected void onCreate() {
        getAdInfo();
        init();
        initTabLayout();
        addRoomFaceType(null);
        setRentDate(getDefaultRentMonth());
        initBroadcast();
        updateViewForEdit();
        initPlace();
    }

    private PlaceAutocompleteFragment placeAutocomplete;
    private boolean onPlaceSelected = false;

    private void initPlace() {
        placeAutocomplete = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.placeAutocomplete);
        placeAutocomplete.setHint(getString(R.string.please_type_here_for_search));
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setCountry("BD")
                .build();
        placeAutocomplete.setFilter(typeFilter);
        placeAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                onPlaceSelected = true;
                addressDetails.setText(place.getAddress());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
            }

            @Override
            public void onError(Status status) {
                showLog("An error occurred: " + status);
            }
        });
    }

    private AdInfo adInfo;

    private void getAdInfo() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
    }

    private void updateViewForEdit() {
        if (adInfo == null)
            return;

        totalRent.setText(String.valueOf(adInfo.flatRent));
        totalUtility.setText(adInfo.othersFee > 0 ? String.valueOf(adInfo.othersFee) : "");
        addressDetails.setText(adInfo.fullAddress);
        submitBtn.setText(R.string.update);

        if (date == null) {
            date = new int[3];
        }

        date[0] = adInfo.startingDate;
        date[1] = adInfo.startingMonth;
        date[2] = adInfo.startingYear;
        String selectedDate = date[0] + "-" + (date[1] + 1) + "-" + date[2];
        setRentDate(selectedDate);

//        int tabIndex = 0;
//        if (adInfo.messInfo != null) {
//            tabIndex = 1;
//        } else if (adInfo.subletInfo != null) {
//            tabIndex = 2;
//        } else if (adInfo.othersInfo != null) {
//            tabIndex = 3;
//        }
//
//        if (tabIndex > tabLayout.getTabCount() - 1) {
//            return;
//        }

//        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
//        assert tab != null;
//        tab.select();
//        updateMarkerIcon();
//        if (adInfo.familyInfo != null) {
//            FamilyFlatAd familyFlatAd = (FamilyFlatAd) getSupportFragmentManager().findFragmentByTag(FamilyFlatAd.TAG);
//            familyFlatAd.updateData(adInfo.familyInfo);
//        } else if (adInfo.messInfo != null) {
//            MessFlatAd messFlatAd = (MessFlatAd) getSupportFragmentManager().findFragmentByTag(MessFlatAd.TAG);
//            messFlatAd.updateData(adInfo.messInfo);
//        } else if (adInfo.subletInfo != null) {
//            SubletFlatAd subletFlatAd = (SubletFlatAd) getSupportFragmentManager().findFragmentByTag(SubletFlatAd.TAG);
//            subletFlatAd.updateData(adInfo.subletInfo);
//        } else if (adInfo.othersInfo != null) {
//            OthersFlatAd othersFlatAd = (OthersFlatAd) getSupportFragmentManager().findFragmentByTag(OthersFlatAd.TAG);
//            othersFlatAd.updateData(adInfo.othersInfo);
//        }

        LinearLayout tabStrip = ((LinearLayout) tabLayout.getChildAt(0));

        if (tabStrip == null)
            return;
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    showLog();
                    return true;
                }
            });
        }

        houseInfo.setText(adInfo.houseNameOrNumber);
        whichFloor.setText(adInfo.floorNumber > -1 ? String.valueOf(adInfo.floorNumber) : "");
        description.setText(adInfo.flatDescription);
    }

    private void completeSingleImageUpload(int type, String adId, String[] imageContents) {
        updateDatabaseForImage(type, adId, imageContents);
    }

    private void updateDatabaseForImage(final int type, final String adId, final String[] imageContents) {
        HashMap<String, Object> mapImageValue = new HashMap<>();
        if (type == AppConstants.adMapImageType) {
            mapImageValue.put(AppConstants.downloadUrl, imageContents[0]);
            mapImageValue.put(AppConstants.imageName, imageContents[1]);
            mapImageValue.put(AppConstants.imagePath, imageContents[2]);
        }

        HashMap<String, Object> adValues = new HashMap<>();
        adValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);
        adValues.put(DBConstants.map, mapImageValue);

        databaseReference
                .child(DBConstants.adList)
                .child(flatType)
                .child(adId)
                .updateChildren(adValues, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            mediaAlertDialog(adId);
                        } else {
                            updateDatabaseForImage(type, adId, imageContents);
                        }
                        closeProgressDialog();
                    }
                });
    }

    private void mediaAlertDialog(final String adId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (adInfo == null) builder.setTitle(R.string.ad_published_successfully);
        else builder.setTitle(R.string.ad_published_successfully);

        builder.setIcon(R.mipmap.ic_launcher);
        if (adInfo == null) {
            builder.setMessage(R.string.your_ad_published_successfully_would_u_like);
        } else {
            if (adInfo.images != null && adInfo.images.size() > 0) {
                builder.setMessage(R.string.your_ad_updated_successfully_would_u_like);
            } else {
                builder.setMessage(R.string.your_ad_published_successfully_would_u_like);
            }
        }

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startMediaActivity(adId);
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startMediaActivity(String adId) {
        Intent intent = new Intent(this, MediaActivity.class);
        if (adInfo != null && adInfo.images != null && adInfo.images.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(AppConstants.keyImageList, adInfo.images);
            intent.putExtras(bundle);
        }

        intent.putExtra(DBConstants.adId, adId);
        intent.putExtra(DBConstants.flatType, flatType);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected BroadcastReceiver mBroadcastReceiver;

    private void initBroadcast() {
        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null)
                    return;

                switch (intent.getAction()) {
                    case AppConstants.uploadError:
                        closeProgressDialog();
                        break;
                    case AppConstants.uploadComplete: {
                        int type = intent.getIntExtra(AppConstants.keyType, 0);
                        String adId = intent.getStringExtra(DBConstants.adId);
                        int imageIndex = intent.getIntExtra(AppConstants.imageIndex, 0);
                        String[] imageContents = intent.getStringArrayExtra(AppConstants.imageContents);
                        completeSingleImageUpload(type, adId, imageContents);
                    }
                    break;
                    case AppConstants.uploadProgress: {
                        int type = intent.getIntExtra(AppConstants.keyType, 0);
                        String adId = intent.getStringExtra(DBConstants.adId);
                        int imageIndex = intent.getIntExtra(AppConstants.imageIndex, 0);
                        int progress = intent.getIntExtra(AppConstants.progress, -1);

//                        if (type == AppConstants.adImageType)
//                            updateProgress(imageIndex, progress);
                    }
                    break;
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, UploadImageService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private Button submitBtn;
    private EditText addressDetails;
    private EditText emailAddress, mobileNumber;
    private LinearLayout flatAdditionalInfoLay;
    private EditText houseInfo, whichFloor, description, totalRent, totalUtility;
    private ImageView fixedMarker;
    private ProgressBar locationLoaderProgressBar;

    private void updateTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private void init() {
        mapScrollView = findViewById(R.id.mapScrollView);

        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(this);

        addressDetails = findViewById(R.id.addressDetails);

        initEmail();
        mobileNumber = findViewById(R.id.mobileNumber);
//        mobileNumber.setText(AppSharedPrefs.getMobileNumber());
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                mobileNumber.setOnFocusChangeListener(this);
            } else {
                String phoneNumber = firebaseUser.getPhoneNumber();
                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    mobileNumber.setOnFocusChangeListener(this);
                } else {
                    mobileNumber.setText(phoneNumber);
                    mobileNumber.setEnabled(false);
                }
            }
        }

        mobileNumber.addTextChangedListener(textWatcher);

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

        flatAdditionalInfoLay = findViewById(R.id.flatAdditionalInfoLay);
        houseInfo = findViewById(R.id.houseInfo);
        whichFloor = findViewById(R.id.whichFloor);
        description = findViewById(R.id.description);
        totalRent = findViewById(R.id.totalRent);
        totalUtility = findViewById(R.id.totalUtility);

        totalUtility.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (!(emailAddress.getText() == null || emailAddress.getText().toString().isEmpty()
                        || emailAddress.getText().toString().trim().isEmpty())) {
                    description.requestFocus();
                    return true;
                }
                return false;
            }
        });

        rentDate = findViewById(R.id.rentDate);
        rentDate.setOnClickListener(this);

        remainingTime = findViewById(R.id.remainingTime);
        fixedMarker = findViewById(R.id.fixedMarker);
        locationLoaderProgressBar = findViewById(R.id.locationLoaderProgressBar);
        locationLoaderProgressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private TextWatcher textWatcher = new TextWatcher() {
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
    };

    private void initEmail() {
        if (emailAddress == null)
            emailAddress = findViewById(R.id.emailAddress);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == mobileNumber) {
            if (hasFocus) {
                if (emailAddress.length() > 1)
                    showPhoneAutoCompleteHint();
                else
                    googleSignOut();
            }
        } else if (view == emailAddress) {
            if (hasFocus)
                googleSignOut();
        }
    }

    private void showPhoneAutoCompleteHint() {
        try {
            startIntentSenderForResult(getPhoneHintIntent().getIntentSender(), AppConstants.phoneHint, null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            showLog("Unable to start hint intent: " + e);
        }
    }

    private PendingIntent getPhoneHintIntent() {
        GoogleApiClient client = new GoogleApiClient
                .Builder(this)
                .addApi(Auth.CREDENTIALS_API)
                .enableAutoManage(
                        this,
                        GoogleApiHelper.getSafeAutoManageId(),
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                showLog("Client connection failed: " + connectionResult.getErrorMessage());
                            }
                        })
                .build();


        HintRequest hintRequest = new HintRequest.Builder()
                .setHintPickerConfig(
                        new CredentialPickerConfig.Builder().setShowCancelButton(true).build())
                .setPhoneNumberIdentifierSupported(true)
                .setEmailAddressIdentifierSupported(false)
                .build();

        return Auth.CredentialsApi.getHintPickerIntent(client, hintRequest);
    }

    private void addRoomFaceType(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View inflatedView = inflater.inflate(R.layout.row_particular_view, viewGroup, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inflatedView.setLayoutParams(layoutParams);

        inflatedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFlatFacePopupMenu(inflatedView);
            }
        });

        int defaultSelection = 1;
        AppConstants.updatePickerView(inflatedView, getString(R.string.flat_face), roomFaceArray[defaultSelection] + " " + getString(R.string.facing) + " " + getString(R.string.flat));
        flatAdditionalInfoLay.addView(inflatedView);
    }

    private final String[] roomFaceArray = {"North", "South", "East", "West"};
    private int flatFaceSelection = 1;

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
                        flatFaceSelection = i;
                        break;
                    }
                }
                AppConstants.updatePickerView(view, getString(R.string.flat_face), subTitle);
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    private Handler handler = new Handler();
    private Runnable mobileNumberValidation = new Runnable() {
        @Override
        public void run() {
            mobileNumber.setError(null);
            if (AppConstants.isMobileNumberValid(NewAdActivity2.this, mobileNumber)) {
                needToVerifyPhoneNumberAlert();
            }
        }
    };

    private void needToVerifyPhoneNumberAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(R.mipmap.ic_launcher_round);
        alertDialog.setTitle(R.string.alert);
        alertDialog.setMessage(getString(R.string.you_need_to_verify_your_phone_number));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendCode("+88" + AppConstants.formatAsSimplePhoneNumber(mobileNumber.getText().toString()));
                    }
                });

        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private TextView rentDate, remainingTime;
    private TabLayout tabLayout;
    private ArrayList<String> flatTypesArray = new ArrayList<>();

    private void initTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);

        flatTypesArray.clear();
        flatTypesArray.add(getString(R.string.family));
        flatTypesArray.add(getString(R.string.mess_member));
        flatTypesArray.add(getString(R.string.sublet));
        flatTypesArray.add(getString(R.string.others));

        tabLayout.addTab(tabLayout.newTab().setText(flatTypesArray.get(0)), false);
        tabLayout.addTab(tabLayout.newTab().setText(flatTypesArray.get(1)), false);
        tabLayout.addTab(tabLayout.newTab().setText(flatTypesArray.get(2)), false);
        tabLayout.addTab(tabLayout.newTab().setText(flatTypesArray.get(3)), false);

        int tabIndex = 0;
        if (adInfo != null) {
            if (adInfo.familyInfo != null) {
                tabIndex = 0;
            } else if (adInfo.messInfo != null) {
                tabIndex = 1;
            } else if (adInfo.subletInfo != null) {
                tabIndex = 2;
            } else if (adInfo.othersInfo != null) {
                tabIndex = 3;
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                String tag;
                Bundle bundle = new Bundle();
                if (tab.getPosition() == 0) {
                    fragment = FamilyFlatAd.newInstance();
                    if (adInfo != null && adInfo.familyInfo != null) {
                        bundle.putSerializable(DBConstants.familyInfo, adInfo.familyInfo);
                        bundle.putLong(DBConstants.flatSpace, adInfo.flatSpace);
                    }
                    tag = FamilyFlatAd.TAG;
                } else if (tab.getPosition() == 1) {
                    fragment = MessFlatAd.newInstance();
                    if (adInfo != null && adInfo.messInfo != null) {
                        bundle.putSerializable(DBConstants.messInfo, adInfo.messInfo);
                    }
                    tag = MessFlatAd.TAG;
                } else if (tab.getPosition() == 2) {
                    fragment = SubletFlatAd.newInstance();
                    if (adInfo != null && adInfo.subletInfo != null) {
                        bundle.putSerializable(DBConstants.subletInfo, adInfo.subletInfo);
                    }
                    tag = SubletFlatAd.TAG;
                } else {
                    fragment = OthersFlatAd.newInstance();
                    if (adInfo != null && adInfo.othersInfo != null) {
                        bundle.putSerializable(DBConstants.othersInfo, adInfo.othersInfo);
                        bundle.putLong(DBConstants.flatSpace, adInfo.flatSpace);
                    }
                    tag = OthersFlatAd.TAG;
                }
                updateTitle(getString(R.string.post_your_ad) + " (" + tab.getText() + ") ");

                fragment.setArguments(bundle);
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                        fragment, R.id.fragmentContainer, tag);
                updateMarkerIcon();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        TabLayout.Tab tab = tabLayout.getTabAt(tabIndex);
        assert tab != null;
        tab.select();
    }

    public ScrollView mapScrollView;
    private GoogleMap googleMap;

    @Override
    protected void onMapReady2(GoogleMap googleMap) {
        this.googleMap = googleMap;

        mapScrollView = findViewById(R.id.mapScrollView); //parent scrollview in xml, give your scrollview id value

        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .setListener(new WorkaroundMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        mapScrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });
        this.googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                selectedCenterLatLng = NewAdActivity2.this.googleMap.getCameraPosition().target;
                if (!onPlaceSelected) {
                    locationLoaderProgressBar.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(addressLoader);
                    handler.postDelayed(addressLoader, 500);
                } else {
                    if (locationLoaderProgressBar.getVisibility() == View.VISIBLE)
                        locationLoaderProgressBar.setVisibility(View.GONE);
                }
                onPlaceSelected = false;
            }
        });

        this.googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                locationLoaderProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private LatLng selectedCenterLatLng;

    private Runnable addressLoader = new Runnable() {
        @Override
        public void run() {
            String findOutFullAddress = getLocationBestApproximateResult(findSelectedLocationDetails(selectedCenterLatLng.latitude, selectedCenterLatLng.longitude));
            placeAutocomplete.setText(findOutFullAddress);
            addressDetails.setText(findOutFullAddress);
            locationLoaderProgressBar.setVisibility(View.GONE);
        }
    };

    @Override
    protected void findLastKnownLocation(LatLng defaultLatLng) {
//        if (adInfo == null)
//            addMarker(defaultLatLng, getString(R.string.we_find_out_your_location));
//        else
//            addMarker(new LatLng(adInfo.latitude, adInfo.longitude), getString(R.string.your_previously_selected_location));
    }

//    private void previouslyPointLocation() {
//        if (adInfo != null) {
//            addMarker(new LatLng(adInfo.latitude, adInfo.longitude), getString(R.string.your_previously_selected_location));
//        }
//    }

    @Override
    protected void onLoadLocationDetails(String fullAddress) {
//        this.fullAddress = fullAddress;
//        addressDetails.setText(fullAddress);
    }

    @Override
    public void onClick(View view) {
        if (submitBtn == view) {
            submitAd();
        } else if (rentDate == view) {
            showDatePickerDialog();
        }
    }

    private DatabaseReference databaseReference;

    private void submitAd() {
        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();

        validateInputtedData();
    }

    private void validateInputtedData() {
        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            googleSignOut();
            return;
        }

        if (firebaseUser.getPhoneNumber() == null || firebaseUser.getPhoneNumber().isEmpty()) {
            if (!AppConstants.isMobileNumberValid(this, mobileNumber)) {
                return;
            }
            needToVerifyPhoneNumberAlert();
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

        summary = null;
        othersFacility = null;

        getRoomDetails();

        if (summary == null || othersFacility == null) {
            return;
        }

        AppSharedPrefs.setMobileNumber(mobileNumber.getText().toString());
        viewSummaryDialog();
    }

    private String summary, othersFacility;

    private void getRoomDetails() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            if (fragment instanceof FamilyFlatAd && fragment.isVisible()) {
                summary = ((FamilyFlatAd) fragment).getRoomSummary();
                othersFacility = ((FamilyFlatAd) fragment).getRoomOthersFacility();
            } else if (fragment instanceof MessFlatAd && fragment.isVisible()) {
                summary = ((MessFlatAd) fragment).getRoomSummary();
                othersFacility = ((MessFlatAd) fragment).getRoomOthersFacility();
            } else if (fragment instanceof SubletFlatAd && fragment.isVisible()) {
                summary = ((SubletFlatAd) fragment).getRoomSummary();
                othersFacility = ((SubletFlatAd) fragment).getRoomOthersFacility();
            } else if (fragment instanceof OthersFlatAd && fragment.isVisible()) {
                summary = ((OthersFlatAd) fragment).getRoomSummary();
                othersFacility = ((OthersFlatAd) fragment).getRoomOthersFacility();
            }
        }
    }

    private void viewSummaryDialog() {
        final Dialog summaryDialog = new Dialog(this);
        summaryDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        summaryDialog.setContentView(R.layout.dialog_summary);
        Window window = summaryDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        }

        summaryDialog.show();

        TextView title = summaryDialog.findViewById(R.id.title);
        title.setText(getString(R.string.summary));

        TextView roomSummary = summaryDialog.findViewById(R.id.roomSummary);
        TextView address = summaryDialog.findViewById(R.id.address);
        TextView totalRent = summaryDialog.findViewById(R.id.totalRent);
        TextView roomOthersFacility = summaryDialog.findViewById(R.id.othersFacility);

        roomSummary.setText(summary);
        roomOthersFacility.setText(othersFacility);

        address.setText(addressDetails.getText());

        long totalR = Long.parseLong(this.totalRent.getText().toString());
        String tr = "Total Rent: " + String.valueOf(totalR);
        long totalU = totalUtility.getText().length() > 0 ? Long.parseLong(totalUtility.getText().toString()) : 0;
        if (totalU > 0) tr += "\nOthers utility bill: " + totalU;
        totalRent.setText(tr);

        summaryDialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                summaryDialog.dismiss();
                writeNewPost();
            }
        });

        summaryDialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                summaryDialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private long getTotalSpace() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        String totalSpace = null;
        if (fragment != null && fragment instanceof FamilyFlatAd && fragment.isVisible()) {
            totalSpace = ((FamilyFlatAd) fragment).getTotalSpace();
        } else if (fragment != null && fragment instanceof OthersFlatAd && fragment.isVisible()) {
            totalSpace = ((OthersFlatAd) fragment).getTotalSpace();
        }

        if (totalSpace == null || totalSpace.trim().isEmpty()) {
            return 0;
        }

        return Long.parseLong(totalSpace);
    }

    private String flatType;

    private void writeNewPost() {
        showProgressDialog();

        final String adId;
        if (adInfo == null) {
            adId = databaseReference.child(DBConstants.adList).push().getKey();
        } else {
            adId = adInfo.adId;
        }

        int startingDate = date[0];
        int startingMonth = date[1] + 1;
        int startingYear = date[2];

        String fullAddress = addressDetails.getText().toString();
        String country = "";
        String division = "";
        String district = "";
        String subDistrict = "";
        String knownAsArea = "";

        long flatSpace = getTotalSpace();
        long flatRent = Long.parseLong(totalRent.getText().toString());
        long othersFee = totalUtility.getText().toString().trim().isEmpty() ? 0 : Long.parseLong(totalUtility.getText().toString());

        FamilyInfo familyInfo = null;
        MessInfo messInfo = null;
        SubletInfo subletInfo = null;
        OthersInfo othersInfo = null;

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null && fragment instanceof FamilyFlatAd && fragment.isVisible()) {
            familyInfo = ((FamilyFlatAd) fragment).getFamilyInfo();
            flatType = DBConstants.keyFamily;
        } else if (fragment != null && fragment instanceof MessFlatAd && fragment.isVisible()) {
            messInfo = ((MessFlatAd) fragment).getMessInfo();
            flatType = DBConstants.keyMess;
        } else if (fragment != null && fragment instanceof SubletFlatAd && fragment.isVisible()) {
            subletInfo = ((SubletFlatAd) fragment).getSubletInfo();
            flatType = DBConstants.keySublet;
        } else if (fragment != null && fragment instanceof OthersFlatAd && fragment.isVisible()) {
            othersInfo = ((OthersFlatAd) fragment).getOthersInfo();
            flatType = DBConstants.keyOthers;
        } else {
            flatType = DBConstants.keyOthers;
        }

        String mobileNumber = this.mobileNumber.getText().toString();
        String emailAddress = this.emailAddress.getText().toString();

        final AdInfo adInfo = new AdInfo(adId, startingMonth, startingDate, startingYear,
                selectedCenterLatLng.latitude, selectedCenterLatLng.longitude,
                fullAddress, country, division, district, subDistrict, knownAsArea,
                flatSpace, flatRent, othersFee,
                houseInfo.getText().toString(),
                whichFloor.getText().toString().trim().isEmpty() ? -1 : Integer.parseInt(whichFloor.getText().toString()),
                roomFaceArray[flatFaceSelection], description.getText().toString(),
                flatTypesArray.get(tabLayout.getSelectedTabPosition()),
                familyInfo, messInfo, subletInfo, othersInfo,
                getUid(), mobileNumber, emailAddress);

//        AdInfo adInfo = new AdInfo(adId, getUid());
        HashMap<String, Object> adValues = adInfo.toMap();
        adValues.put(DBConstants.createdTime, ServerValue.TIMESTAMP);
        adValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);

        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put(DBConstants.userId, getUid());
        userValues.put(DBConstants.mobileNumber, mobileNumber);
        userValues.put(DBConstants.mobileNumberVerified, true);
        userValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DBConstants.adList + "/"
                + flatType + "/" + adId, adValues);

        childUpdates.put("/" + DBConstants.users + "/"
                + DBConstants.registeredUsers + "/"
                + getUid(), userValues);
        databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                        @Override
                        public void onSnapshotReady(Bitmap bitmap) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                            byte[] byteArray = stream.toByteArray();
                            uploadImage(AppConstants.adMapImageType, adId, byteArray);
                        }
                    });
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(DBConstants.geoFire);
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(adId, new GeoLocation(selectedCenterLatLng.latitude, selectedCenterLatLng.longitude));
                } else {
                    showSimpleDialog(databaseError.getMessage());
                    closeProgressDialog();
                }
            }
        });
    }

    protected void uploadImage(int type, String adId, byte[] imageByte) {
        // Start StorageUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, UploadImageService.class)
                .putExtra(AppConstants.keyType, type)
                .putExtra(AppConstants.fileUri, imageByte)
                .putExtra(DBConstants.adId, adId)
                .putExtra(AppConstants.imageIndex, 0)
                .setAction(AppConstants.actionUpload));
    }

    private void updateUserInfo() {
        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();

        if (firebaseUser == null) {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        String fcmToken = FirebaseInstanceId.getInstance().getToken();

        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put(DBConstants.userId, firebaseUser.getUid());
        userValues.put(DBConstants.userEmail, firebaseUser.getEmail());
        userValues.put(DBConstants.userDisplayName, firebaseUser.getDisplayName());

        if (firebaseUser.getPhoneNumber() != null && firebaseUser.getPhoneNumber().isEmpty())
            userValues.put(DBConstants.userPhoneNumber, firebaseUser.getPhoneNumber());

        if (firebaseUser.getPhotoUrl() != null)
            userValues.put(DBConstants.userProfilePic, firebaseUser.getPhotoUrl().toString());

        userValues.put(DBConstants.fcmToken, fcmToken);

        databaseReference
                .child(DBConstants.users)
                .child(DBConstants.registeredUsers)
                .child(firebaseUser.getUid())
                .updateChildren(userValues);
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
        String remainingTime = "";
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
                remainingTime = elapsedDays + " day remaining.";
            } else {
                remainingTime = elapsedDays + " days remaining.";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            setRentDate(getDefaultRentMonth());
        }

        rentDate.setText(date);
        this.remainingTime.setText(remainingTime);
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

    private int[] date = new int[3];//0=dayOfMonth, 1=monthOfYear, 2=year (month start from 0)

    public void updateCalculatedRent(long calculatedRent) {
//        totalRent.setText(String.valueOf(calculatedRent));
//        totalRent.setText("");
    }

    public void focusDescription() {
        description.requestFocus();
    }

    private int getMarkerResource() {
        int resourceId = R.drawable.marker_purple_others;
        TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
        if (tab != null)
            if (String.valueOf(tab.getText()).equalsIgnoreCase(getString(R.string.family))) {
                resourceId = R.drawable.marker_blue_family;
            } else if (String.valueOf(tab.getText()).equalsIgnoreCase(getString(R.string.mess_member))) {
                resourceId = R.drawable.marker_green_mess;
            } else if (String.valueOf(tab.getText()).equalsIgnoreCase(getString(R.string.sublet))) {
                resourceId = R.drawable.marker_merun_sublet;
            }

        return resourceId;
    }

    private void updateMarkerIcon() {
        fixedMarker.setImageResource(getMarkerResource());
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.phoneHint) {
            if (data != null) {
                Credential cred = data.getParcelableExtra(Credential.EXTRA_KEY);
                if (cred != null) {
                    // Hint selector does not always return phone numbers in e164 format.
                    // To accommodate either case, we normalize to e164 with best effort
                    final String unformattedPhone = cred.getId();
                    final String formattedPhone = PhoneNumberUtils.formatUsingCurrentCountry(unformattedPhone, this);
                    if (formattedPhone == null) {
                        showLog("Unable to normalize phone number from hint selector:" + unformattedPhone);
                    } else {
//                      final PhoneNumber phoneNumberObj = PhoneNumberUtils.getPhoneNumber(formattedPhone);
                        sendCode(formattedPhone);
                    }
                }
            }
        }
    }

    private void sendCode(final String phoneNumber) {
        showProgressDialog();
        final long time = 60000; // one minute
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                time,
                TimeUnit.MILLISECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        closeProgressDialog();
                        updateUserPhoneNumber(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException ex) {
                        closeProgressDialog();
                        if (!mIsDestroyed) {
                            if (ex instanceof FirebaseAuthException) {
                                FirebaseAuthError error = FirebaseAuthError.fromException((FirebaseAuthException) ex);
                                showSimpleDialog(error.getDescription());
                            } else {
                                showSimpleDialog(ex.getLocalizedMessage());
                            }
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        closeProgressDialog();
                        mVerificationId = verificationId;
                        mForceResendingToken = forceResendingToken;
                        if (!mIsDestroyed) {
                            //AuthenticationActivity.this.onCodeSent();
                            showSmsAlertDialog(phoneNumber, time);
                        }
                    }
                }, mForceResendingToken);
    }

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mForceResendingToken;

    private boolean mIsDestroyed = false;

    @Override
    protected void onDestroy() {
        mIsDestroyed = true;
        super.onDestroy();
    }

    private Dialog smsCodeDialog;

    private void showSmsAlertDialog(final String phoneNumber, long time) {

        if (smsCodeDialog == null) {
            smsCodeDialog = new Dialog(this);
            smsCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            smsCodeDialog.setContentView(R.layout.dialog_sms_code);
            Window window = smsCodeDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            }

            smsCodeDialog.setCanceledOnTouchOutside(false);
            smsCodeDialog.setCancelable(false);
        }

        if (!smsCodeDialog.isShowing())
            smsCodeDialog.show();

        final EditText smsCode = smsCodeDialog.findViewById(R.id.smsCode);
        final TextView description = smsCodeDialog.findViewById(R.id.description);
        description.setText(getString(R.string.phone_verification_message, phoneNumber));

        final TextView resendTxt = smsCodeDialog.findViewById(R.id.resendTxt);
        resendTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(phoneNumber);
            }
        });
        resendTxt.setEnabled(false);

        new CountDownTimer(time, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                resendTxt.setText(getString(R.string.do_not_get_sms_code_resend) + "\nplease wait: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                if (smsCodeDialog != null && smsCodeDialog.isShowing()) {
                    resendTxt.setText(R.string.do_not_get_sms_code_resend);
                    resendTxt.setEnabled(true);
                }
            }
        }.start();

        final ImageView okBtn, cancelBtn;
        okBtn = smsCodeDialog.findViewById(R.id.okBtn);
        cancelBtn = smsCodeDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String smsCodeString = smsCode.getText().toString();

                if (smsCodeString.length() != 6) {
                    smsCode.setError(getString(R.string.sms_code_empty_error));
                    return;
                }

                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId, smsCodeString);
                updateUserPhoneNumber(phoneAuthCredential);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileNumber.setText("");
                smsCodeDialog.dismiss();
            }
        });
    }

    private void updateUserPhoneNumber(PhoneAuthCredential phoneAuthCredential) {
        showProgressDialog();
        firebaseUser.updatePhoneNumber(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                closeProgressDialog();
                if (task.isSuccessful()) {
                    if (smsCodeDialog != null && smsCodeDialog.isShowing()) smsCodeDialog.dismiss();
                    mobileNumber.removeTextChangedListener(textWatcher);
                    mobileNumber.setText(firebaseUser.getPhoneNumber());
                    mobileNumber.setEnabled(false);
                    updateUserInfo();
                } else {
                    FirebaseException firebaseException = (FirebaseException) task.getException();
                    if (firebaseException != null) {
                        if (firebaseException instanceof FirebaseAuthException) {
                            FirebaseAuthError error = FirebaseAuthError.fromException((FirebaseAuthException) firebaseException);
                            showSimpleDialog(error.getDescription());
                        } else {
                            showSimpleDialog(firebaseException.getLocalizedMessage());
                        }
                    } else {
                        showSimpleDialog(getString(R.string.unknown_error));
                    }
                }
            }
        });
    }
}
