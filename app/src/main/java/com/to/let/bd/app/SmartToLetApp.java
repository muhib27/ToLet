package com.to.let.bd.app;

import android.app.Application;
import android.content.Context;

public class SmartToLetApp extends Application {
    private static SmartToLetApp sInstance;
    private static final String TAG = SmartToLetApp.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static Context getContext() {
        return sInstance;
    }
}