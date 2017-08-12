package com.to.let.bd.utils;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.to.let.bd.R;

/**
 * Service to handle uploading files to Firebase Storage.
 */
public class UploadImageService extends BaseTaskService {

    private static final String TAG = UploadImageService.class.getSimpleName();

    // [START declare_ref]
    private StorageReference mStorageRef;
    // [END declare_ref]

    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        if (SmartToLetConstants.actionUpload.equals(intent.getAction())) {
            int type = intent.getIntExtra(SmartToLetConstants.keyType, -1);
            String adId = intent.getStringExtra(SmartToLetConstants.adId);
            if (adId == null || adId.isEmpty()) {
                adId = SmartToLetConstants.storageCommonFolderName;
            }
            int imageIndex = intent.getIntExtra(SmartToLetConstants.imageIndex, -1);
            uploadFromUri(intent, type, adId, imageIndex);
        }

        return START_REDELIVER_INTENT;
    }

    // [START upload_from_uri]
    private void uploadFromUri(final Intent intent, final int type, final String adId, final int imageIndex) {
        // [START_EXCLUDE]
        taskStarted();
        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        StorageReference storageReference = null;
        UploadTask uploadTask = null;
        if (type == SmartToLetConstants.adImageType) {
            final Uri fileUri = intent.getParcelableExtra(SmartToLetConstants.fileUri);
            storageReference = mStorageRef
                    .child(SmartToLetConstants.photos)
                    .child(SmartToLetConstants.adPhotos)
                    .child(adId)
                    .child(String.valueOf(imageIndex) + ".jpg");
            uploadTask = storageReference.putFile(fileUri);
        } else if (type == SmartToLetConstants.adMapImageType) {
            storageReference = mStorageRef
                    .child(SmartToLetConstants.photos)
                    .child(SmartToLetConstants.adPhotos)
                    .child(adId)
                    .child(getString(R.string.map) + ".jpg");

            final byte[] fileUri = intent.getByteArrayExtra(SmartToLetConstants.fileUri);
            uploadTask = storageReference.putBytes(fileUri);
        }

        if (uploadTask == null) {
            broadcastUploadFinished(type, adId, imageIndex, null);
            taskCompleted();
            return;
        }

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Get the public download URL
                String[] imageContents = new String[3];//0=public download url, 1=image name, 2=image path(firebase)
                Uri downloadUrl = taskSnapshot.getMetadata() == null ? null : taskSnapshot.getMetadata().getDownloadUrl();

                imageContents[0] = downloadUrl == null ? null : downloadUrl.toString();
                imageContents[1] = taskSnapshot.getMetadata() == null ? null : taskSnapshot.getMetadata().getName();
                imageContents[2] = taskSnapshot.getMetadata() == null ? null : taskSnapshot.getMetadata().getPath();

                // [START_EXCLUDE]
//                broadcastUploadFinished(imageContents, type, imageIndex);
                broadcastUploadFinished(type, adId, imageIndex, imageContents);
                taskCompleted();
                // [END_EXCLUDE]
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // [START_EXCLUDE]
                broadcastUploadFinished(type, adId, imageIndex, null);
                taskCompleted();
                // [END_EXCLUDE]
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                long transferredByte = taskSnapshot.getBytesTransferred();
                long totalByte = taskSnapshot.getTotalByteCount();
                double progress = ((double) transferredByte / totalByte) * 100;
                if (progress > lastProgress + 1) {
                    lastProgress = progress;
                    broadcastUploadingProgress(type, adId, imageIndex, progress);
                }
            }
        });
    }
    // [END upload_from_uri]

    private double lastProgress = 0;

    /**
     * Broadcast uploading progress
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadingProgress(int type, String adId, int imageIndex, double progress) {
        Intent broadcast = new Intent(SmartToLetConstants.uploadProgress)
                .putExtra(SmartToLetConstants.keyType, type)
                .putExtra(SmartToLetConstants.adId, adId)
                .putExtra(SmartToLetConstants.imageIndex, imageIndex)
                .putExtra(SmartToLetConstants.progress, (int) progress);
        return LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
    }

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(int type, String adId, int imageIndex, String[] imageContents) {
        boolean success = imageContents != null && imageContents[0] != null;
        String action = success ? SmartToLetConstants.uploadComplete : SmartToLetConstants.uploadError;
        Intent broadcast = new Intent(action)
                .putExtra(SmartToLetConstants.keyType, type)
                .putExtra(SmartToLetConstants.adId, adId)
                .putExtra(SmartToLetConstants.imageIndex, imageIndex)
                .putExtra(SmartToLetConstants.imageContents, imageContents);
        return LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
    }

//    /**
//     * Show a notification for a finished upload.
//     */
//    private void uploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
//        // Set message and icon based on success or failure
//        boolean success = downloadUrl != null;
//        String message = success ? "Upload finished" : "Upload failed";
//        int icon = success ? R.drawable.ic_check_white_24 : R.drawable.ic_error_white_24dp;
//    }

    /**
     * Show notification with an indeterminate upload progress bar.
     */

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartToLetConstants.uploadComplete);
        filter.addAction(SmartToLetConstants.uploadError);
        filter.addAction(SmartToLetConstants.uploadProgress);
        return filter;
    }
}
