package com.to.let.bd.fcm;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.to.let.bd.R;

import java.util.Map;

public class MyFbMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFbMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String body = "";
        String title = getString(R.string.app_name) + " Message";

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> dataMap = remoteMessage.getData();
            if (dataMap.containsKey("body")) {
                body = dataMap.get("body");
            }

            if (dataMap.containsKey("title")) {
                title = dataMap.get("title");
            }
        }

        Bundle data = new Bundle();
        data.putString("title", title);
        data.putString("body", body);

//        data.putString("template", template);
//        //
//        Intent openableIntent = null;
//        if (SBConstants.GCM_MESSAGE_FOR_APP_UPDATE
//                .equalsIgnoreCase(template)) {
//            openableIntent = getIntentForAppUpdate(data);
//            title = "SheiBoi version " + title + " update is now available.";
//            notificationId = SBConstants.GCM_STATUS_BAR_NOTIFICATION_APP_UPDATE_ID;
//        } else if (SBConstants.GCM_MESSAGE_FOR_NEW_BOOK_RELEASE
//                .equalsIgnoreCase(template)) {
////            BookDetailsActivity.mSelectedBookStoreBook = null;
//            openableIntent = getIntent();
//            data.putString(SBConstants.KEY_REQUEST_TYPE, template);
//            data.putString(SBConstants.KEY_BOOK_ID, bookId);
//            data.putString(SBConstants.KEY_BOOK_TITLE, bookName);
//            notificationId = SBConstants.GCM_STATUS_BAR_NOTIFICATION_APP_NEW_BOOK_RELEASE_ID;
//        } else if (SBConstants.GCM_MESSAGE_FOR_TourScreeen
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_LOGIN
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_REGISTRATION
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_RECENT
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_MY_LIBRARY
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_HELP
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_INFO_ABOUT
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_INFO_PRIVACY
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_INFO_TERMS_OF_USES
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_BOOKSTORE_TAB_ALL_BOOKS
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_BOOKSTORE_TAB_FREEBOOKS
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_BOOKSTORE_TAB_RECENTBOOKS
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_BOOKSTORE_TAB_TOPRATEDBOOKS
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_BOOKSTORE_TAB_AUTHORS
//                .equalsIgnoreCase(template)
//                || SBConstants.GCM_MESSAGE_FOR_BOOKSTORE_TAB_CATEGORYS
//                .equalsIgnoreCase(template)) {
//            openableIntent = getIntent();
//            data.putString(SBConstants.KEY_REQUEST_TYPE, template);
//            notificationId = SBConstants.GCM_STATUS_BAR_NOTIFICATION_OPEN_SPECIFIC_SCREEN_ID;
//        } else {
//            return;
//        }
//
//        if (openableIntent == null) {
//            return;
//        }
//
//        //
//        buildNotification(openableIntent, data, title, body, notificationId);
    }

    private Intent getIntentForAppUpdate(final Bundle data) {
//        String appVersion = data.getString("title");
//        final String existingAppVersion = SheiBoiApplication.getVersionName();
//
//        if (appVersion == null ||
//                appVersion.isEmpty() ||
//                !appVersion.equals(existingAppVersion)) {
//
//            final String appPackageName = SheiBoiApplication.getAppPackageName();
//            try {
//                return new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("market://details?id=" + appPackageName));
//            } catch (android.content.ActivityNotFoundException anfe) {
//                return new Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse("https://play.google.com/store/apps/details?id="
//                                + appPackageName));
//            }
//        }
        return null;
    }

//    private Intent getIntent() {
//        return new Intent(this, SplashActivity.class);
//    }
//
//    private void buildNotification(Intent openableIntent, final Bundle data,
//                                   final String title, final String body,
//                                   final int notificationId) {
//
//        openableIntent.putExtras(data);
//        // openableIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        // Sets the Activity to start in a new, empty task
//        openableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                notificationId, openableIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        final Uri defaultSoundUri = RingtoneManager
//                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        final NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this)
//                        .setContentTitle(title)
//                        .setContentText(body)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent);
//
//        if (!SdkVersion.isLolipopAndAbove()) {
//            notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
//        } else {
//            notificationBuilder.setSmallIcon(R.drawable.ic_stat_push_notification_lolipop);
//            notificationBuilder.setColor(getResources().getColor(R.color.orange));
//        }
//        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        // System.out.println("----------------- sbgcmLis- "+title+" - "+notificationId);
//        notificationManager.notify(notificationId, notificationBuilder.build());
//    }
}
