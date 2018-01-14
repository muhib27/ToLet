package com.to.let.bd.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.to.let.bd.R;
import com.to.let.bd.adapters.SlidingImageAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.components.ImageViewZoomT;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.model.FamilyInfo;
import com.to.let.bd.model.ImageInfo;
import com.to.let.bd.model.MessInfo;
import com.to.let.bd.model.OthersInfo;
import com.to.let.bd.model.SubletInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.DateUtils;
import com.to.let.bd.utils.MyAnalyticsUtil;

import java.util.Date;
import java.util.HashMap;

public class AdDetailsActivity extends BaseActivity implements View.OnClickListener {

    public static boolean needToRefreshData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.ad_details);
        }

        needToRefreshData = false;

        myAnalyticsUtil = new MyAnalyticsUtil(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        getData();
        init();

        if (images != null && images.length > 0) {
            initSlider();
        }

        myAnalyticsUtil.showAdDetails(adInfo, getUid());
        initInterstitialAd();
    }

    private InterstitialAd interstitialAd;

    private void initInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.ad_mob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showLog("Code to be executed when an ad finishes loading.");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                showLog("Code to be executed when an ad request fails.");
            }

            @Override
            public void onAdOpened() {
                showLog("Code to be executed when the ad is displayed.");
            }

            @Override
            public void onAdLeftApplication() {
                showLog("Code to be executed when the user has left the app.");
            }

            @Override
            public void onAdClosed() {
                showLog("Code to be executed when when the interstitial ad is closed.");
            }
        });
    }

    private DatabaseReference databaseReference;
    private MyAnalyticsUtil myAnalyticsUtil;

    private AdInfo adInfo;
    private String flatType = null;

    private void getData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            images = new String[0];
            return;
        }
        adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
        if (adInfo == null || adInfo.images == null || adInfo.images.isEmpty()) {
            images = new String[0];
        } else {
            images = new String[adInfo.images.size()];
            for (int i = 0; i < adInfo.images.size(); i++) {
                ImageInfo imageInfo = adInfo.images.get(i);
                if (imageInfo != null) {
                    images[i] = adInfo.images.get(i).downloadUrl;
                }
            }
        }

        flatType = null;
        if (adInfo.familyInfo != null) {
            flatType = DBConstants.keyFamily;
        } else if (adInfo.messInfo != null) {
            flatType = DBConstants.keyMess;
        } else if (adInfo.subletInfo != null) {
            flatType = DBConstants.keySublet;
        } else if (adInfo.othersInfo != null) {
            flatType = DBConstants.keyOthers;
        }
    }

    private TextView rentDate, totalRent, roomDetails, addressDetails, rentType,
            othersFacility, othersFacilityDetails, reportThis, photoCount,
            privacyPolicy, imageAddOrEdit;
    private Button callBtn, emailBtn, editBtn;
    private LinearLayout showInMapView, reportLay, contactLay;
    private ImageView noImageView, favAd;

    private void init() {
        rentDate = findViewById(R.id.rentDate);
        totalRent = findViewById(R.id.totalRent);
        roomDetails = findViewById(R.id.roomSummary);
        addressDetails = findViewById(R.id.addressDetails);

        rentType = findViewById(R.id.rentType);
        othersFacility = findViewById(R.id.othersFacility);
        othersFacilityDetails = findViewById(R.id.othersFacilityDetails);

        reportLay = findViewById(R.id.reportLay);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyPolicy.setText(getString(R.string.privacy_policy_note, getString(R.string.terms_of_use), getString(R.string.privacy_policy)));
        reportThis = findViewById(R.id.reportThis);

        reportThis.setEnabled(true);

        if (adInfo.reportCount > 0) {
            if (adInfo.report.containsKey(BaseActivity.getUid())) {
                reportThis.setEnabled(false);
                reportThis.setText(R.string.already_reported);
            }
        }

        favAd = findViewById(R.id.favAd);
        favAd.setSelected(false);

        if (adInfo.favCount > 0) {
            if (adInfo.fav.containsKey(BaseActivity.getUid())) {
                favAd.setSelected(true);
            }
        }

        favAd.setOnClickListener(this);

        photoCount = findViewById(R.id.photoCount);
        imageAddOrEdit = findViewById(R.id.imageAddOrEdit);

        showInMapView = findViewById(R.id.showInMapView);
        contactLay = findViewById(R.id.contactLay);
        callBtn = findViewById(R.id.callBtn);
        emailBtn = findViewById(R.id.emailBtn);
        editBtn = findViewById(R.id.editBtn);

        if (adInfo.userId.equals(BaseActivity.getUid())) {
            editBtn.setVisibility(View.VISIBLE);
            contactLay.setVisibility(View.GONE);
            imageAddOrEdit.setVisibility(View.VISIBLE);
        }

        if (adInfo.userId.equals(BaseActivity.getUid())) {
            reportLay.setVisibility(View.GONE);
        }

        imageAddOrEdit.setOnClickListener(this);

        if (adInfo != null) {
            String totalRent = "TK " + AppConstants.rentFormatter(adInfo.flatRent);
            this.totalRent.setText(totalRent);
            this.roomDetails.setText(AppConstants.flatDescription(this, adInfo));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(adInfo.fullAddress);
            if (adInfo.flatDescription != null && !adInfo.flatDescription.isEmpty()) {
                stringBuilder.append("\n\n");
                stringBuilder.append(adInfo.flatDescription);
            }

            addressDetails.setText(stringBuilder);
            String[] splittedDate = DateUtils.splittedDate(adInfo.startingFinalDate);
            Date date = DateUtils.getDate(splittedDate, DateUtils.format4);

            String rentDate = DateUtils.getFormattedDateString(date, DateUtils.format2);
            long elapsedDays = DateUtils.differenceBetweenToday(date.getTime());
            setRentDate(rentDate, elapsedDays);
        }

        noImageView = findViewById(R.id.noImageView);
        othersFacilityDetails();
        noImageView.setVisibility(View.GONE);
        if (images != null && images.length > 0) {
            String photoCount = images.length > 1 ? images.length + " Photo's" : images.length + " Photos";
            this.photoCount.setText(photoCount);
        } else {
            noImageView.setVisibility(View.VISIBLE);
            if (adInfo.map != null && adInfo.map.downloadUrl != null) {
                Glide.with(this)
                        .load(Uri.parse(adInfo.map.downloadUrl))
                        .into(noImageView);
            }
        }

        showInMapView.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        emailBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        reportThis.setOnClickListener(this);
        noImageView.setOnClickListener(this);
    }

    private void onFavClicked() {
        DatabaseReference userAdRef = databaseReference.child(DBConstants.adList).child(adInfo.adId);
        userAdRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                AdInfo p = mutableData.getValue(AdInfo.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                if (p.fav.containsKey(getUid())) {
                    // Unstar the ad and remove self from stars
                    p.favCount = p.favCount - 1;
                    p.fav.remove(getUid());
                } else {
                    // Star the ad and add self to stars
                    p.favCount = p.favCount + 1;
                    p.fav.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    private void othersFacilityDetails() {
        StringBuilder stringBuilder = new StringBuilder();
        String rentType = "";
        if (adInfo.familyInfo != null) {
            FamilyInfo familyInfo = adInfo.familyInfo;
            rentType = getString(R.string.family);

            if (familyInfo.twentyFourWater) {
                stringBuilder.append(getString(R.string.twenty_four_water_facility));
                stringBuilder.append("\n");
            }
            if (familyInfo.gasSupply) {
                stringBuilder.append(getString(R.string.supply_gas_facility));
                stringBuilder.append("\n");
            }
            if (familyInfo.securityGuard) {
                stringBuilder.append(getString(R.string.always_security_guard));
                stringBuilder.append("\n");
            }

            if (familyInfo.parkingGarage) {
                stringBuilder.append(getString(R.string.parking_garage_facility));
                stringBuilder.append("\n");
            }
            if (familyInfo.lift) {
                stringBuilder.append(getString(R.string.lift_facility));
                stringBuilder.append("\n");
            }
            if (familyInfo.generator) {
                stringBuilder.append(getString(R.string.generator_facility));
                stringBuilder.append("\n");
            }
            if (familyInfo.wellFurnished) {
                stringBuilder.append(getString(R.string.fully_furnished));
                stringBuilder.append("\n");
            }
            if (familyInfo.kitchenCabinet) {
                stringBuilder.append(getString(R.string.have_a_kitchen_cabinet));
                stringBuilder.append("\n");
            }
        } else if (adInfo.messInfo != null) {
            MessInfo messInfo = adInfo.messInfo;

            rentType = getString(R.string.mess);

            if (messInfo.memberType == 1) {
                rentType += ", " + getString(R.string.only_female);
            } else {
                rentType += ", " + getString(R.string.only_male);
            }

            if (messInfo.mealFacility) {
                stringBuilder.append(getString(R.string.meal_facility));
                stringBuilder.append("\n");

                if (messInfo.mealRate > 0) {
                    stringBuilder.append(getString(R.string.approximate_meal_rate));
                    stringBuilder.append(" ");
                    stringBuilder.append(messInfo.mealRate);
                    stringBuilder.append("\n");
                }
            }
            if (messInfo.maidServant) {
                stringBuilder.append(getString(R.string.maid_servant));
                stringBuilder.append("\n");
            }
            if (messInfo.twentyFourWater) {
                stringBuilder.append(getString(R.string.twenty_four_water_facility));
                stringBuilder.append("\n");
            }
            if (messInfo.nonSmoker) {
                stringBuilder.append(getString(R.string.only_non_smoker));
                stringBuilder.append("\n");
            }
            if (messInfo.fridge) {
                stringBuilder.append(getString(R.string.have_fridge_facility));
                stringBuilder.append("\n");
            }
            if (messInfo.wifi) {
                stringBuilder.append(getString(R.string.have_wifi_facility));
                stringBuilder.append("\n");
            }
        } else if (adInfo.subletInfo != null) {
            SubletInfo subletInfo = adInfo.subletInfo;
            rentType = getString(R.string.sublet);
            if (subletInfo.twentyFourWater) {
                stringBuilder.append(getString(R.string.twenty_four_water_facility));
                stringBuilder.append("\n");
            }
            if (subletInfo.gasSupply) {
                stringBuilder.append(getString(R.string.supply_gas_facility));
                stringBuilder.append("\n");
            }
            if (subletInfo.generator) {
                stringBuilder.append(getString(R.string.generator_facility));
                stringBuilder.append("\n");
            }
            if (subletInfo.lift) {
                stringBuilder.append(getString(R.string.lift_facility));
                stringBuilder.append("\n");
            }
            if (subletInfo.wellFurnished) {
                stringBuilder.append(getString(R.string.fully_furnished));
                stringBuilder.append("\n");
            }
            if (subletInfo.kitchenShare) {
                stringBuilder.append(getString(R.string.need_to_share_kitchen));
                stringBuilder.append("\n");
            } else {
                stringBuilder.append(getString(R.string.no_need_to_share_kitchen));
                stringBuilder.append("\n");
            }
        } else if (adInfo.othersInfo != null) {
            OthersInfo othersInfo = adInfo.othersInfo;
            rentType = othersInfo.rentType;
            stringBuilder.append(othersInfo.rentType);
            stringBuilder.append("\n");

            if (othersInfo.lift) {
                stringBuilder.append(getString(R.string.lift_facility));
                stringBuilder.append("\n");
            }
            if (othersInfo.generator) {
                stringBuilder.append(getString(R.string.generator_facility));
                stringBuilder.append("\n");
            }
            if (othersInfo.securityGuard) {
                stringBuilder.append(getString(R.string.always_security_guard));
                stringBuilder.append("\n");
            }
            if (othersInfo.parkingGarage) {
                stringBuilder.append(getString(R.string.parking_garage_facility));
                stringBuilder.append("\n");
            }
            if (othersInfo.fullyDecorated) {
                stringBuilder.append(getString(R.string.interior_fully_decorated));
                stringBuilder.append("\n");
            }
            if (othersInfo.wellFurnished) {
                stringBuilder.append(getString(R.string.fully_furnished));
                stringBuilder.append("\n");
            }
        }

        if (stringBuilder.toString().trim().isEmpty()) {
            othersFacility.setVisibility(View.GONE);
            othersFacilityDetails.setVisibility(View.GONE);
        }

        this.rentType.setText(rentType);
        othersFacilityDetails.setText(stringBuilder);
    }

    @Override
    public void onClick(View view) {
        if (view == showInMapView) {
            openMapActivity(0);
        } else if (view == callBtn) {
            callTheUser();
        } else if (view == emailBtn) {
            mailTheUser();
        } else if (view == reportThis) {
            showReportAlert();
        } else if (view == favAd) {
//            if (adInfo.userId.equals(BaseActivity.getUid())) {
//                showToast(R.string.this_is_your_own_post);
//                return;
//            }
            needToRefreshData = true;
            favAd.setSelected(!favAd.isSelected());
            onFavClicked();
        } else if (view == editBtn) {
            editAd();
        } else if (view == noImageView) {
            if (noImageView.getVisibility() == View.VISIBLE && adInfo.map != null && adInfo.map.downloadUrl != null) {
                showInMapView.performClick();
            }
        } else if (view == imageAddOrEdit) {
            startMediaActivity();
        }
    }

    private void startMediaActivity() {
        if (adInfo == null)
            return;

        Intent intent = new Intent(this, MediaActivity.class);
        if (adInfo.images != null && adInfo.images.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(AppConstants.keyImageList, adInfo.images);
            intent.putExtras(bundle);
        }

        intent.putExtra(DBConstants.adId, adInfo.adId);
        intent.putExtra(DBConstants.flatType, flatType);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void callTheUser() {
        String mobileNumber = adInfo.mobileNumber;
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            showToast(R.string.mobile_number_not_found);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNumber, null));
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    private void mailTheUser() {
        String emailAddress = adInfo.emailAddress;
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            showToast(R.string.email_address_not_found);
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Interested in your ad");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello dear, I would like to inform you that the ");
        startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), 2);
    }

    private void showReportAlert() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Please select anyone");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.ad_report_type_array));
//
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                onReportClicked(strName);
                needToRefreshData = true;
            }
        });
        builderSingle.show();
    }

    private String[] images;

    private void initSlider() {
        ViewPager pager = findViewById(R.id.pager);

        SlidingImageAdapter slidingImageAdapter = new SlidingImageAdapter(this, images, new SlidingImageAdapter.ImageClickListener() {
            @Override
            public void imageClick(int position) {
                showImageDialog(position);
            }
        });

        pager.setAdapter(slidingImageAdapter);
    }

    private void setRentDate(String date, long elapsedDays) {
        if (elapsedDays <= 1) {
            date = date + "\n" + elapsedDays + " day remaining";
        } else {
            date = date + "\n" + elapsedDays + " days remaining";
        }
        rentDate.setText(date);
    }

    private void editAd() {
        Intent editIntent = new Intent(this, NewAdActivity2.class);
        editIntent.putExtra(AppConstants.keyAdInfo, adInfo);
        startActivity(editIntent);
    }

    private void openMapActivity(int typePosition) {
        if (adInfo == null)
            return;

        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.putExtra(AppConstants.keyAdInfo, adInfo);
        startActivity(mapIntent);
    }

    private Dialog imageDialog;

    private void showImageDialog(final int imagePosition) {
        if (imageDialog == null) {
            imageDialog = new Dialog(this);
            imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            imageDialog.setContentView(R.layout.dialog_image);
            Window window = imageDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            }
        }

        if (!imageDialog.isShowing())
            imageDialog.show();

        final ImageView crossButton = imageDialog.findViewById(R.id.crossButton);
        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageDialog.dismiss();
            }
        });

        final ImageViewZoomT zoomableImageView = imageDialog.findViewById(R.id.zoomableImageView);

        if (images[imagePosition] != null)
            Glide.with(this)
                    .load(Uri.parse(images[imagePosition]))
                    .into(zoomableImageView);
        else
            zoomableImageView.setImageResource(R.drawable.dummy_flat_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ad_details_activity, menu);
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
            case R.id.deleteAction:
                if (adInfo.userId.equals(BaseActivity.getUid()))
                    showDeleteAdDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteMenuItem = menu.findItem(R.id.deleteAction);
        if (deleteMenuItem != null) {
            deleteMenuItem.setVisible(false);
            if (adInfo.userId.equals(BaseActivity.getUid()))
                deleteMenuItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private Dialog deleteAdDialog;

    private void showDeleteAdDialog() {
        deleteAdDialog = new Dialog(this);
        deleteAdDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteAdDialog.setContentView(R.layout.dialog_delete_ad);
        Window window = deleteAdDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        }

        deleteAdDialog.setCanceledOnTouchOutside(true);
        deleteAdDialog.setCancelable(true);
        deleteAdDialog.show();

        final RadioGroup deleteReason = deleteAdDialog.findViewById(R.id.deleteReason);

        final ImageView okBtn, cancelBtn;
        okBtn = deleteAdDialog.findViewById(R.id.okBtn);
        cancelBtn = deleteAdDialog.findViewById(R.id.cancelBtn);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int deleteReasonValue = 0;
                if (deleteReason.getCheckedRadioButtonId() == R.id.notInterestedNow) {
                    deleteReasonValue = 1;
                } else if (deleteReason.getCheckedRadioButtonId() == R.id.hideNowOnly) {
                    deleteReasonValue = 2;
                } else if (deleteReason.getCheckedRadioButtonId() == R.id.justDelete) {
                    deleteReasonValue = 3;
                }

                deleteAd(deleteReasonValue);
                deleteAdDialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAdDialog.dismiss();
            }
        });

    }

    private void deleteAd(int deleteReasonValue) {
        showProgressDialog();

        if (flatType == null || adInfo.adId == null) {
            closeProgressDialog();
            return;
        }

        HashMap<String, Object> adValues = new HashMap<>();
        adValues.put(DBConstants.deleteReason, deleteReasonValue);
        adValues.put(DBConstants.isActive, false);
        adValues.put(DBConstants.modifiedTime, ServerValue.TIMESTAMP);

        databaseReference
                .child(DBConstants.adList)
                .child(flatType)
                .child(adInfo.adId)
                .updateChildren(adValues, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        closeProgressDialog();
                    }
                });
    }

    // [START ad_stars_transaction]
    private void onReportClicked(final String reportType) {
        DatabaseReference userAdRef = databaseReference.child(DBConstants.adList).child(adInfo.adId);
        userAdRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                AdInfo p = mutableData.getValue(AdInfo.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                if (!p.report.containsKey(getUid())) {
                    p.reportCount = p.reportCount + 1;
                    p.report.put(getUid(), reportType);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError == null) {
                    reportThis.setEnabled(false);
                }
            }
        });
    }
}
