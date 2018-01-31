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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.database.ValueEventListener;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

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
        }

        updateTitle(getString(R.string.ad_details));

        needToRefreshData = false;

        myAnalyticsUtil = new MyAnalyticsUtil(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        getData();
        init();
        initSlider();
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
            images = new ArrayList<>();
            images.clear();
            return;
        }
        adInfo = (AdInfo) bundle.getSerializable(AppConstants.keyAdInfo);
        if (adInfo == null)
            return;

        processImageList();

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

    private void processImageList() {
        images = new ArrayList<>();
        images.clear();
        if (!(adInfo.images == null || adInfo.images.isEmpty())) {
            SortedSet<String> keys = new TreeSet<>(adInfo.images.keySet());
            for (String key : keys) {
                ImageInfo imageInfo = adInfo.images.get(key);
                if (imageInfo != null) {
                    images.add(imageInfo.downloadUrl);
                }
            }
        }
    }

    private TextView rentDate, totalRent, roomDetails, addressDetails, houseInfo,
            rentType, othersFacility, othersFacilityDetails,
            reportThis, photoCount, privacyPolicy, imageAddOrEdit;
    private Button callBtn, emailBtn, editBtn;
    private LinearLayout showInMapView, reportLay, contactLay;
    private ImageView noImageView, favAd;

    private void init() {
        rentDate = findViewById(R.id.rentDate);
        totalRent = findViewById(R.id.totalRent);
        roomDetails = findViewById(R.id.roomSummary);
        addressDetails = findViewById(R.id.addressDetails);
        addressDetails.setOnClickListener(this);
        houseInfo = findViewById(R.id.houseInfo);

        rentType = findViewById(R.id.rentType);
        othersFacility = findViewById(R.id.othersFacility);
        othersFacilityDetails = findViewById(R.id.othersFacilityDetails);

        reportLay = findViewById(R.id.reportLay);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyPolicy.setText(getString(R.string.privacy_policy_note, getString(R.string.terms_of_use), getString(R.string.privacy_policy)));
        reportThis = findViewById(R.id.reportThis);

        favAd = findViewById(R.id.favAd);

        favAd.setOnClickListener(this);

        photoCount = findViewById(R.id.photoCount);
        imageAddOrEdit = findViewById(R.id.imageAddOrEdit);
        imageAddOrEdit.setSelected(true);

        showInMapView = findViewById(R.id.showInMapView);
        contactLay = findViewById(R.id.contactLay);
        callBtn = findViewById(R.id.callBtn);
        emailBtn = findViewById(R.id.emailBtn);
        editBtn = findViewById(R.id.editBtn);
        noImageView = findViewById(R.id.noImageView);

        showInMapView.setOnClickListener(this);
        callBtn.setOnClickListener(this);
        emailBtn.setOnClickListener(this);
        editBtn.setOnClickListener(this);
        reportThis.setOnClickListener(this);
        noImageView.setOnClickListener(this);

        updateFullView();
    }

    private void onFavClicked() {
        if (getUid() == null)
            return;

        DatabaseReference userFavAdRef = databaseReference
                .child(DBConstants.userFavAdList)
                .child(getUid())
                .child(adInfo.adId);

        if (favAd.isSelected()) {
            favAd.setSelected(false);
            userFavAdRef.removeValue();
        } else {
            favAd.setSelected(true);
            AdInfo tmpAdInfo = adInfo;
            tmpAdInfo.favCount++;
            if (tmpAdInfo.fav == null) {
                tmpAdInfo.fav = new HashMap<>();
            }
            tmpAdInfo.fav.put(getUid(), true);
            userFavAdRef.setValue(tmpAdInfo);
        }

        DatabaseReference globalAdRef = databaseReference.child(DBConstants.adList).child(flatType).child(adInfo.adId);
        DatabaseReference userAdRef = databaseReference.child(DBConstants.userAdList).child(getUid()).child(adInfo.adId);

        // Run two transactions
        onFavClicked(globalAdRef);
        onFavClicked(userAdRef);
    }

    // [START ad_fav_transaction]
    private void onFavClicked(DatabaseReference adRef) {
        favAd.setEnabled(false);
        adRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                AdInfo adInfo = mutableData.getValue(AdInfo.class);
                if (adInfo == null) {
                    return Transaction.success(mutableData);
                }
                if (adInfo.fav.containsKey(getUid())) {
                    // UnFav the ad and remove self from stars
                    adInfo.favCount = adInfo.favCount - 1;
                    adInfo.fav.remove(getUid());
                } else {
                    // Fav the ad and add self to stars
                    adInfo.favCount = adInfo.favCount + 1;
                    adInfo.fav.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(adInfo);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                favAd.setEnabled(true);
            }
        });
    }
    // [END ad_fav_transaction]

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
            String[] subletTypeArray = getResources().getStringArray(R.array.sublet_type_array);
            rentType = getString(R.string.sublet) + ": "
                    + (subletInfo.subletType >= 3 ? subletInfo.subletTypeOthers :
                    subletTypeArray[subletInfo.subletType]);

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
            openMapActivity();
        } else if (view == callBtn) {
            phoneCall();
        } else if (view == emailBtn) {
            sendEmail();
        } else if (view == reportThis) {
            showReportAlert();
        } else if (view == favAd) {
            onFavClicked();
        } else if (view == editBtn) {
            editAd();
        } else if (view == noImageView) {
            if (noImageView.getVisibility() == View.VISIBLE) {
                if (adInfo.map != null && adInfo.map.downloadUrl != null) {
                    showInMapView.performClick();
                }
            }
        } else if (view == imageAddOrEdit) {
            startMediaActivity();
        } else if (view == addressDetails) {
            showInMapView.performClick();
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
        startActivityForResult(intent, AppConstants.addOrUpdateMedia);
    }

    private void phoneCall() {
        String mobileNumber = adInfo.mobileNumber;
        if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
            showSimpleDialog(R.string.mobile_number_not_found);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNumber, null));
        startActivityForResult(intent, AppConstants.phoneCall);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.addOrUpdateMedia) {
            if (resultCode == RESULT_OK) {
                reloadAdDataFromServer(onlyUpdateImageView);
            }
        } else if (requestCode == AppConstants.editAd) {
            if (resultCode == RESULT_OK) {
                reloadAdDataFromServer(updateFullView);
            }
        } else if (requestCode == AppConstants.phoneCall || requestCode == AppConstants.sendEmail) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
                initInterstitialAd();
            } else {
                initInterstitialAd();
            }
        }
    }

    private final int onlyUpdateImageView = 1;
    private final int updateFullView = 100;

    private void reloadAdDataFromServer(final int type) {
        if (flatType == null || flatType.isEmpty())
            return;

        if (adInfo == null || adInfo.adId == null || adInfo.adId.isEmpty())
            return;

        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference
                .child(DBConstants.adList)
                .child(flatType)
                .child(adInfo.adId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        AdInfo newAdInfo = dataSnapshot.getValue(AdInfo.class);
                        if (newAdInfo != null) {
                            adInfo = newAdInfo;

                            if (type == onlyUpdateImageView) {
                                processImageList();
                                updateImageSingleView();
                                initSlider();
                            } else {
                                updateFullView();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateFullView() {
        reportThis.setEnabled(true);
        if (adInfo.reportCount > 0) {
            if (adInfo.report.containsKey(BaseActivity.getUid())) {
                reportThis.setEnabled(false);
                reportThis.setText(R.string.already_reported);
            }
        }

        favAd.setSelected(false);
        if (adInfo.favCount > 0) {
            if (adInfo.fav.containsKey(BaseActivity.getUid())) {
                favAd.setSelected(true);
            }
        }

        imageAddOrEdit.setVisibility(View.INVISIBLE);
        if (adInfo.userId.equals(BaseActivity.getUid())) {
            editBtn.setVisibility(View.VISIBLE);
            contactLay.setVisibility(View.GONE);
            imageAddOrEdit.setVisibility(View.VISIBLE);
            imageAddOrEdit.setOnClickListener(this);
        }

        if (adInfo.userId.equals(BaseActivity.getUid())) {
            reportLay.setVisibility(View.GONE);
        }

        if (adInfo != null) {
            String totalRent = "TK " + String.valueOf(AppConstants.rentFormatter(adInfo.flatRent));
            if (adInfo.othersFee > 0)
                totalRent += " (+Utility TK " + AppConstants.rentFormatter(adInfo.othersFee) + ")";
            this.totalRent.setText(totalRent);

            this.roomDetails.setText(AppConstants.flatDescription(this, adInfo));
            addressDetails.setText(adInfo.fullAddress);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("House number/name: ")
                    .append(adInfo.houseNameOrNumber)
                    .append(" Floor: ")
                    .append(adInfo.floorNumber);
            if (adInfo.flatDescription != null && !adInfo.flatDescription.isEmpty()) {
                stringBuilder.append("\n\n");
                stringBuilder.append(adInfo.flatDescription);
            }

            houseInfo.setText(stringBuilder.toString());

            String[] dateArray = DateUtils.splittedDate(adInfo.startingFinalDate);
            Date date = DateUtils.getDate(dateArray, DateUtils.format4);
            String dateAsString = DateUtils.getFormattedDateString(date, DateUtils.format2);
            long elapsedDays = DateUtils.differenceBetweenToday(date.getTime());

            setRentDate(dateAsString, elapsedDays);
        }

        othersFacilityDetails();
        processImageList();
        updateImageSingleView();
        initSlider();
    }

    private void updateImageSingleView() {
        noImageView.setVisibility(View.GONE);
        if (images != null && !images.isEmpty()) {
            String photoCount = images.size() > 1 ? images.size() + " Photo's" : images.size() + " Photos";
            this.photoCount.setText(photoCount);
        } else {
            noImageView.setVisibility(View.VISIBLE);
            if (adInfo.map != null && adInfo.map.downloadUrl != null) {
                Glide.with(this)
                        .load(Uri.parse(adInfo.map.downloadUrl))
                        .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).error(R.mipmap.ic_launcher))
                        .into(noImageView);
            }
        }
    }

    private void sendEmail() {
        String emailAddress = adInfo.emailAddress;
        if (emailAddress == null || emailAddress.trim().isEmpty()) {
            showSimpleDialog(R.string.email_address_not_found);
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Interested in your ad");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello dear, I would like to inform you that the ");
        startActivityForResult(Intent.createChooser(emailIntent, "Send email..."), AppConstants.sendEmail);
    }

    private void showReportAlert() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Please select any of them:");

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

    private ArrayList<String> images;
    private SlidingImageAdapter slidingImageAdapter;

    private void initSlider() {
        ViewPager pager = findViewById(R.id.pager);

        slidingImageAdapter = new SlidingImageAdapter(this, images, new SlidingImageAdapter.ImageClickListener() {
            @Override
            public void imageClick(int position) {
                selectedImagePosition = position;
                showImageDialog();
            }
        });

        pager.setAdapter(slidingImageAdapter);
    }

    private int selectedImagePosition;

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
        startActivityForResult(editIntent, AppConstants.editAd);
    }

    private void openMapActivity() {
        if (adInfo == null)
            return;

        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.putExtra(AppConstants.keyAdInfo, adInfo);
        mapIntent.putExtra(DBConstants.flatType, flatType);
        startActivity(mapIntent);
    }

    private Dialog imageDialog;

    private void showImageDialog() {
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

        final Button previousImageBtn, nextImageBtn;
        previousImageBtn = imageDialog.findViewById(R.id.previousImageBtn);
        previousImageBtn.setVisibility(View.VISIBLE);
        nextImageBtn = imageDialog.findViewById(R.id.nextImageBtn);
        nextImageBtn.setVisibility(View.VISIBLE);

        final ImageViewZoomT zoomableImageView = imageDialog.findViewById(R.id.zoomableImageView);
        showImage(images.get(selectedImagePosition), zoomableImageView);

        previousImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImagePosition--;
                if (selectedImagePosition < 0)
                    selectedImagePosition = images.size() - 1;
                showImage(images.get(selectedImagePosition), zoomableImageView);
            }
        });
        nextImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImagePosition++;
                if (selectedImagePosition >= images.size())
                    selectedImagePosition = 0;
                showImage(images.get(selectedImagePosition), zoomableImageView);
            }
        });
    }

    private void showImage(String imagePath, ImageView imageView) {
        if (imagePath != null)
            Glide.with(this)
                    .load(Uri.parse(imagePath))
                    .apply(new RequestOptions().placeholder(R.drawable.image_loading).error(R.drawable.no_image_available))
                    .into(imageView);
        else
            imageView.setImageResource(R.drawable.dummy_flat_image);
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

        final TextView title = deleteAdDialog.findViewById(R.id.title);
        title.setText(R.string.why_do_you_want_to_delete);
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
