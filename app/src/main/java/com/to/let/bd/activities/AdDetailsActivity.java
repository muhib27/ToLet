package com.to.let.bd.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.to.let.bd.R;
import com.to.let.bd.adapters.SlidingImageAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.components.ImageViewZoomT;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.AppConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

        getData();
        init();
        initRecycler();

        if (images != null && images.length > 0) {
            initSlider();
        }

        showLog();
    }

    private AdInfo adInfo;
    private String map;

    private void getData() {
        adInfo = new AdInfo();
        adInfo.setAdId(getIntent().getStringExtra(DBConstants.adId));
        adInfo.setAdId(getIntent().getStringExtra(DBConstants.adId));
        adInfo.setFlatRent(getIntent().getLongExtra(DBConstants.flatRent, 0));
        adInfo.setOthersFee(getIntent().getLongExtra(DBConstants.othersFee, 0));

//        adInfo.setBedRoom(getIntent().getIntExtra(DBConstants.bedRoom, 0));
//        adInfo.setBathroom(getIntent().getIntExtra(DBConstants.bathroom, 0));
//        adInfo.setBalcony(getIntent().getIntExtra(DBConstants.balcony, 0));

        adInfo.setStartingDate(getIntent().getIntExtra(DBConstants.startingDate, 0));
        adInfo.setStartingMonth(getIntent().getIntExtra(DBConstants.startingMonth, 0));
        adInfo.setStartingYear(getIntent().getIntExtra(DBConstants.startingYear, 0));

        adInfo.setLatitude(getIntent().getDoubleExtra(DBConstants.latitude, 0));
        adInfo.setLongitude(getIntent().getDoubleExtra(DBConstants.longitude, 0));
        adInfo.setFlatSpace(getIntent().getLongExtra(DBConstants.flatSpace, 0));

        adInfo.setFullAddress(getIntent().getStringExtra(DBConstants.fullAddress));

        images = getIntent().getStringArrayExtra(DBConstants.images);
        map = getIntent().getStringExtra(DBConstants.map);
    }

    private TextView rentDate, totalRent, roomDetails, addressDetails,
            descriptionDetails, reportThis, photoCount, privacyPolicy;
    private Button callBtn, emailBtn;

    private void init() {
        rentDate = (TextView) findViewById(R.id.rentDate);
        totalRent = (TextView) findViewById(R.id.totalRent);
        roomDetails = (TextView) findViewById(R.id.roomDetails);
        addressDetails = (TextView) findViewById(R.id.addressDetails);
        descriptionDetails = (TextView) findViewById(R.id.descriptionDetails);

        privacyPolicy = (TextView) findViewById(R.id.privacyPolicy);
        privacyPolicy.setText(getString(R.string.privacy_policy_note, getString(R.string.terms_of_use), getString(R.string.privacy_policy)));
        reportThis = (TextView) findViewById(R.id.reportThis);

        photoCount = (TextView) findViewById(R.id.photoCount);

        callBtn = (Button) findViewById(R.id.callBtn);
        emailBtn = (Button) findViewById(R.id.emailBtn);

        if (adInfo != null) {
            String totalRent = "৳" + adInfo.getFlatRent();
            this.totalRent.setText(totalRent);

//            String roomDetails = adInfo.getBedRoom() + " bed, " + adInfo.getBathroom() + " bath, " + adInfo.getBalcony() + " balcony/veranda.";
//            this.roomDetails.setText(roomDetails);

            addressDetails.setText(adInfo.getFullAddress());

            setRentDate(adInfo.getStartingDate() + "-" + adInfo.getStartingMonth() + "-" + adInfo.getStartingYear());
        }

        if (images != null) {
            String photoCount = images.length > 1 ? images.length + " Photo's" : images.length + " Photos";
            this.photoCount.setText(photoCount);
        }

        callBtn.setOnClickListener(this);
        emailBtn.setOnClickListener(this);
//        privacyPolicy.setOnClickListener(this);
//        reportThis.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == callBtn) {

        } else if (view == emailBtn) {

        }
//        else if (view == privacyPolicy) {
//
//        } else if (view == reportThis) {
//
//        }
    }

    private RecyclerView recyclerView;

    private void initRecycler() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mapIcon.clear();
        mapTitle.clear();

        mapIcon.add(R.mipmap.ic_launcher);
        mapIcon.add(R.mipmap.ic_launcher);
        mapIcon.add(R.mipmap.ic_launcher);

        mapTitle.add(getString(R.string.map_view));
        mapTitle.add(getString(R.string.school_college));
        mapTitle.add(getString(R.string.grocery_store));

        MapListAdapter mapListAdapter = new MapListAdapter();
        recyclerView.setAdapter(mapListAdapter);

        LinearLayoutManager categoryLLM = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(categoryLLM);
    }

    private ViewPager pager;
    private String[] images;

    private void initSlider() {
        pager = (ViewPager) findViewById(R.id.pager);

        SlidingImageAdapter slidingImageAdapter = new SlidingImageAdapter(this, images, new SlidingImageAdapter.ImageClickListener() {
            @Override
            public void imageClick(int position) {
                showImageDialog(position);
            }
        });

        pager.setAdapter(slidingImageAdapter);
//        pager.post(new Runnable() {
//            @Override
//            public void run() {
//                startScroll();
//            }
//        });
//        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            //            private int lastPosition = -1;
//            private int state = 0;
//
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                startScroll();
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                if (state == 1) {
//                    handler.removeCallbacks(autoRunPage);
//                } else {
//                    startScroll();
//                }
//                this.state = state;
//            }
//        });
    }

//    private void startScroll() {
//        handler.removeCallbacks(autoRunPage);
//        handler.postDelayed(autoRunPage, AppConstants.autoScrollDuration);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        handler.removeCallbacks(autoRunPage);
//    }

//    private Runnable autoRunPage = new Runnable() {
//        @Override
//        public void run() {
//            int pagerCurrentItem = pager.getCurrentItem();
//            if (pagerCurrentItem == images.length - 1) {
//                pagerCurrentItem = 0;
//                pager.setCurrentItem(pagerCurrentItem, false);
//            } else {
//                pagerCurrentItem++;
//                pager.setCurrentItem(pagerCurrentItem, true);
//            }
//        }
//    };
//
//    private Handler handler = new Handler();

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
                icon = (ImageView) itemView.findViewById(R.id.icon);
                title = (TextView) itemView.findViewById(R.id.title);

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

        final ImageViewZoomT zoomableImageView = (ImageViewZoomT) imageDialog.findViewById(R.id.zoomableImageView);
        Glide.with(this)
                .load(Uri.parse(images[imagePosition]))
                .into(zoomableImageView);
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

}
