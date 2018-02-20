package com.to.let.bd.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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
import com.bumptech.glide.request.RequestOptions;
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
import com.to.let.bd.utils.MyAnalyticsUtil;
import com.to.let.bd.utils.UploadImageService;
import com.to.let.bd.utils.pick_photo.PickConfig;
import com.to.let.bd.utils.pick_photo.PickPhotoHelper;
import com.to.let.bd.utils.pick_photo.PickPhotoListener;
import com.to.let.bd.utils.pick_photo.PickPreferences;
import com.to.let.bd.utils.pick_photo.PickUtils;
import com.to.let.bd.utils.pick_photo.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class MediaActivity extends BaseActivity {

    private MyAnalyticsUtil myAnalyticsUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        myAnalyticsUtil = new MyAnalyticsUtil(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        updateTitle(getString(R.string.please_select_photo));

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
                imagePreviewArray.clear();
                previouslySelectedImage.clear();

                Object object = bundle.getSerializable(AppConstants.keyImageList);
                if (object != null) {
                    HashMap<String, ImageInfo> imageMap = (HashMap) object;
                    SortedSet<String> keys = new TreeSet<>(imageMap.keySet());
                    for (String key : keys) {
                        ImageInfo imageInfo = imageMap.get(key);
                        if (imageInfo != null) {
                            imagePreviewArray.add(imageInfo);
                            previouslySelectedImage.add(imageInfo);
                        }
                    }
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

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    executeStorageAction();
                } else {
                    myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyStoragePermissionEvent, "PERMISSION_DENIED");
                    finish();
                }
            default:
                break;
        }
    }

    private PickData pickData;
    private RecyclerView photoList;
    private PickGridAdapter pickGridAdapter;
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
                ArrayList<String> allPhotos = groupImage.mGroupMap.get(PickConfig.ALL_PHOTOS);
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
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isImageDeleted = false;

    @Override
    public void onBackPressed() {
        if (isImageDeleted) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
        super.onBackPressed();
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
    private int imageIndex = 0;

    private void submitImages() {
        if (pickGridAdapter.getSelectPath().isEmpty()) {
            showSimpleDialog(getString(R.string.please_select_photo));
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryMediaEvent, "no media");
            return;
        }

        if (previouslySelectedImage.size() == imagePreviewArray.size()) {
            boolean similarityFlag = true;
            for (int i = 0; i < previouslySelectedImage.size(); i++) {
                if (previouslySelectedImage.get(i) == null || imagePreviewArray.get(i) == null) {
                    continue;
                }

                if (!previouslySelectedImage.get(i).downloadUrl.equals(imagePreviewArray.get(i).downloadUrl)) {
                    similarityFlag = false;
                    break;
                }
            }

            if (similarityFlag) {
                showSimpleDialog(getString(R.string.there_has_no_new_photo_to_publish));
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySubmitTryMediaEvent, "no new media");
                return;
            }
        }

        startUploadingPhoto();
    }

    private StorageReference storageReference;

    private void initStorageRef() {
        if (storageReference == null)
            storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void startUploadingPhoto() {
        imageIndex = imagePreviewArray.size() - 1;
        showImageUploadProgressDialog();

        restrictedIndexArrayList.clear();
        permittedIndexArrayList.clear();
        for (int i = 0; i < imagePreviewArray.size(); i++) {
            int foundedIndex = processImagePathIndex(i);
            if (foundedIndex >= 0)
                restrictedIndexArrayList.add(foundedIndex);
        }

        for (int i = imagePreviewArray.size() - 1; i >= 0; i--) {
            if (!restrictedIndexArrayList.contains(i))
                permittedIndexArrayList.add(i);
        }
        uploadSingleImage();
    }

    private ArrayList<Integer> restrictedIndexArrayList = new ArrayList<>();
    private ArrayList<Integer> permittedIndexArrayList = new ArrayList<>();

    private void uploadSingleImage() {
        if (imageIndex >= imagePreviewArray.size() || imageIndex < 0) {
            completeImageUpload();
            return;
        }

        if (imagePreviewArray.get(imageIndex) != null && imagePreviewArray.get(imageIndex).downloadUrl.startsWith("https")) {
            imageIndex = imageIndex - 1;
            uploadSingleImage();
            return;
        }

        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMediaUploadEvent, "start");
        int saveIndex = imageIndex;

        if (restrictedIndexArrayList.contains(saveIndex)) {
            if (permittedIndexArrayList.size() > 0) {
                saveIndex = permittedIndexArrayList.get(0);
                permittedIndexArrayList.remove(0);
                restrictedIndexArrayList.add(saveIndex);
            }
        }

//        for (int i = saveIndex; i >= 0; i--) {
//            int foundedIndex = processImagePathIndex(i);
//            if (foundedIndex < 0)
//                continue;
//
//            if (foundedIndex == saveIndex) {
//                conflictDatabaseIndex = true;
//                break;
//            }
//        }
//
//        if (conflictDatabaseIndex)


        // Start StorageUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, UploadImageService.class)
                .putExtra(AppConstants.keyType, AppConstants.adImageType)
                .putExtra(AppConstants.fileUri, Uri.parse("file://" + imagePreviewArray.get(imageIndex).downloadUrl))
                .putExtra(DBConstants.adId, adId)
                .putExtra(AppConstants.saveIndex, saveIndex)
                .putExtra(AppConstants.imageIndex, imageIndex)
                .setAction(AppConstants.actionUpload));
    }

    private int processImagePathIndex(int providedIndex) {
        ImageInfo imageInfo = imagePreviewArray.get(providedIndex);
        if (imageInfo == null || imageInfo.imageName == null || imageInfo.imageName.isEmpty())
            return -1;
        String imageName = imageInfo.imageName;

        if (imageName.contains(".")) {
            String[] part = imageName.split("\\.");

            if (part.length > 0) {
                try {
                    return Integer.parseInt(part[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return -1;
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
                        int saveIndex = intent.getIntExtra(AppConstants.saveIndex, 0);
                        String[] imageContents = intent.getStringArrayExtra(AppConstants.imageContents);
                        completeSingleImageUpload(type, adId, saveIndex, imageIndex, imageContents);
                    }
                    break;
                    case AppConstants.uploadProgress: {
                        int type = intent.getIntExtra(AppConstants.keyType, 0);
                        String adId = intent.getStringExtra(DBConstants.adId);
                        int imageIndex = intent.getIntExtra(AppConstants.imageIndex, 0);
                        int saveIndex = intent.getIntExtra(AppConstants.saveIndex, 0);
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
        int count = imagePreviewArray.size();

        int perImageMax = max / count;

        float f = (float) progress / 100f;

        int finalProgress = (perImageMax * (count - imageIndex - 1)) + (int) (perImageMax * f);
        progressBar.setProgress(finalProgress);

        TextView progressStatus = progressDialog.findViewById(R.id.progressStatus);

        String imageUploadStatus = getString(R.string.image_upload_status, finalProgress + "%.");
        progressStatus.setText(imageUploadStatus);
    }

    private void completeSingleImageUpload(int type, String adId, int saveIndex, int imageIndex, String[] imageContents) {
        updateDatabase(type, adId, saveIndex, imageContents);
        if (imageIndex != 0) {
            this.imageIndex = imageIndex - 1;
            uploadSingleImage();
        } else {
            completeImageUpload();
        }
    }

    private void completeImageUpload() {
        closeImageUploadProgressDialog();
        imageUploadSuccess();
        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMediaUploadEvent, "succeed");
    }

    private void closeImageUploadProgressDialog() {
        if (progressDialog == null) {
            return;
        }
        progressDialog.dismiss();
    }

    private void imageUploadSuccess() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void imageUploadFailed() {
        closeImageUploadProgressDialog();
        showSimpleDialog(R.string.image_upload_failed_unknown_error);
        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMediaUploadEvent, "failed");
    }

    private void updateDatabase(int type, String adId, int imageIndex, String[] imageContents) {
        HashMap<String, Object> imageValues = new HashMap<>();
        imageValues.put(AppConstants.downloadUrl, imageContents[0]);
        imageValues.put(AppConstants.imageName, imageContents[1]);
        imageValues.put(AppConstants.imagePath, imageContents[2]);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("/" + DBConstants.adList + "/" + flatType + "/" + adId + "/" + DBConstants.images + "/" + DBConstants.imageKeyForDatabase + String.valueOf(imageIndex), imageValues);
        hashMap.put("/" + DBConstants.userAdList + "/" + getUid() + "/" + adId + "/" + DBConstants.images + "/" + DBConstants.imageKeyForDatabase + String.valueOf(imageIndex), imageValues);

        databaseReference.updateChildren(hashMap);
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

    private TextView previewMessage;

    private void initPreviewPhoto() {
        previewMessage = findViewById(R.id.previewMessage);
        RecyclerView previewPhotos = findViewById(R.id.previewPhotos);
        previewPhotos.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        previewPhotos.setLayoutManager(layoutManager);
        imagePreviewAdapter = new ImagePreviewAdapter();
        previewPhotos.setAdapter(imagePreviewAdapter);
    }

    private ImagePreviewAdapter imagePreviewAdapter;
    private ArrayList<ImageInfo> imagePreviewArray = new ArrayList<>();

    private class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.MyViewHolder> {

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
                        .apply(new RequestOptions().placeholder(R.drawable.image_loading).error(R.drawable.image_error))
                        .into(holder.previewPhoto);
            } else
                holder.previewPhoto.setImageResource(R.drawable.no_image_available);
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
                        deleteImage(getLayoutPosition());
                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String imageUri = imagePreviewArray.get(getLayoutPosition()).downloadUrl;
                        showImageDialog(imageUri);
                    }
                });
            }
        }
    }

    private void deleteConfirmationAlert(final String imagePath, final int imageIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_photo);
        builder.setIcon(R.mipmap.ic_launcher_round);
        builder.setMessage(R.string.do_you_want_to_delete_this_image_you_can_not_undo_this);

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteSingleImage(imagePath, imageIndex);
            }
        });

        builder.setNegativeButton(getString(R.string.no), null);
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteSingleImage(String imagePath, final int deleteImageIndex) {
        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyMediaDeleteEvent, adId);
        initStorageRef();
        showProgressDialog();
        storageReference
                .child(imagePath)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        closeProgressDialog();
                        removeContentFromDatabase(deleteImageIndex);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String message = exception.getMessage();
                        if (message != null && message.toLowerCase().contains("not exist")) {
                            removeContentFromDatabase(deleteImageIndex);
                        }
                        closeProgressDialog();
                    }
                });
    }

    private void removeContentFromDatabase(int deleteImageIndex) {
        int processImageIndex = processImagePathIndex(deleteImageIndex);
        if (processImageIndex > -1) {
//            databaseReference
//                    .child(DBConstants.adList)
//                    .child(flatType)
//                    .child(adId)
//                    .child(DBConstants.images)
//                    .child(DBConstants.imageKeyForDatabase + String.valueOf(processImageIndex))
//                    .removeValue();

            HashMap<String, Object> removeImage = new HashMap<>();
            removeImage.put("/" + DBConstants.adList + "/" + flatType + "/" + adId + "/" + DBConstants.images
                    + "/" + DBConstants.imageKeyForDatabase + String.valueOf(processImageIndex), null);
            removeImage.put("/" + DBConstants.userAdList + "/" + getUid() + "/" + adId + "/" + DBConstants.images
                    + "/" + DBConstants.imageKeyForDatabase + String.valueOf(processImageIndex), null);

            databaseReference.updateChildren(removeImage);
        }
        updateList(deleteImageIndex);
        isImageDeleted = true;
    }

    private void updateList(int position) {
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

    private void deleteImage(int position) {
        ImageInfo imageInfo;
        if (position < imagePreviewArray.size()) {
            imageInfo = imagePreviewArray.get(position);
            if (imageInfo != null && imageInfo.imagePath != null && !imageInfo.imagePath.isEmpty()) {
                deleteConfirmationAlert(imageInfo.imagePath, position);
                return;
            }
        }
        updateList(position);
    }

    private void updateSelectText(int selectSize) {
        if (selectSize <= 1) {
            updateTitle("(" + selectSize + ") item selected.");
        } else {
            updateTitle("(" + selectSize + ") item's selected.");
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

        if (imageUri != null) {
            if (!imageUri.startsWith("https")) {
                imageUri = "file://" + imageUri;
            }
            Glide.with(this)
                    .load(Uri.parse(imageUri))
                    .apply(new RequestOptions().placeholder(R.drawable.image_loading).error(R.drawable.image_error))
                    .into(zoomableImageView);
        } else {
            zoomableImageView.setImageResource(R.drawable.no_image_available);
        }
    }
}
