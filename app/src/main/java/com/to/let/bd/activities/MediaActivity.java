package com.to.let.bd.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.to.let.bd.R;
import com.to.let.bd.adapters.pick_photo.PickGridAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.components.ImageViewZoomT;
import com.to.let.bd.model.ImageInfo;
import com.to.let.bd.model.pick_photo.GroupImage;
import com.to.let.bd.model.pick_photo.PickData;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.UploadImageService;
import com.to.let.bd.utils.pick_photo.PickConfig;
import com.to.let.bd.utils.pick_photo.PickPhotoHelper;
import com.to.let.bd.utils.pick_photo.PickPhotoListener;
import com.to.let.bd.utils.pick_photo.PickPreferences;
import com.to.let.bd.utils.pick_photo.PickUtils;
import com.to.let.bd.utils.pick_photo.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;

public class MediaActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.please_select_photo);
        }

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

//    private void requestCameraPermission() {
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            executeCameraAction();
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
//        }
//    }

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
    private String flatType;
    private ArrayList<ImageInfo> previouslySelectedImage = new ArrayList<>();

    private void executeStorageAction() {
        Intent intent = getIntent();

        if (intent != null) {
            adId = intent.getStringExtra(DBConstants.adId);
            flatType = intent.getStringExtra(DBConstants.flatType);

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object imageArray = bundle.getSerializable(AppConstants.keyImageList);
                if (imageArray instanceof ArrayList) {
                    imagePreviewArray.clear();
                    imagePreviewArray.addAll((ArrayList<ImageInfo>) imageArray);
                    previouslySelectedImage.clear();
                    previouslySelectedImage.addAll((ArrayList<ImageInfo>) imageArray);
                }
            }
        }

        if (adId == null || adId.isEmpty()) {
            finish();
            return;
        }

        firebaseInit();

        FloatingActionButton fab = findViewById(R.id.fab);
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

        initPhotoRecyclerView();
        initPreviewPhoto();
        updateSelectText(imagePreviewArray.size());
    }

    private void initPhotoRecyclerView() {
        photoList = findViewById(R.id.photoList);
        photoList.setItemAnimator(new DefaultItemAnimator());
        GridLayoutManager layoutManager = new GridLayoutManager(this, pickData.getSpanCount());
        photoList.setLayoutManager(layoutManager);
        photoList.addItemDecoration(new SpaceItemDecoration(PickUtils.getInstance(this).dp2px(PickConfig.ITEM_SPACE), pickData.getSpanCount()));
        photoList.addOnScrollListener(scrollListener);
        showProgressDialog();
        PickPhotoHelper helper = new PickPhotoHelper(this, new PickPhotoListener() {
            @Override
            public void pickSuccess() {
                closeProgressDialog();
                GroupImage groupImage = PickPreferences.getInstance(MediaActivity.this).getListImage();
                allPhotos = groupImage.mGroupMap.get(PickConfig.ALL_PHOTOS);
                if (allPhotos != null && !allPhotos.isEmpty()) {
                    pickGridAdapter = new PickGridAdapter(MediaActivity.this,
                            allPhotos, pickData, (AppConstants.maximumImage - imagePreviewArray.size()));
                    photoList.setAdapter(pickGridAdapter);
                }
            }
        });
        helper.getImages(pickData.isShowGif());
    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
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

    private BroadcastReceiver mBroadcastReceiver;
    private ArrayList<String> imagePathList = new ArrayList<>();
    private int imageIndex = 0;

    private void submitImages() {
        ArrayList<String> imagePathList = new ArrayList<>(pickGridAdapter.getSelectPath());
        if (previouslySelectedImage.size() == 0 && !imagePathList.isEmpty()) {
            onlyUploadNewPhoto(imagePathList, adId, AppConstants.adImageType);
        } else if (previouslySelectedImage.size() == imagePreviewArray.size() && imagePreviewArray.size() > 0 && imagePathList.isEmpty()) {
            showToast(getString(R.string.there_has_no_new_photo_to_publish));
        } else if (previouslySelectedImage.size() > imagePreviewArray.size() && !imagePreviewArray.isEmpty() && imagePathList.isEmpty()) {
            deletePreviouslySelectedSomePhoto();
        } else if (previouslySelectedImage.size() > imagePreviewArray.size() && imagePreviewArray.isEmpty()) {
            deletePreviouslySelectedAllPhoto();
        } else if (previouslySelectedImage.size() < imagePreviewArray.size() && imagePreviewArray.size() > 0 && !imagePathList.isEmpty()) {
            deleteAndNewUpload();
        } else if (imagePathList.isEmpty()) {
            showToast(getString(R.string.please_select_photo));
        } else {
            showToast(getString(R.string.please_report_a_bug));
        }
    }

    private void deleteAndNewUpload() {

    }

    //photos/adPhotos/-L25qyUHGklzpUca2_4b/Map.jpg

    private void deletePreviouslySelectedAllPhoto() {
        initStorageRef();

        for (int i = 0; i < previouslySelectedImage.size(); i++) {
            ImageInfo imageInfo = previouslySelectedImage.get(i);
            final String imagePath;
            if (imageInfo != null) {
                imagePath = imageInfo.imagePath;
            } else {
                imagePath = "photos/adPhotos/" + adId + "/" + i + ".jpg";
            }

            final int finalI = i;
            storageReference
                    .child(imagePath)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            databaseReference
                                    .child(DBConstants.adList)
                                    .child(flatType)
                                    .child(adId)
                                    .child(DBConstants.images)
                                    .child(String.valueOf(finalI))
                                    .setValue(null);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            databaseReference
                                    .child(DBConstants.adList)
                                    .child(flatType)
                                    .child(adId)
                                    .child(DBConstants.images)
                                    .child(String.valueOf(finalI))
                                    .setValue(null);
                        }
                    });
        }
    }

    private void deletePreviouslySelectedSomePhoto() {

    }

    private StorageReference storageReference;

    private void initStorageRef() {
        if (storageReference == null)
            storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void onlyUploadNewPhoto(ArrayList<String> imagePathList, String adId, int type) {
        this.imagePathList.clear();
        this.imagePathList.addAll(imagePathList);
        imageIndex = imagePathList.size() - 1;
        showImageUploadProgressDialog();
        uploadSingleImage(type, adId);
    }

    private void uploadSingleImage(int type, String adId) {
        if (imageIndex >= imagePathList.size()) {
            return;
        }
        // Start StorageUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, UploadImageService.class)
                .putExtra(AppConstants.keyType, type)
                .putExtra(AppConstants.fileUri, Uri.parse("file://" + imagePathList.get(imageIndex)))
                .putExtra(DBConstants.adId, adId)
                .putExtra(AppConstants.imageIndex, imageIndex)
                .setAction(AppConstants.actionUpload));
    }

    private void initBroadcast() {
        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null)
                    return;
                switch (intent.getAction()) {
                    case AppConstants.uploadError:
                        imageUploadFailed();
                        break;
                    case AppConstants.uploadComplete: {
                        int type = intent.getIntExtra(AppConstants.keyType, 0);
                        String adId = intent.getStringExtra(DBConstants.adId);
                        int imageIndex = intent.getIntExtra(AppConstants.imageIndex, 0);
                        String[] imageContents = intent.getStringArrayExtra(AppConstants.imageContents);
                        completeSingleImageUpload(type, adId, imageIndex, imageContents);
                    }
                    break;
                    case AppConstants.uploadProgress: {
                        int type = intent.getIntExtra(AppConstants.keyType, 0);
                        String adId = intent.getStringExtra(DBConstants.adId);
                        int imageIndex = intent.getIntExtra(AppConstants.imageIndex, 0);
                        int progress = intent.getIntExtra(AppConstants.progress, -1);

                        if (type == AppConstants.adImageType)
                            updateProgress(imageIndex, progress);
                    }
                    break;
                }
            }
        };
    }

    private void updateProgress(int imageIndex, int progress) {
        if (progressDialog == null) {
            showImageUploadProgressDialog();
        }

        if (progressDialog == null)
            return;

        ProgressBar progressBar = (ProgressBar) progressDialog.findViewById(R.id.progressBar);
        if (progressBar.isIndeterminate())
            progressBar.setIndeterminate(false);

        int max = progressBar.getMax();
        int count = imagePathList.size();

        int perImageMax = max / count;

        float f = (float) progress / 100f;

        int finalProgress = (perImageMax * (count - imageIndex - 1)) + (int) (perImageMax * f);
        progressBar.setProgress(finalProgress);

        TextView progressStatus = progressDialog.findViewById(R.id.progressStatus);

        String imageUploadStatus = getString(R.string.image_upload_status, finalProgress + "%.");
        progressStatus.setText(imageUploadStatus);
    }

    private void completeSingleImageUpload(int type, String adId, int imageIndex, String[] imageContents) {
        updateDatabase(type, adId, imageIndex, imageContents);
        if (imageIndex != 0) {
            this.imageIndex = imageIndex - 1;
            uploadSingleImage(type, adId);
        } else {
            completeImageUpload();
        }
    }

    private void completeImageUpload() {
        closeImageUploadProgressDialog();
        imageUploadSuccess();
    }

    private void closeImageUploadProgressDialog() {
        if (progressDialog == null) {
            return;
        }
        progressDialog.dismiss();
    }

    private void imageUploadSuccess() {
//        Intent intent = new Intent(this, AdListActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
        finish();
    }

    private void imageUploadFailed() {
        closeImageUploadProgressDialog();
    }

    private void updateDatabase(int type, String adId, int imageIndex, String[] imageContents) {
        HashMap<String, Object> adValues = new HashMap<>();
        adValues.put(AppConstants.downloadUrl, imageContents[0]);
        adValues.put(AppConstants.imageName, imageContents[1]);
        adValues.put(AppConstants.imagePath, imageContents[2]);

        HashMap<String, Object> childUpdates = new HashMap<>();
        if (type == AppConstants.adImageType) {
            childUpdates.put("/" + DBConstants.adList + "/" + flatType + "/" + adId + "/" + DBConstants.images + "/" + imageIndex, adValues);
        }

        databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });
    }

    private DatabaseReference databaseReference;

    private void firebaseInit() {
        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private Dialog progressDialog;

    private void showImageUploadProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new Dialog(this);
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            progressDialog.setContentView(R.layout.dialog_progress);
            Window window = progressDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            }

            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        progressDialog.show();

        ProgressBar progressBar = progressDialog.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        TextView title = progressDialog.findViewById(R.id.title);
        title.setText(R.string.image_upload_title);

        TextView progressMessage = progressDialog.findViewById(R.id.progressMessage);
        progressMessage.setText(R.string.image_upload_message);

        TextView progressStatus = progressDialog.findViewById(R.id.progressStatus);

        String imageUploadStatus = getString(R.string.image_upload_starting);
        progressStatus.setText(imageUploadStatus);
    }

    private RecyclerView previewPhotos;
    private TextView previewMessage;

    private void initPreviewPhoto() {
        previewMessage = findViewById(R.id.previewMessage);
        previewPhotos = findViewById(R.id.previewPhotos);
        previewPhotos.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        previewPhotos.setLayoutManager(layoutManager);
        imagePreviewAdapter = new ImagePreviewAdapter();
        previewPhotos.setAdapter(imagePreviewAdapter);
    }

    private ImagePreviewAdapter imagePreviewAdapter;
    private ArrayList<ImageInfo> imagePreviewArray = new ArrayList<>();

    class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new MyViewHolder(inflater.inflate(R.layout.item_image_preview, parent, false));
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            ImageInfo imageInfo = imagePreviewArray.get(position);
            String imageUri = null;
            if (imageInfo != null) {
                imageUri = imageInfo.downloadUrl;
            }

            if (imageUri != null) {
                if (!imageUri.startsWith("https")) {
                    imageUri = "file://" + imageUri;
                }
                Glide.with(MediaActivity.this)
                        .load(Uri.parse(imageUri))
                        .into(holder.previewPhoto);
            } else
                holder.previewPhoto.setImageResource(R.drawable.dummy_flat_image);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return imagePreviewArray.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView previewPhoto;
            Button crossButton;

            MyViewHolder(View itemView) {
                super(itemView);

                previewPhoto = itemView.findViewById(R.id.previewPhoto);
                crossButton = itemView.findViewById(R.id.crossButton);

                crossButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateList(getLayoutPosition());
                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String imageUri = imagePreviewArray.get(getLayoutPosition()).downloadUrl;
                        if (imageUri != null) {
                            if (!imageUri.startsWith("https")) {
                                imageUri = "file://" + imageUri;
                            }
                            showImageDialog(imageUri);
                        }
                    }
                });
            }
        }
    }

    void updateList(int position) {
        String imageUri = "";
        if (imagePreviewArray.get(position) != null)
            imageUri = imagePreviewArray.get(position).downloadUrl;

        if (position < imagePreviewArray.size()) {
            ImageInfo imageInfo = imagePreviewArray.get(position);
            imagePreviewArray.remove(position);
            imagePreviewAdapter.notifyDataSetChanged();

            if (imageInfo == null || (imageInfo.imageName != null && !imageInfo.imageName.isEmpty()))
                pickGridAdapter.maxSelectSize = pickGridAdapter.maxSelectSize + 1;
        }

        if (position < pickGridAdapter.getSelectPath().size() &&
                pickGridAdapter.getSelectPath().get(position).contains(imageUri)) {
            pickGridAdapter.getSelectPath().remove(position);
            pickGridAdapter.notifyDataSetChanged();
        }

        updateSelectText(imagePreviewArray.size());
    }

    private void updateSelectText(int selectSize) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (selectSize <= 1) {
                actionBar.setTitle("(" + selectSize + ") item selected.");
            } else {
                actionBar.setTitle("(" + selectSize + ") item's selected.");
            }
        }
        if (selectSize > 0) {
            previewMessage.setVisibility(View.GONE);
        } else {
            previewMessage.setVisibility(View.INVISIBLE);
        }
    }

    public void addNewImage(String imagePath) {
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.downloadUrl = imagePath;
        imagePreviewArray.add(imageInfo);
        imagePreviewAdapter.notifyDataSetChanged();
        updateSelectText(imagePreviewArray.size());
    }

    public void removeImage(String imagePath) {
        for (int i = 0; i < imagePreviewArray.size(); i++) {
            if (imagePreviewArray.get(i) != null && imagePreviewArray.get(i).downloadUrl.contains(imagePath)) {
                imagePreviewArray.remove(i);
                break;
            }
        }
        imagePreviewAdapter.notifyDataSetChanged();
        updateSelectText(imagePreviewArray.size());
    }

    private Dialog imageDialog;

    private void showImageDialog(String imageUri) {
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
        Glide.with(this)
                .load(Uri.parse(imageUri))
                .into(zoomableImageView);
    }
}
