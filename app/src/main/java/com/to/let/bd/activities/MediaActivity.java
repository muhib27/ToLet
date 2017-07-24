package com.to.let.bd.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.to.let.bd.MainActivity;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.adapters.pick_photo.PickGridAdapter;
import com.to.let.bd.utils.pick_photo.SpaceItemDecoration;
import com.to.let.bd.model.pick_photo.GroupImage;
import com.to.let.bd.model.pick_photo.PickData;
import com.to.let.bd.utils.pick_photo.PickConfig;
import com.to.let.bd.utils.pick_photo.PickPhotoHelper;
import com.to.let.bd.utils.pick_photo.PickPhotoListener;
import com.to.let.bd.utils.pick_photo.PickPreferences;
import com.to.let.bd.utils.pick_photo.PickUtils;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.SmartToLetConstants;
import com.to.let.bd.utils.UploadImageService;

import java.util.ArrayList;
import java.util.HashMap;

public class MediaActivity extends BaseActivity {

    private String adId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        requestStoragePermission();
        initBroadcast();
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

    private void executeStorageAction() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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
                imagePathList.clear();
                imagePathList.addAll(pickGridAdapter.getSelectPath());
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
        if (imagePathList.isEmpty()) {
            showToast(getString(R.string.please_select_photo));
        } else {
            showProgressDialog();
            uploadImage();
        }
    }

    private ArrayList<String> imagePathList = new ArrayList<>();
    private int imageIndex = 0;

    private void uploadImage() {
        imageIndex = imagePathList.size() - 1;
        if (imageIndex >= imagePathList.size()) {
            return;
        }

        // Start StorageUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, UploadImageService.class)
                .putExtra(SmartToLetConstants.fileUri, Uri.parse("file://" + pickGridAdapter.getSelectPath().get(imageIndex)))
                .putExtra(SmartToLetConstants.keyType, SmartToLetConstants.adImageType)
                .putExtra(SmartToLetConstants.adId, adId)
                .putExtra(SmartToLetConstants.imageIndex, imageIndex)
                .setAction(SmartToLetConstants.actionUpload));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

//        if (mUserBasicInfoListener != null) {
//            databaseReference.removeEventListener(mUserBasicInfoListener);
//        }
//        if (mUserPhotoIdsListener != null) {
//            databaseReference.removeEventListener(mUserBasicInfoListener);
//        }
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void initBroadcast() {
        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showLog("onReceive:" + intent);
                switch (intent.getAction()) {
                    case SmartToLetConstants.uploadComplete:
                        int imageIndex = intent.getIntExtra(SmartToLetConstants.imageIndex, 0);
                        String[] imageContents = intent.getStringArrayExtra(SmartToLetConstants.imageContents);
                        String downloadUrl = intent.getStringExtra(SmartToLetConstants.downloadUrl);

                        updateDatabase(imageIndex, imageContents);
                        if (imageIndex != 0) {
                            imagePathList.remove(imageIndex);
                            uploadImage();
                        } else {
                            closeProgressDialog();
                            completeUploading();
                        }
                        break;
                    case SmartToLetConstants.uploadError:
                        closeProgressDialog();
                        break;
                }
            }
        };
    }

    private void completeUploading() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void updateDatabase(int imageIndex, String[] imageContents) {
        firebaseInit();

        HashMap<String, Object> adValues = new HashMap<>();
        adValues.put(SmartToLetConstants.downloadUrl, imageContents[0]);
        adValues.put(SmartToLetConstants.imageName, imageContents[1]);
        adValues.put(SmartToLetConstants.imagePath, imageContents[2]);

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DBConstants.adList + "/" + adId + "/" + DBConstants.images + "/" + imageIndex, adValues);
        mDatabase.updateChildren(childUpdates);
    }

    private DatabaseReference mDatabase;

    private void firebaseInit() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
