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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.to.let.bd.R;
import com.to.let.bd.adapters.SlidingImageAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.components.ImageViewZoomT;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.model.FamilyInfo;
import com.to.let.bd.model.MessInfo;
import com.to.let.bd.model.OthersInfo;
import com.to.let.bd.model.SubletInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.DateUtils;
import com.to.let.bd.utils.MyAnalyticsUtil;

import java.util.ArrayList;
import java.util.Date;

public class AdDetailsActivity extends BaseActivity implements View.OnClickListener {

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

        myAnalyticsUtil = new MyAnalyticsUtil(this);

        getData();
        init();
        initRecycler();

        if (images != null && images.length > 0) {
            initSlider();
        }

        myAnalyticsUtil.adDetails(adInfo, getUid());

        showLog();
    }

    private MyAnalyticsUtil myAnalyticsUtil;

    private AdInfo adInfo;

    private void getData() {
        adInfo = (AdInfo) getIntent().getExtras().getSerializable(AppConstants.keyAdInfo);

        if (adInfo == null || adInfo.getImages() == null || adInfo.getImages().isEmpty()) {
            images = new String[0];
        } else {
            images = new String[adInfo.getImages().size()];
            for (int i = 0; i < adInfo.getImages().size(); i++) {
                images[i] = adInfo.getImages().get(i).downloadUrl;
            }
        }
    }

    private TextView rentDate, totalRent, roomDetails, addressDetails, rentType,
            othersFacility, othersFacilityDetails, reportThis, photoCount, privacyPolicy;
    private Button callBtn, emailBtn;

    private void init() {
        rentDate = findViewById(R.id.rentDate);
        totalRent = findViewById(R.id.totalRent);
        roomDetails = findViewById(R.id.roomDetails);
        addressDetails = findViewById(R.id.addressDetails);

        rentType = findViewById(R.id.rentType);
        othersFacility = findViewById(R.id.othersFacility);
        othersFacilityDetails = findViewById(R.id.othersFacilityDetails);

        privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyPolicy.setText(getString(R.string.privacy_policy_note, getString(R.string.terms_of_use), getString(R.string.privacy_policy)));
        reportThis = findViewById(R.id.reportThis);

        photoCount = findViewById(R.id.photoCount);

        callBtn = findViewById(R.id.callBtn);
        emailBtn = findViewById(R.id.emailBtn);

        if (adInfo != null) {
            String totalRent = "TK " + AppConstants.rentFormatter(adInfo.getFlatRent());
            this.totalRent.setText(totalRent);
            this.roomDetails.setText(AppConstants.flatDescription(this, adInfo));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(adInfo.getFullAddress());
            if (adInfo.getFlatDescription() != null && !adInfo.getFlatDescription().isEmpty()) {
                stringBuilder.append("\n\n");
                stringBuilder.append(adInfo.getFlatDescription());
            }

            addressDetails.setText(stringBuilder);
            String[] splittedDate = DateUtils.splittedDate(adInfo.getStartingFinalDate());
            Date date = DateUtils.getDate(splittedDate, DateUtils.format4);

            String rentDate = DateUtils.getFormattedDateString(date, DateUtils.format2);
            long elapsedDays = DateUtils.differenceBetweenToday(date.getTime());
            setRentDate(rentDate, elapsedDays);
        }

        ImageView noImageView = findViewById(R.id.noImageView);
        othersFacilityDetails();
        noImageView.setVisibility(View.GONE);
        if (images != null && images.length > 0) {
            String photoCount = images.length > 1 ? images.length + " Photo's" : images.length + " Photos";
            this.photoCount.setText(photoCount);
        } else {
            noImageView.setVisibility(View.VISIBLE);
            if (adInfo.getMap() != null && adInfo.getMap().downloadUrl != null) {
                Glide.with(this)
                        .load(Uri.parse(adInfo.getMap().downloadUrl))
                        .into(noImageView);
            }
        }

        callBtn.setOnClickListener(this);
        emailBtn.setOnClickListener(this);
        reportThis.setOnClickListener(this);
    }

    private void othersFacilityDetails() {
        StringBuilder stringBuilder = new StringBuilder();
        String rentType = "";
        if (adInfo.getFamilyInfo() != null) {
            FamilyInfo familyInfo = adInfo.getFamilyInfo();
            rentType = getString(R.string.family);

            if (familyInfo.twentyFourWater) {
                stringBuilder.append(getString(R.string.twenty_four_water));
                stringBuilder.append("\n");
            }
            if (familyInfo.gasSupply) {
                stringBuilder.append(getString(R.string.supply_gas));
                stringBuilder.append("\n");
            }
            if (familyInfo.securityGuard) {
                stringBuilder.append(getString(R.string.security_guard));
                stringBuilder.append("\n");
            }

            if (familyInfo.parkingGarage) {
                stringBuilder.append(getString(R.string.parking_garage));
                stringBuilder.append("\n");
            }
            if (familyInfo.lift) {
                stringBuilder.append(getString(R.string.lift));
                stringBuilder.append("\n");
            }
            if (familyInfo.generator) {
                stringBuilder.append(getString(R.string.generator));
                stringBuilder.append("\n");
            }
            if (familyInfo.wellFurnished) {
                stringBuilder.append(getString(R.string.well_furnished));
                stringBuilder.append("\n");
            }
            if (familyInfo.kitchenCabinet) {
                stringBuilder.append(getString(R.string.kitchen_cabinet));
                stringBuilder.append("\n");
            }
        } else if (adInfo.getMessInfo() != null) {
            MessInfo messInfo = adInfo.getMessInfo();

            rentType = getString(R.string.mess);

            if (messInfo.memberType == 1) {
                rentType += ", Only Female";
            } else {
                rentType += ", Only Male";
            }

            if (messInfo.mealFacility) {
                stringBuilder.append(getString(R.string.meal_facility));
                stringBuilder.append("\n");
            }
            if (messInfo.maidServant) {
                stringBuilder.append(getString(R.string.maid_servant));
                stringBuilder.append("\n");
            }
            if (messInfo.twentyFourWater) {
                stringBuilder.append(getString(R.string.twenty_four_water));
                stringBuilder.append("\n");
            }
            if (messInfo.nonSmoker) {
                stringBuilder.append(getString(R.string.non_smoker_only));
                stringBuilder.append("\n");
            }
            if (messInfo.fridge) {
                stringBuilder.append(getString(R.string.fridge_facility));
                stringBuilder.append("\n");
            }
            if (messInfo.wifi) {
                stringBuilder.append(getString(R.string.wifi_facility));
                stringBuilder.append("\n");
            }
        } else if (adInfo.getSubletInfo() != null) {
            SubletInfo subletInfo = adInfo.getSubletInfo();
            rentType = getString(R.string.sublet);
            if (subletInfo.twentyFourWater) {
                stringBuilder.append(getString(R.string.twenty_four_water));
                stringBuilder.append("\n");
            }
            if (subletInfo.gasSupply) {
                stringBuilder.append(getString(R.string.supply_gas));
                stringBuilder.append("\n");
            }
            if (subletInfo.generator) {
                stringBuilder.append(getString(R.string.generator));
                stringBuilder.append("\n");
            }
            if (subletInfo.lift) {
                stringBuilder.append(getString(R.string.lift));
                stringBuilder.append("\n");
            }
            if (subletInfo.wellFurnished) {
                stringBuilder.append(getString(R.string.well_furnished));
                stringBuilder.append("\n");
            }
            if (subletInfo.kitchenShare) {
                stringBuilder.append(getString(R.string.kitchen_share));
                stringBuilder.append("\n");
            }
        } else if (adInfo.getOthersInfo() != null) {
            OthersInfo othersInfo = adInfo.getOthersInfo();
            rentType = othersInfo.rentType;
            stringBuilder.append(othersInfo.rentType);
            stringBuilder.append("\n");

            if (othersInfo.lift) {
                stringBuilder.append(getString(R.string.lift));
                stringBuilder.append("\n");
            }
            if (othersInfo.generator) {
                stringBuilder.append(getString(R.string.generator));
                stringBuilder.append("\n");
            }
            if (othersInfo.securityGuard) {
                stringBuilder.append(getString(R.string.security_guard));
                stringBuilder.append("\n");
            }
            if (othersInfo.parkingGarage) {
                stringBuilder.append(getString(R.string.parking_garage));
                stringBuilder.append("\n");
            }
            if (othersInfo.fullyDecorated) {
                stringBuilder.append(getString(R.string.fully_decorated));
                stringBuilder.append("\n");
            }
            if (othersInfo.wellFurnished) {
                stringBuilder.append(getString(R.string.well_furnished));
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
        if (view == callBtn) {
            callTheUser();
        } else if (view == emailBtn) {
            mailTheUser();
        } else if (view == reportThis) {
            showReportAlert();
        }
    }

    private void callTheUser() {
        String mobileNumber = "+8801914868646";
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNumber, null));
        startActivity(intent);
    }

    private void mailTheUser() {
        String emailAddress = "mcnasim@gmail.com";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Interested in your ad");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello dear, I would like to inform you that the ");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void showReportAlert() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Select one them:");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.ad_report_type_array));
//
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
            }
        });
        builderSingle.show();
    }

    private RecyclerView recyclerView;

    private void initRecycler() {
        recyclerView = findViewById(R.id.recyclerView);

        mapIcon.clear();
        mapTitle.clear();

        mapIcon.add(R.mipmap.ic_launcher);
        mapIcon.add(R.mipmap.ic_launcher);

        mapTitle.add(getString(R.string.map_view));
        mapTitle.add(getString(R.string.near_by));

        MapListAdapter mapListAdapter = new MapListAdapter();
        recyclerView.setAdapter(mapListAdapter);

        LinearLayoutManager categoryLLM = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(categoryLLM);
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

    private ArrayList<String> mapTitle = new ArrayList<>();
    private ArrayList<Integer> mapIcon = new ArrayList<>();

    private class MapListAdapter extends RecyclerView.Adapter<MapListAdapter.MyViewHolder> {

        private LayoutInflater layoutInflater;

        MapListAdapter() {
            layoutInflater = LayoutInflater.from(AdDetailsActivity.this);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.row_item_map, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            holder.icon.setImageResource(mapIcon.get(position));
            holder.title.setText(mapTitle.get(position));
        }

        @Override
        public int getItemCount() {
            return mapIcon.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title;

            MyViewHolder(final View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                title = itemView.findViewById(R.id.title);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openMapActivity(getLayoutPosition());
                    }
                });
            }
        }
    }

    private void openMapActivity(int typePosition) {
        if (adInfo == null)
            return;

        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.putExtra(AppConstants.keyType, typePosition);
        mapIntent.putExtra(DBConstants.latitude, adInfo.getLatitude());
        mapIntent.putExtra(DBConstants.longitude, adInfo.getLongitude());
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

        final ImageViewZoomT zoomableImageView = imageDialog.findViewById(R.id.zoomableImageView);
        Glide.with(this)
                .load(Uri.parse(images[imagePosition]))
                .into(zoomableImageView);
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
