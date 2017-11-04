package com.to.let.bd.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseMapActivity;
import com.to.let.bd.fragments.FamilyFragment;
import com.to.let.bd.fragments.MessFragment;
import com.to.let.bd.fragments.OthersFragment;
import com.to.let.bd.fragments.SubletFragment;
import com.to.let.bd.utils.ActivityUtils;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.AppSharedPrefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class NewAdActivity2 extends BaseMapActivity implements View.OnClickListener {

    private static final String TAG = NewAdActivity2.class.getSimpleName();

    @Override
    public int getLayoutResourceId() {
        return R.layout.activity_new_post2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String getActivityTitle() {
        return getString(R.string.post_your_ad);
    }

    @Override
    protected void setEmailAddress() {
        firebaseUser.getUid();
    }

    @Override
    public void onCreate() {
        init();
        initTabLayout();
        addRoomFaceType(null);
    }

    private Button submitBtn;
    private EditText addressDetails;
    private EditText emailAddress, mobileNumber;
    private LinearLayout flatAdditionalInfoLay;
    private EditText whichFloor, houseInfo, totalRent, totalUtility;
    private TextView utilityBdt;

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
//        addressDetails.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_TOP = 1;
//                final int DRAWABLE_RIGHT = 2;
//                final int DRAWABLE_BOTTOM = 3;
//
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (event.getRawX() >= (addressDetails.getRight() - addressDetails.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        addressDetails.setText("test");
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });

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

//        if (firebaseUser != null) {
//            if (firebaseUser.isAnonymous()) {
//                emailAddress.setOnFocusChangeListener(this);
//            } else {
//                String email = firebaseUser.getEmail();
//                if (email == null || email.isEmpty()) {
//                    emailAddress.setOnFocusChangeListener(this);
//                } else {
//                    emailAddress.setText(email);
//                    emailAddress.setEnabled(false);
//                }
//            }
//        }

        flatAdditionalInfoLay = findViewById(R.id.flatAdditionalInfoLay);
        houseInfo = findViewById(R.id.houseInfo);
        whichFloor = findViewById(R.id.whichFloor);
        totalRent = findViewById(R.id.totalRent);
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

        rentDate = findViewById(R.id.rentDate);
        rentDate.setOnClickListener(this);
    }

    private void addRoomFaceType(ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View inflatedView = inflater.inflate(R.layout.row_perticular_view, viewGroup, false);
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
//        familyRoom[3] = defaultSelection;
//        addParticularView();
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
//                        familyRoom[3] = i;
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

    private TextView rentDate;
    private TabLayout tabLayout;

    private void initTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.family)), false);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.mess_member)), false);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.sublet)), false);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.others)), false);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                String tag;
                if (tab.getPosition() == 0) {
                    fragment = FamilyFragment.newInstance();
                    tag = FamilyFragment.TAG;
                } else if (tab.getPosition() == 1) {
                    fragment = MessFragment.newInstance();
                    tag = MessFragment.TAG;
                } else if (tab.getPosition() == 2) {
                    fragment = SubletFragment.newInstance();
                    tag = SubletFragment.TAG;
                } else {
                    fragment = OthersFragment.newInstance();
                    tag = OthersFragment.TAG;
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

    @Override
    public void onMapReady2(GoogleMap googleMap) {
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

    @Override
    public void onLoadLocationDetails(String fullAddress) {
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

        AppSharedPrefs.setMobileNumber(mobileNumber.getText().toString());
        viewSummaryDialog();
    }

    private String getRoomDetails() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            if (fragment instanceof FamilyFragment && fragment.isVisible()) {
                return ((FamilyFragment) fragment).getRoomDetails();
            } else if (fragment instanceof MessFragment && fragment.isVisible()) {
                return ((MessFragment) fragment).getRoomDetails();
            } else if (fragment instanceof SubletFragment && fragment.isVisible()) {
                return ((SubletFragment) fragment).getRoomDetails();
            } else if (fragment instanceof OthersFragment && fragment.isVisible()) {
                return ((OthersFragment) fragment).getRoomDetails();
            }
        }
        return null;
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

        roomDetails.setText(getRoomDetails());

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
//        showProgressDialog();
//        adId = mDatabase.child(DBConstants.adList).push().getKey();
//        final AdInfo adInfo = new AdInfo(adId, date[0], (date[1] + 1), date[2], rentLatitude, rentLongitude,
//                addressDetails.getText().toString(), "", "", "", "", "",
//                1, familyRoom[0], familyRoom[1], familyRoom[2], familyRoom[3], 1,
//                houseInfo.getText().toString(),
//                whichFloor.getText().length() == 0 ? -1 : Integer.parseInt(whichFloor.getText().toString()),
//                drawingDining.getCheckedRadioButtonId() == R.id.drawingDiningYes ? 1 : 0,
//                electricityCB.isChecked() ? 1 : 0, gasCB.isChecked() ? 1 : 0, waterCB.isChecked() ? 1 : 0,
//                liftCB.isChecked() ? 1 : 0, generatorCB.isChecked() ? 1 : 0, securityGuardCB.isChecked() ? 1 : 0,
//                totalSpace.getText().length() == 0 ? -1 : Long.parseLong(totalSpace.getText().toString()),
//                Long.parseLong(totalRent.getText().toString()),
//                utilityBill.getCheckedRadioButtonId() == R.id.utilityBillIncluded || totalUtility.getText().length() > 0 ?
//                        Long.parseLong(totalUtility.getText().toString()) : 0,
//                getUid());
//
//        //AdInfo adInfo = new AdInfo(adId, getUid());
//        HashMap<String, Object> adValues = adInfo.toMap();
//        adValues.put(DBConstants.createdTime, ServerValue.TIMESTAMP);
//        adValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);
//
////        showLog("server time: " + ServerValue.TIMESTAMP + " device time: " + System.currentTimeMillis());
//
//        HashMap<String, Object> childUpdates = new HashMap<>();
//        childUpdates.put("/" + DBConstants.adList + "/" + adId, adValues);
//        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                if (databaseError == null) {
//                    if (selectedLocation != null) {
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation.getPosition(), DEFAULT_ZOOM));
//                        if (selectedLocation.isInfoWindowShown()) {
//                            selectedLocation.hideInfoWindow();
//                        }
//                    }
//                    if (googleMap.isMyLocationEnabled()) {
//                        if (!(ActivityCompat.checkSelfPermission(NewAdActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewAdActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//                            googleMap.setMyLocationEnabled(false);
//                        }
//                    }
//                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//                        @Override
//                        public void onMapLoaded() {
//                            googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
//                                @Override
//                                public void onSnapshotReady(Bitmap bitmap) {
//                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
//                                    byte[] byteArray = stream.toByteArray();
//                                    uploadImage(AppConstants.adMapImageType, adId, byteArray);
//                                }
//                            });
//                        }
//                    });
//                } else {
//                    showToast(databaseError.getMessage());
//                    closeProgressDialog();
//                }
//                showLog();
//            }
//        });
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

    public void updateCalculatedRent(long calculatedRent) {
        totalRent.setText(String.valueOf(calculatedRent));
    }
}
