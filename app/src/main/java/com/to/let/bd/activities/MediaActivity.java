package com.to.let.bd.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.to.let.bd.R;
import com.to.let.bd.adapters.pick_photo.PickGridAdapter;
import com.to.let.bd.common.BaseImageUploadActivity;
import com.to.let.bd.model.pick_photo.GroupImage;
import com.to.let.bd.model.pick_photo.PickData;
import com.to.let.bd.utils.SmartToLetConstants;
import com.to.let.bd.utils.pick_photo.PickConfig;
import com.to.let.bd.utils.pick_photo.PickPhotoHelper;
import com.to.let.bd.utils.pick_photo.PickPhotoListener;
import com.to.let.bd.utils.pick_photo.PickPreferences;
import com.to.let.bd.utils.pick_photo.PickUtils;
import com.to.let.bd.utils.pick_photo.SpaceItemDecoration;

import java.util.ArrayList;

public class MediaActivity extends BaseImageUploadActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.post_your_add);
        }

        requestStoragePermission();
//        initBroadcast();
    }

    private static final int PERMISSIONS_REQUEST_CODE = 1;

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            executeStorageAction();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            executeCameraAction();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
        }
    }

//    private void requestCameraAndStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            executeFullAction();
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
//                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    PERMISSIONS_REQUEST_CODE);
//        }
//    }

    private String adId;

    private void executeStorageAction() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra(SmartToLetConstants.mediaExtra);
            if (bundle != null) {
                adId = bundle.getString(SmartToLetConstants.adId, SmartToLetConstants.storageCommonFolderName);
            }
        }

        if (adId == null || adId.isEmpty()) {
            adId = SmartToLetConstants.storageCommonFolderName;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitImages();
            }
        });
        init();
    }

    private void executeCameraAction() {

    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        showLog("requestCode: " + requestCode);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    executeStorageAction();
                } else if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    executeCameraAction();
                } else {
                    finish();
                }
            default:
                break;
        }
    }

    private PickData pickData;
    private RecyclerView photoList;
    private PickGridAdapter pickGridAdapter;
    private ArrayList<String> allPhotos;
    private RequestManager manager;

    private void init() {
        manager = Glide.with(this);
        pickData = new PickData();
        pickData.setPickPhotoSize(3);
        pickData.setShowCamera(false);
        pickData.setSpanCount(3);
        pickData.setLightStatusBar(true);
        pickData.setStatusBarColor("#ffffff");
        pickData.setToolbarColor("#ffffff");
        pickData.setToolbarIconColor("#000000");
        pickData.setClickSelectable(true);
        pickData.setShowGif(false);

        updateSelectText(0);
        initPhotoRecyclerView();
    }

    private void initPhotoRecyclerView() {
        photoList = (RecyclerView) findViewById(R.id.photoList);
        photoList.setItemAnimator(new DefaultItemAnimator());
        GridLayoutManager layoutManager = new GridLayoutManager(this, pickData.getSpanCount());
        photoList.setLayoutManager(layoutManager);
        photoList.addItemDecoration(new SpaceItemDecoration(PickUtils.getInstance(this).dp2px(PickConfig.ITEM_SPACE), pickData.getSpanCount()));
        photoList.addOnScrollListener(scrollListener);
        PickPhotoHelper helper = new PickPhotoHelper(this, new PickPhotoListener() {
            @Override
            public void pickSuccess() {
                GroupImage groupImage = PickPreferences.getInstance(MediaActivity.this).getListImage();
                allPhotos = groupImage.mGroupMap.get(PickConfig.ALL_PHOTOS);
                if (allPhotos == null) {
                    showLog("PickPhotoView:" + "Image is Empty");
                } else {
                    showLog("All photos size: " + String.valueOf(allPhotos.size()));
                }
                if (allPhotos != null && !allPhotos.isEmpty()) {
                    pickGridAdapter = new PickGridAdapter(MediaActivity.this, allPhotos, pickData);
                    photoList.setAdapter(pickGridAdapter);
                }
            }
        });
        helper.getImages(pickData.isShowGif());
    }

    public void updateSelectText(int selectSize) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (selectSize <= 1) {
                actionBar.setTitle("(" + selectSize + ") item selected.");
            } else {
                actionBar.setTitle("(" + selectSize + ") item's selected.");
            }
        }
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (Math.abs(dy) > PickConfig.SCROLL_THRESHOLD) {
                manager.pauseRequests();
            } else {
                manager.resumeRequests();
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                manager.resumeRequests();
            }
        }
    };

    private void submitImages() {
        ArrayList<String> imagePathList = new ArrayList<>();
        imagePathList.clear();
        imagePathList.addAll(pickGridAdapter.getSelectPath());

        if (imagePathList.isEmpty()) {
            showToast(getString(R.string.please_select_photo));
        } else {
            uploadImages(imagePathList, adId, SmartToLetConstants.adImageType);
        }
    }

    @Override
    protected void imageUploadSuccess() {
        finish();
        Intent intent = new Intent(this, AdListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void imageUploadFailed() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
