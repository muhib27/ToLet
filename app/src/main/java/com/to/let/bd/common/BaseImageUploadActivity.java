package com.to.let.bd.common;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.to.let.bd.R;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.UploadImageService;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseImageUploadActivity extends BaseActivity {

    protected abstract void imageUploadSuccess();

    protected abstract void imageUploadFailed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBroadcast();
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

    protected BroadcastReceiver mBroadcastReceiver;

    private ArrayList<String> imagePathList = new ArrayList<>();

    private int imageIndex = 0;

    protected void uploadImages(ArrayList<String> imagePathList, String adId, int type) {
        this.imagePathList.clear();
        this.imagePathList.addAll(imagePathList);
        imageIndex = imagePathList.size() - 1;
        showImageUploadProgressDialog();
        uploadSingleImage(type, adId);
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
                switch (intent.getAction()) {
                    case AppConstants.uploadError:

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

        TextView progressStatus = (TextView) progressDialog.findViewById(R.id.progressStatus);

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
        imageUploadSuccess();
        if (progressDialog == null) {
            return;
        }
        progressDialog.dismiss();
    }

    private void updateDatabase(int type, String adId, int imageIndex, String[] imageContents) {
        firebaseInit();

        HashMap<String, Object> adValues = new HashMap<>();
        adValues.put(AppConstants.downloadUrl, imageContents[0]);
        adValues.put(AppConstants.imageName, imageContents[1]);
        adValues.put(AppConstants.imagePath, imageContents[2]);

        HashMap<String, Object> childUpdates = new HashMap<>();
        if (type == AppConstants.adImageType) {
            childUpdates.put("/" + DBConstants.adList + "/" + adId + "/" + DBConstants.images + "/" + imageIndex, adValues);
        } else if (type == AppConstants.adMapImageType) {
            childUpdates.put("/" + DBConstants.adList + "/" + adId + "/" + DBConstants.map, adValues);
        }
        mDatabase.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });
    }

    private DatabaseReference mDatabase;

    private void firebaseInit() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance().getReference();
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

        ProgressBar progressBar = (ProgressBar) progressDialog.findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        TextView title = (TextView) progressDialog.findViewById(R.id.title);
        title.setText(R.string.image_upload_title);

        TextView progressMessage = (TextView) progressDialog.findViewById(R.id.progressMessage);
        progressMessage.setText(R.string.image_upload_message);

        TextView progressStatus = (TextView) progressDialog.findViewById(R.id.progressStatus);

        String imageUploadStatus = getString(R.string.image_upload_starting);
        progressStatus.setText(imageUploadStatus);
    }
}
