package com.to.let.bd.utils;

import android.content.SharedPreferences;

import com.to.let.bd.app.SmartToLetApp;

public class AppSharedPrefs {
    public static final String TAG = AppSharedPrefs.class.getName();
    private static final String prefsName = "stPrefs";

    private static SharedPreferences getSharedPreferences() {
        return SmartToLetApp.getContext().getSharedPreferences(prefsName, 0);
    }

    private static final String keyFirstTime = "firstTime";

    public static void setFirstTime(long firstTime) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(keyFirstTime, firstTime);
        editor.apply();
    }

    public static long getTime() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getLong(keyFirstTime, 0);
    }

    private static final String keyMobileNumber = "mobileNumber";

    public static void setMobileNumber(String mobileNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyMobileNumber, mobileNumber);
        editor.apply();
    }

    public static String getMobileNumber() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.getString(keyMobileNumber, null);
    }
}
