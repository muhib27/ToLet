package com.to.let.bd.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseMapActivity;
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
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.AppSharedPrefs;
import com.to.let.bd.utils.UploadImageService;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class NewAdActivity2 extends BaseMapActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private static final String TAG = NewAdActivity2.class.getSimpleName();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_new_post2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getActivityTitle() {
        return getString(R.string.post_your_ad);
    }

    @Override
    protected void setEmailAddress() {
        firebaseUser.getUid();
    }

    @Override
    protected void onCreate() {
        init();
        initTabLayout();
        addRoomFaceType(null);
        setRentDate(getDefaultRentMonth());

        initBroadcast();
    }

    private void initBroadcast() {
        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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

    private void completeSingleImageUpload(int type, String adId, String[] imageContents) {
        updateDatabase(type, adId, imageContents);
    }

    private void updateDatabase(int type, String adId, String[] imageContents) {
        HashMap<String, Object> adValues = new HashMap<>();
        adValues.put(AppConstants.downloadUrl, imageContents[0]);
        adValues.put(AppConstants.imageName, imageContents[1]);
        adValues.put(AppConstants.imagePath, imageContents[2]);

        HashMap<String, Object> childUpdates = new HashMap<>();
        if (type == AppConstants.adMapImageType) {
            childUpdates.put("/" + DBConstants.adList + "/" + adId + "/" + DBConstants.map, adValues);
            childUpdates.put("/" + DBConstants.adList + "/" + adId + "/" + DBConstants.modifiedTime, ServerValue.TIMESTAMP);
        }
        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                closeProgressDialog();
            }
        });
    }


    protected BroadcastReceiver mBroadcastReceiver;

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

    private void updateTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            updateTitle(getString(R.string.post_your_ad));
        }

        submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(this);

        addressDetails = findViewById(R.id.addressDetails);
        addressDetails.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (addressDetails.getRight() - addressDetails.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (addressDetails.getText().toString().isEmpty()) {
                            addressDetails.setText(fullAddress);
                            addressDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cross, 0);
                        } else {
                            addressDetails.setText("");
                            addressDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.undo, 0);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

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
                    mobileNumber.requestFocus();
                    return true;
                }
                return false;
            }
        });

        rentDate = findViewById(R.id.rentDate);
        rentDate.setOnClickListener(this);

        remainingTime = findViewById(R.id.remainingTime);
    }

    @Override
    public void onFocusChange(View view, boolean b) {

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
            AppConstants.isMobileNumberValid(NewAdActivity2.this, mobileNumber);
        }
    };

    private TextView rentDate, remainingTime;
    private TabLayout tabLayout;
    private ArrayList<String> flatTypes = new ArrayList<>();

    private void initTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);

        flatTypes.clear();
        flatTypes.add(getString(R.string.family));
        flatTypes.add(getString(R.string.mess_member));
        flatTypes.add(getString(R.string.sublet));
        flatTypes.add(getString(R.string.others));

        tabLayout.addTab(tabLayout.newTab().setText(flatTypes.get(0)), false);
        tabLayout.addTab(tabLayout.newTab().setText(flatTypes.get(1)), false);
        tabLayout.addTab(tabLayout.newTab().setText(flatTypes.get(2)), false);
        tabLayout.addTab(tabLayout.newTab().setText(flatTypes.get(3)), false);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                String tag;
                if (tab.getPosition() == 0) {
                    fragment = FamilyFlatAd.newInstance();
                    tag = FamilyFlatAd.TAG;
                } else if (tab.getPosition() == 1) {
                    fragment = MessFlatAd.newInstance();
                    tag = MessFlatAd.TAG;
                } else if (tab.getPosition() == 2) {
                    fragment = SubletFlatAd.newInstance();
                    tag = SubletFlatAd.TAG;
                } else {
                    fragment = OthersFlatAd.newInstance();
                    tag = OthersFlatAd.TAG;
                }
                updateTitle(getString(R.string.post_your_ad) + " (" + tab.getText() + ") ");
                ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                        fragment, R.id.fragmentContainer, tag);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.getTabAt(0).select();
    }

    private GoogleMap googleMap;

    @Override
    protected void onMapReady2(GoogleMap googleMap) {
        this.googleMap = googleMap;
//        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//
//            }
//        });
//
//        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//            @Override
//            public void onMapLongClick(LatLng latLng) {
//
//            }
//        });
    }

    private String fullAddress = "";

    @Override
    protected void onLoadLocationDetails(String fullAddress) {
        this.fullAddress = fullAddress;
        addressDetails.setText(fullAddress);
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

        if (firebaseUser == null || firebaseUser.isAnonymous()) {
            googleSignOut();
            return;
        }

        if (!AppConstants.isMobileNumberValid(this, mobileNumber)) {
            return;
        }
        String summary = getRoomDetails();
        if (summary == null) {
            return;
        }

        AppSharedPrefs.setMobileNumber(mobileNumber.getText().toString());
        viewSummaryDialog(summary);
    }

    private String getRoomDetails() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            if (fragment instanceof FamilyFlatAd && fragment.isVisible()) {
                return ((FamilyFlatAd) fragment).getRoomDetails();
            } else if (fragment instanceof MessFlatAd && fragment.isVisible()) {
                return ((MessFlatAd) fragment).getRoomDetails();
            } else if (fragment instanceof SubletFlatAd && fragment.isVisible()) {
                return ((SubletFlatAd) fragment).getRoomDetails();
            } else if (fragment instanceof OthersFlatAd && fragment.isVisible()) {
                return ((OthersFlatAd) fragment).getRoomDetails();
            }
        }
        return null;
    }

    private void viewSummaryDialog(String summary) {
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

        TextView roomDetails = summaryDialog.findViewById(R.id.roomDetails);
        TextView address = summaryDialog.findViewById(R.id.address);
        TextView totalRent = summaryDialog.findViewById(R.id.totalRent);

        roomDetails.setText(summary);

        address.setText(addressDetails.getText());

        long totalR = Long.parseLong(this.totalRent.getText().toString());
        long totalU = totalUtility.getText().length() > 0 ? Long.parseLong(totalUtility.getText().toString()) : 0;
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        adId = null;
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

    private String adId = null;

    private void writeNewPost() {
        showProgressDialog();
        adId = mDatabase.child(DBConstants.adList).push().getKey();

        int startingMonth = date[0];
        int startingDate = date[1] + 1;
        int startingYear = date[2];

        double latitude = rentLatitude;
        double longitude = rentLongitude;

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
        } else if (fragment != null && fragment instanceof MessFlatAd && fragment.isVisible()) {
            messInfo = ((MessFlatAd) fragment).getMessInfo();
        } else if (fragment != null && fragment instanceof SubletFlatAd && fragment.isVisible()) {
            subletInfo = ((SubletFlatAd) fragment).getSubletInfo();
        } else if (fragment != null && fragment instanceof OthersFlatAd && fragment.isVisible()) {
            othersInfo = ((OthersFlatAd) fragment).getOthersInfo();
        }

        final AdInfo adInfo = new AdInfo(adId, startingMonth, startingDate, startingYear,
                latitude, longitude,
                fullAddress, country, division, district, subDistrict, knownAsArea,
                flatSpace, flatRent, othersFee,
                houseInfo.getText().toString(),
                whichFloor.getText().toString().trim().isEmpty() ? -1 : Integer.parseInt(whichFloor.getText().toString()),
                roomFaceArray[flatFaceSelection], description.getText().toString(),
                flatTypes.get(tabLayout.getSelectedTabPosition()),
                familyInfo, messInfo, subletInfo, othersInfo, getUid());

//        AdInfo adInfo = new AdInfo(adId, getUid());
        HashMap<String, Object> adValues = adInfo.toMap();
        adValues.put(DBConstants.createdTime, ServerValue.TIMESTAMP);
        adValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);

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
                        if (!(ActivityCompat.checkSelfPermission(NewAdActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewAdActivity2.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
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

    private int[] date = new int[3];//0=dayOfMonth, 1=monthOfYear, 2=year

    public void updateCalculatedRent(long calculatedRent) {
//        totalRent.setText(String.valueOf(calculatedRent));
//        totalRent.setText("");
    }

    public void focusDescription() {
        description.requestFocus();
    }
}
