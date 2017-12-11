package com.to.let.bd.utils;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.to.let.bd.model.AdInfo;

public class MyAnalyticsUtil {
    private static final String TAG = MyAnalyticsUtil.class.getName();

    public MyAnalyticsUtil(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    private FirebaseAnalytics firebaseAnalytics;

    public void sendScreen(final String screenName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.LEVEL, screenName);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    public static final String keyNameLogin = "Login";

    private static final String keyFavItem = "favItem";

    private static final String keyAdId = "adId";
    private static final String keyUserId = "userId";

    public void favItem(AdInfo adInfo, String firebaseUid) {
        Bundle bundle = new Bundle();
        bundle.putString(keyAdId, adInfo.getAdId());
        bundle.putString(keyUserId, firebaseUid);
        firebaseAnalytics.logEvent(keyFavItem, bundle);
    }

    private static final String keyAdDetails = "adDetails";

    public void adDetails(AdInfo adInfo, String firebaseUid) {
        Bundle bundle = new Bundle();
        bundle.putString(keyAdId, adInfo.getAdId());
        bundle.putString(keyUserId, firebaseUid);
        firebaseAnalytics.logEvent(keyAdDetails, bundle);
    }

//    public static final String keyNameLogin = "Login";
//    public static final String keyNameRegistration = "Registration";
//    public static final String keyNameTour = "Tour";
//    public static final String keyNamePaymentBrowserActivity = "Payment_Browser_Activity";
//    public static final String keyNameTermsAndPolicy = "Terms_And_Policy";
//
//    public static final String DownloadBookEvent = "Download a book";
//    public static final String RateBookEvent = "Rate a book";
//    public static final String ViewReviewsEvent = "View Reviews";
//    public static final String ViewSummaryEvent = "View Summary";
//    public static final String ClickedOnDetailEvent = "Clicked on Detail";
//    public static final String SearchBookEvent = "Search Book";
//    public static final String GaveFeedbackEvent = "Gave Feedback";
//    public static final String RegisteredEvent = "Registered";
//    public static final String LogoutEvent = "Logout";
//    public static final String GuestLoginEvent = "Guest Login";
//    public static final String SignedInEvent = "Signed in";
//    public static final String StartedReadingEvent = "Started Reading";
//    public static final String CompletedPurchaseEvent = "Completed purchase";
//    public static final String WalletRecharge = "WalletBalance Recharge";
//    public static final String ChangeLanguage = "Change Language";
//    public static final String EnglishLanguage = "English Language";

//    public static void sendScreen(final String screenName) {
////		// Set screen name.
////		SheiBoiApplication.getGoogleTracker().setScreenName(screenName);
////
////		// Send a screen view.
////		SheiBoiApplication.getGoogleTracker().send(
////				new HitBuilders.ScreenViewBuilder().build());
////
////		// Clear the screen name field when we're done.
////		SheiBoiApplication.getGoogleTracker().setScreenName(null);
//
//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.LEVEL, screenName);
//        SheiBoiApplication.getGoogleAnalytics().logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
//
//        bundle = new Bundle();
//        bundle.putString(AppEventsConstants.EVENT_PARAM_LEVEL, screenName);
//        SheiBoiApplication.getFbAnalytics().logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, bundle);
//    }
//
//    public static void sendEvent(final String action, final String category, final String label) {
//        // Build and send an Event.
////		SheiBoiApplication.getGoogleTracker().send(
////				new HitBuilders.EventBuilder().setAction(action)
////						.setCategory(category).setLabel(label).build());
//
//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.LEVEL, label);
//        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category);
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, action);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
//        SheiBoiApplication.getFbAnalytics().logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, bundle);
//    }
//
//    public static void sendTimeDuration(final String timingCategory,
//                                        final long timingInterval, final String timingName,
//                                        final String timingLabel) {
//        // Build and send an Event.
////		SheiBoiApplication.getGoogleTracker().send(
////				new HitBuilders.TimingBuilder().setCategory(timingCategory)
////						.setValue(timingInterval).setVariable(timingName)
////						.setLabel(timingLabel).build());
//
//        Bundle bundle = new Bundle();
//        bundle.putLong(timingCategory, timingInterval);
//        bundle.putString(timingName, timingLabel);
//
//        SheiBoiApplication.getGoogleAnalytics().setSessionTimeoutDuration(timingInterval);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent("session", bundle);
//        SheiBoiApplication.getFbAnalytics().logEvent(AppEventsConstants.EVENT_NAME_TIME_BETWEEN_SESSIONS, bundle);
//    }
//
//    public static void registrationComplete() {
//        Bundle bundle = new Bundle();
//        bundle.putInt("Value", 1);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(RegisteredEvent.replace(" ", "_"), bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(RegisteredEvent);
//    }
//
//    public static void loginAsRegisteredUser() {
//        Bundle bundle = new Bundle();
//        bundle.putInt("Value", 1);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(SignedInEvent.replace(" ", "_"), bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(SignedInEvent);
//    }
//
//    public static void loginAsGuestUser() {
//        Bundle bundle = new Bundle();
//        bundle.putInt("Value", 1);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(GuestLoginEvent.replace(" ", "_"), bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(GuestLoginEvent);
//    }
//
//    public static void logoutUser() {
//        Bundle bundle = new Bundle();
//        bundle.putBoolean("Logout", true);
//        SheiBoiApplication.getGoogleAnalytics().logEvent(LogoutEvent.replace(" ", "_"), bundle);
//        SheiBoiApplication.getFbAnalytics().logEvent(LogoutEvent);
//    }
//
//    public static void rateEvent(int rateValue, String bookId) {
//        Bundle bundle = new Bundle();
//
//        bundle.putInt("rateValue", rateValue);
//        bundle.putString("bookId", bookId);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(RateBookEvent.replace(" ", "_"), bundle);
//        SheiBoiApplication.getFbAnalytics().logEvent(RateBookEvent, bundle);
//    }
//
//    public static void feedbackEvent(int rateValue, String rateTitle, String rateDescription, String bookId) {
//        Bundle bundle = new Bundle();
//
//        bundle.putInt("rateValue", rateValue);
//        bundle.putString("rateTitle", rateTitle);
//        bundle.putString("rateDescription", rateDescription);
//        bundle.putString("bookId", bookId);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(GaveFeedbackEvent.replace(" ", "_"), bundle);
//        SheiBoiApplication.getFbAnalytics().logEvent(GaveFeedbackEvent, bundle);
//    }
//
//    public static void downloadBook(String authorName, String bookName, String publisher) {
//        Bundle bundle = new Bundle();
//        bundle.putString("authorName", authorName);
//        bundle.putString("bookName", bookName);
//        bundle.putString("publisher", publisher);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(DownloadBookEvent.replace(" ", "_"), bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(DownloadBookEvent, bundle);
//    }
//
//    public static void cartBook(String authorName, String bookName, String publisher) {
//        Bundle bundle = new Bundle();
//        bundle.putString("authorName", authorName);
//        bundle.putString("bookName", bookName);
//        bundle.putString("publisher", publisher);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, bundle);
//    }
//
//    public static void walletRechargeSuccessful() {
//        Bundle bundle = new Bundle();
//        bundle.putBoolean("rechargeSuccessful", true);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent("walletRecharge", bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent("walletRecharge", bundle);
//    }
//
//    public static void purchaseSuccessful(String authorName, String bookName, String publisher,
//                                          String currency, String amount) {
//        Bundle bundle = new Bundle();
//        bundle.putString("authorName", authorName);
//        bundle.putString("bookName", bookName);
//        bundle.putString("publisher", publisher);
//        bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency);
//        bundle.putString(FirebaseAnalytics.Param.PRICE, amount);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(CompletedPurchaseEvent.replace(" ", "_"), bundle);
//
//        bundle = new Bundle();
//        bundle.putString("authorName", authorName);
//        bundle.putString("bookName", bookName);
//        bundle.putString("publisher", publisher);
//        bundle.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency);
//        bundle.putString("price", amount);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(CompletedPurchaseEvent, bundle);
//    }
//
//    public static void sendEventInfo(String eventName, String eventValue) {
//        Bundle bundle = new Bundle();
//        bundle.putInt(eventValue, 1);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(eventName.replace(" ", "_"), bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(eventName, bundle);
//    }
//
//    public static void startReadingBook(String authorName, String bookName, String publisher) {
//        Bundle bundle = new Bundle();
//        bundle.putString("authorName", authorName);
//        bundle.putString("bookName", bookName);
//        bundle.putString("publisher", publisher);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(StartedReadingEvent.replace(" ", "_"), bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(StartedReadingEvent, bundle);
//    }
//
//    public static void changeLanguage(String languageName, String userId) {
//        Bundle bundle = new Bundle();
//
//        bundle.putString("languageName", languageName);
//        bundle.putString("userId", userId);
//
//        SheiBoiApplication.getGoogleAnalytics().logEvent(ChangeLanguage.replace(" ", "_"), bundle);
//
//        SheiBoiApplication.getFbAnalytics().logEvent(ChangeLanguage, bundle);
//
//        if (languageName.equals("en")) {
//            sendEventInfo(EnglishLanguage, "Value");
//        }
//    }
}
