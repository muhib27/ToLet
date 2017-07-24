package com.to.let.bd.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.to.let.bd.R;
import com.to.let.bd.activities.MediaActivity;

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

    private NotificationManager manager;
    private NotificationCompat.Builder builder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_upload_icon)
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setAutoCancel(false);

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
    private void uploadFromUri(final Intent intent, final int type, String adId, final int imageIndex) {
        final Uri fileUri = intent.getParcelableExtra(SmartToLetConstants.fileUri);

        // [START_EXCLUDE]
        taskStarted();

        builder.setContentText("upload started...").setProgress(0, 0, true);
        manager.notify(SmartToLetConstants.notifyIdUpload, builder.build());
        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        StorageReference storageReference = null;
        if (type == SmartToLetConstants.adImageType) {
            storageReference = mStorageRef
                    .child(SmartToLetConstants.photos)
                    .child(SmartToLetConstants.adPhotos)
                    .child(adId)
                    .child(String.valueOf(imageIndex) + ".jpg");
        }

        if (storageReference == null) {
            broadcastUploadFinished(null, type, imageIndex);
            if (imageIndex == 0) {
                showUploadFinishedNotification(null, fileUri);
            }
            taskCompleted();
            return;
        }

        storageReference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Upload succeeded

                // Get the public download URL
                String[] imageContents = new String[3];
                Uri downloadUrl = taskSnapshot.getMetadata() == null ? null : taskSnapshot.getMetadata().getDownloadUrl();
                imageContents[0] = downloadUrl == null ? null : downloadUrl.toString();
                imageContents[1] = taskSnapshot.getMetadata() == null ? null : taskSnapshot.getMetadata().getName();
                imageContents[2] = taskSnapshot.getMetadata() == null ? null : taskSnapshot.getMetadata().getPath();

                // [START_EXCLUDE]
                broadcastUploadFinished(imageContents, type, imageIndex);
                if (imageIndex == 0) {
                    showUploadFinishedNotification(downloadUrl, fileUri);
                }
                taskCompleted();
                // [END_EXCLUDE]
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Upload failed
                Log.w(TAG, "uploadFromUri:onFailure", exception);

                // [START_EXCLUDE]
                broadcastUploadFinished(null, type, imageIndex);
                if (imageIndex == 0) {
                    showUploadFinishedNotification(null, fileUri);
                }
                taskCompleted();
                // [END_EXCLUDE]
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                long transferredByte = taskSnapshot.getBytesTransferred();
                long totalByte = taskSnapshot.getTotalByteCount();

                double progress = ((double) transferredByte / totalByte) * 100;
                if (progress >= 1) {
                    builder.setContentText("uploading...").setProgress(100, (int) progress, false);
                    manager.notify(SmartToLetConstants.notifyIdUpload, builder.build());
                }
            }
        });
    }
    // [END upload_from_uri]

    /**
     * Broadcast finished upload (success or failure).
     *
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(String[] imageContents, int type, int imageIndex) {
        boolean success = imageContents[0] != null;
        String action = success ? SmartToLetConstants.uploadComplete : SmartToLetConstants.uploadError;
        Intent broadcast = new Intent(action)
                .putExtra(SmartToLetConstants.keyType, type)
                .putExtra(SmartToLetConstants.imageIndex, imageIndex)
                .putExtra(SmartToLetConstants.imageContents, imageContents);
        return LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished upload.
     */
    private void showUploadFinishedNotification(@Nullable Uri downloadUrl, @Nullable Uri fileUri) {
        // Set message and icon based on success or failure
        boolean success = downloadUrl != null;
        String message = success ? "Upload finished" : "Upload failed";
        int icon = success ? R.drawable.ic_check_white_24 : R.drawable.ic_error_white_24dp;

        builder.setSmallIcon(icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setOngoing(false)
                .setAutoCancel(true);
        manager.notify(SmartToLetConstants.notifyIdUpload, builder.build());
    }

    /**
     * Show notification with an indeterminate upload progress bar.
     */

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(SmartToLetConstants.uploadComplete);
        filter.addAction(SmartToLetConstants.uploadError);
        return filter;
    }
}
