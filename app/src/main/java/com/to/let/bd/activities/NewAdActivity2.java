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
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.to.let.bd.utils.DateUtils;
import com.to.let.bd.utils.FirebaseAuthError;
import com.to.let.bd.utils.GoogleApiHelper;
import com.to.let.bd.utils.MyAnalyticsUtil;
import com.to.let.bd.utils.PhoneNumberUtils;
import com.to.let.bd.utils.UploadImageService;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class NewAdActivity2 extends BaseMapActivity implements View.OnClickListener, View.OnFocusChangeListener {

    public static final String TAG = NewAdActivity2.class.getSimpleName();

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

        if (!(firebaseUser.getPhoneNumber() == null || firebaseUser.getPhoneNumber().isEmpty())) {
            initPhone();
            phoneNumber.removeTextChangedListener(textWatcher);
            phoneNumber.setText(firebaseUser.getPhoneNumber());
            phoneNumber.setEnabled(false);
        }

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

                Bundle bundle = new Bundle();
                bundle.putString(MyAnalyticsUtil.keySearchType, MyAnalyticsUtil.placePickerNewAdView);
                bundle.putDouble(MyAnalyticsUtil.keySearchLat, place.getLatLng().latitude);
                bundle.putDouble(MyAnalyticsUtil.keySearchLng, place.getLatLng().longitude);
                bundle.putCharSequence(MyAnalyticsUtil.keySearchName, place.getName());
                myAnalyticsUtil.searchEvent(bundle);
            }

            @Override
            public void onError(Status status) {
                showLog("An error occurred: " + status.getStatusMessage());
            }
        });
    }

    private AdInfo adInfo;

    private void getAdInfo() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyNewAdEvent, "start");
        } else {
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyEditAdEvent, "start");
            adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
        }
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

        date = DateUtils.splittedDate(String.valueOf(adInfo.startingFinalDate));
        setRentDate(date);

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

        editMap.setVisibility(View.VISIBLE);
        editMap.setOnClickListener(this);
        googleMapLay.setClickable(true);
        placeAutocompleteView.setClickable(true);
    }

    private void completeMapImageUpload(int type, String adId, String[] imageContents) {
        updateDatabaseForMapImage(type, adId, imageContents);
    }

    private void updateDatabaseForMapImage(final int type, final String adId, final String[] imageContents) {
        HashMap<String, Object> mapImageValue = new HashMap<>();
        if (type == AppConstants.adMapImageType) {
            mapImageValue.put("/" + DBConstants.adList + "/" + flatType + "/" + adId + "/" + DBConstants.map + "/" + AppConstants.downloadUrl, imageContents[0]);
            mapImageValue.put("/" + DBConstants.adList + "/" + flatType + "/" + adId + "/" + DBConstants.map + "/" + AppConstants.imageName, imageContents[1]);
            mapImageValue.put("/" + DBConstants.adList + "/" + flatType + "/" + adId + "/" + DBConstants.map + "/" + AppConstants.imagePath, imageContents[2]);
            mapImageValue.put("/" + DBConstants.adList + "/" + flatType + "/" + adId + "/" + DBConstants.modifiedTime, ServerValue.TIMESTAMP);

            mapImageValue.put("/" + DBConstants.userAdList + "/" + getUid() + "/" + adId + "/" + DBConstants.map + "/" + AppConstants.downloadUrl, imageContents[0]);
            mapImageValue.put("/" + DBConstants.userAdList + "/" + getUid() + "/" + adId + "/" + DBConstants.map + "/" + AppConstants.imageName, imageContents[1]);
            mapImageValue.put("/" + DBConstants.userAdList + "/" + getUid() + "/" + adId + "/" + DBConstants.map + "/" + AppConstants.imagePath, imageContents[2]);
            mapImageValue.put("/" + DBConstants.userAdList + "/" + getUid() + "/" + adId + "/" + DBConstants.modifiedTime, ServerValue.TIMESTAMP);

            databaseReference.updateChildren(mapImageValue, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        mediaAlertDialog(adId);
                    } else {
                        updateDatabaseForMapImage(type, adId, imageContents);
                    }
                    closeProgressDialog();
                }
            });
        }
    }

    private void mediaAlertDialog(final String adId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (adInfo == null) builder.setTitle(R.string.ad_published_successfully);
        else builder.setTitle(R.string.ad_updated_successfully);

        builder.setIcon(R.mipmap.ic_launcher);
        if (adInfo == null) {
            builder.setMessage(R.string.your_ad_published_successfully_would_u_like);
        } else {
            if (adInfo.images != null) {
                builder.setMessage(R.string.your_ad_updated_successfully_would_u_like);
            } else {
                builder.setMessage(R.string.your_ad_published_successfully_would_u_like);
            }
        }

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyWantToAddMediaEvent, "true: " + adId);
                finish();
                startMediaActivity(adId);
            }
        });

        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyWantToAddMediaEvent, "false: " + adId);
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
        startActivityForResult(intent, AppConstants.addOrUpdateMedia);
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
                        completeMapImageUpload(type, adId, imageContents);
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
    private EditText emailAddress, phoneNumber;
    private LinearLayout flatAdditionalInfoLay, googleMapLay;
    private EditText houseInfo, whichFloor, description, totalRent, totalUtility;
    private TextInputLayout totalRentTIL;
    private ImageView fixedMarker;
    private ProgressBar locationLoaderProgressBar;
    private TextView editMap;
    private View placeAutocompleteView;

    private void init() {
        mapScrollView = findViewById(R.id.mapScrollView);

        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(this);

        addressDetails = findViewById(R.id.addressDetails);

        initEmail();
        initPhone();
//        phoneNumber.setText(AppSharedPrefs.getMobileNumber());
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                phoneNumber.setOnFocusChangeListener(this);
            } else {
                String phoneNumber = firebaseUser.getPhoneNumber();
                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    this.phoneNumber.setOnFocusChangeListener(this);
                } else {
                    this.phoneNumber.setText(phoneNumber);
                    this.phoneNumber.setEnabled(false);
                }
            }
        }

        phoneNumber.addTextChangedListener(textWatcher);

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
        totalRentTIL = findViewById(R.id.totalRentTIL);

        totalRentTIL.setHint(getString(R.string.rent_per_month));

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

        placeAutocompleteView = findViewById(R.id.placeAutocompleteView);
        googleMapLay = findViewById(R.id.googleMapLay);
        editMap = findViewById(R.id.editMap);
        editMap.setSelected(true);
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

    private void initPhone() {
        if (phoneNumber == null)
            phoneNumber = findViewById(R.id.phoneNumber);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == phoneNumber) {
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
            phoneNumber.setError(null);
            if (AppConstants.isMobileNumberValid(NewAdActivity2.this, phoneNumber)) {
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
                        sendCode("+88" + AppConstants.formatAsSimplePhoneNumber(phoneNumber.getText().toString()));
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

        int tabIndex = 1;
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
                totalRentTIL.setHint(getString(R.string.rent_per_month));
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
                    ((MessFlatAd) fragment).setListener(new MessFlatAd.MyListener() {
                        @Override
                        public void onItemSelect(String message) {
                            totalRentTIL.setHint(message);
                        }
                    });
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
                if (editMap.getVisibility() == View.VISIBLE && adInfo != null) {
                    addressDetails.setText(adInfo.fullAddress);
                    if (locationLoaderProgressBar.getVisibility() == View.VISIBLE)
                        locationLoaderProgressBar.setVisibility(View.GONE);
                    return;
                }
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
                if (editMap.getVisibility() == View.VISIBLE && adInfo != null) {
                    if (locationLoaderProgressBar.getVisibility() == View.VISIBLE)
                        locationLoaderProgressBar.setVisibility(View.GONE);
                    return;
                }
                locationLoaderProgressBar.setVisibility(View.VISIBLE);
            }
        });

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                NewAdActivity2.this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        });

        if (adInfo != null)
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(adInfo.latitude, adInfo.longitude), DEFAULT_ZOOM));
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
        if (adInfo == null)
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, DEFAULT_ZOOM));
        else {
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(adInfo.latitude, adInfo.longitude), DEFAULT_ZOOM));
        }
    }

    @Override
    public void onClick(View view) {
        if (submitBtn == view) {
            submitAd();
        } else if (rentDate == view) {
            showDatePickerDialog();
        } else if (editMap == view) {
            googleMapLay.setClickable(false);
            placeAutocompleteView.setClickable(false);
            placeAutocompleteView.setVisibility(View.GONE);
            editMap.setVisibility(View.GONE);
        }
    }

    private DatabaseReference databaseReference;

    private void submitAd() {
        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();

        validateInputtedData();
    }

    private void validateInputtedData() {
        houseInfo.setError(null);
        whichFloor.setError(null);
        totalRent.setError(null);
        emailAddress.setError(null);
        phoneNumber.setError(null);

        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "not registered");
            googleSignOut();
            return;
        }

        if (firebaseUser.getPhoneNumber() == null || firebaseUser.getPhoneNumber().isEmpty()) {
            if (!AppConstants.isMobileNumberValid(this, phoneNumber)) {
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "invalid phone number");
                return;
            }
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "no phone number");
            needToVerifyPhoneNumberAlert();
            return;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null && fragment instanceof OthersFlatAd && fragment.isVisible()) {
            String rentType = ((OthersFlatAd) fragment).getRentType();
            if (rentType.equals(getString(R.string.others)) && description.getText().length() <= 0) {
                description.setError(getString(R.string.error_field_required));
                description.requestFocus();
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "invalid description for other type ad");
                return;
            }
        } else if (fragment != null && fragment instanceof MessFlatAd && fragment.isVisible()) {
            if (!((MessFlatAd) fragment).isSeatOrRoomSelected()) {
                showSimpleDialog(R.string.please_select_valid_seat_or_room);
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "no room/seat for mess type ad");
                return;
            }
        }

        if (houseInfo.getText().length() == 0) {
            houseInfo.setError(getString(R.string.error_field_required));
            houseInfo.requestFocus();
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "no house name/number");
            return;
        }

        if (whichFloor.getText().length() == 0) {
            whichFloor.setError(getString(R.string.error_field_required));
            whichFloor.requestFocus();
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "no floor number");
            return;
        }

        if (totalRent.getText().length() == 0) {
            totalRent.setError(getString(R.string.error_field_required));
            totalRent.requestFocus();
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "no rent");
            return;
        }

        summary = null;
        othersFacility = null;

        getRoomDetails();

        if (summary == null || othersFacility == null) {
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "invalid summary or others facility");
            return;
        }

        AppSharedPrefs.setMobileNumber(phoneNumber.getText().toString());
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
        title.setText(getString(R.string.please_review_the_summary));

        TextView roomSummary = summaryDialog.findViewById(R.id.roomSummary);
        TextView address = summaryDialog.findViewById(R.id.address);
        TextView totalRent = summaryDialog.findViewById(R.id.totalRent);
        TextView roomOthersFacility = summaryDialog.findViewById(R.id.othersFacility);

        roomSummary.setText(summary);
        roomOthersFacility.setText(othersFacility);

        String addressString = addressDetails.getText().toString();
        addressString += "\nHouse number/name: " + houseInfo.getText().toString()
                + " Floor: " + whichFloor.getText().toString();

        if (description.getText().toString().trim().length() > 0)
            addressString += "\n" + description.getText().toString().trim();
        address.setText(addressString);

        long totalR = Long.parseLong(this.totalRent.getText().toString());
        String tr;
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null && fragment instanceof MessFlatAd && fragment.isVisible()) {
            MessInfo messInfo = ((MessFlatAd) fragment).getMessInfo();
            if (messInfo.numberOfSeat > 0) {
                tr = "Rent per seat: TK " + String.valueOf(AppConstants.rentFormatter(totalR));
            } else {
                if (messInfo.numberOfRoom == 1)
                    tr = "Rent for room: TK " + String.valueOf(AppConstants.rentFormatter(totalR));
                else
                    tr = "Rent for all room: TK " + String.valueOf(AppConstants.rentFormatter(totalR));
            }
        } else {
            tr = "Rent per month: TK " + String.valueOf(AppConstants.rentFormatter(totalR));
        }

        long totalU = totalUtility.getText().length() > 0 ? Long.parseLong(totalUtility.getText().toString()) : 0;
        if (totalU > 0) tr += "\nOthers utility bill: TK" + AppConstants.rentFormatter(totalU);
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
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "only show summary");
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

        int startingYear = date[0];
        int startingMonth = date[1];
        int startingDate = date[2];

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

        String mobileNumber = this.phoneNumber.getText().toString();
        String emailAddress = this.emailAddress.getText().toString();

        final AdInfo newAdInfo = new AdInfo(adId, startingMonth, startingDate, startingYear,
                selectedCenterLatLng.latitude, selectedCenterLatLng.longitude,
                fullAddress, country, division, district, subDistrict, knownAsArea,
                flatSpace, flatRent, othersFee,
                houseInfo.getText().toString(),
                whichFloor.getText().toString().trim().isEmpty() ? -1 : Integer.parseInt(whichFloor.getText().toString()),
                roomFaceArray[flatFaceSelection], description.getText().toString().trim(),
                flatTypesArray.get(tabLayout.getSelectedTabPosition()),
                familyInfo, messInfo, subletInfo, othersInfo,
                getUid(), mobileNumber, emailAddress);

        HashMap<String, Object> adValues = newAdInfo.toMap();
        if (adInfo == null) {
            adValues.put(DBConstants.createdTime, ServerValue.TIMESTAMP);
        } else {
            adValues.put(DBConstants.images, adInfo.images);
            adValues.put(DBConstants.map, adInfo.map);
            adValues.put(DBConstants.favCount, adInfo.favCount);
            adValues.put(DBConstants.fav, adInfo.fav);
            adValues.put(DBConstants.reportCount, adInfo.reportCount);
            adValues.put(DBConstants.report, adInfo.report);
        }

        adValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("/" + DBConstants.adList + "/" + flatType + "/" + adId, adValues);
        hashMap.put("/" + DBConstants.userAdList + "/" + getUid() + "/" + adId, adValues);

        databaseReference.updateChildren(hashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    if (editMap.getVisibility() != View.VISIBLE) {
                        googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(selectedCenterLatLng.latitude, selectedCenterLatLng.longitude))
                                .snippet("")
                                .title("")
                                .icon(BitmapDescriptorFactory.fromResource(getMarkerResource())));
                        googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap bitmap) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                                byte[] byteArray = stream.toByteArray();
                                uploadImage(AppConstants.adMapImageType, adId, byteArray);
                            }
                        });
                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference(DBConstants.geoFire + "/" + flatType);
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(adId, new GeoLocation(selectedCenterLatLng.latitude, selectedCenterLatLng.longitude));
                    } else {
                        mediaAlertDialog(adId);
                    }
                } else {
                    myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryAdEvent, "submission failed: " + databaseError.getDetails());
                    showSimpleDialog(databaseError.getMessage());
                    closeProgressDialog();
                }

                AdListActivity2.needToRefreshData = true;
                SubAdListActivity.needToRefreshData = true;
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

        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put(DBConstants.userId, firebaseUser.getUid());
        userValues.put(DBConstants.userEmail, firebaseUser.getEmail());
        userValues.put(DBConstants.userDisplayName, firebaseUser.getDisplayName());

        if (firebaseUser.getPhoneNumber() != null && !firebaseUser.getPhoneNumber().isEmpty())
            userValues.put(DBConstants.userPhoneNumber, firebaseUser.getPhoneNumber());

        if (firebaseUser.getPhotoUrl() != null)
            userValues.put(DBConstants.userProfilePic, firebaseUser.getPhotoUrl().toString());

        userValues.put(DBConstants.fcmToken, FirebaseInstanceId.getInstance().getToken());

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
                date[0] = year;
                date[1] = monthOfYear;
                date[2] = dayOfMonth;
                setRentDate(date);
            }
        }, date[0], date[1], date[2]);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setRentDate(int[] dateArray) {
        Date date = DateUtils.getDate(dateArray);
        String dateAsString = DateUtils.getFormattedDateString(date, DateUtils.format2);
        long elapsedDays = DateUtils.differenceBetweenToday(date.getTime());

        String remainingTime = "";
        if (elapsedDays <= 1) {
            remainingTime = elapsedDays + " day remaining.";
        } else {
            remainingTime = elapsedDays + " days remaining.";
        }

        rentDate.setText(dateAsString);
        this.remainingTime.setText(remainingTime);
    }

    private int[] getDefaultRentMonth() {
        Calendar calendar = Calendar.getInstance();
        int monthOfYear = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        date[0] = year;
        date[1] = monthOfYear;
        date[2] = 1;// 1st day of month

        return date;
    }

    private int[] date = new int[3];//0=year, 1=monthOfYear, 2=dayOfMonth (month start from 0)

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
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (adInfo == null)
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyNewAdEvent, "canceled");
        else
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyEditAdEvent, "canceled");
        super.onBackPressed();
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
                        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyPhoneNumberVerificationEvent, "succeed");
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

                        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyPhoneNumberVerificationEvent, "failed: " + ex.getMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyPhoneNumberVerificationEvent, "code sent");
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
                NewAdActivity2.this.phoneNumber.setText("");
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
                    myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyPhoneNumberAddedEvent, "succeed");
                    if (smsCodeDialog != null && smsCodeDialog.isShowing()) smsCodeDialog.dismiss();
                    phoneNumber.removeTextChangedListener(textWatcher);
                    phoneNumber.setText(firebaseUser.getPhoneNumber());
                    phoneNumber.setEnabled(false);
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
                    myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyPhoneNumberAddedEvent, "failed: " + task.getException().getMessage());
                }
            }
        });
    }
}
