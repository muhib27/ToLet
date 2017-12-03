package com.to.let.bd.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.to.let.bd.app.SmartToLetApp;

import java.io.IOException;

public class NetworkConnection {
    public static final String TAG = NetworkConnection.class.getName();

    private static final NetworkConnection instance = new NetworkConnection();

    private NetworkConnection() {
    }

    public static NetworkConnection getInstance() {
        return instance;
    }

    public boolean isAvailable() {
        final NetworkInfo info = getNetworkInfo();
        if (info != null) {
            return info.isConnected();
        }
        return false;
    }

    public boolean isWifiAvailable() {
        final NetworkInfo info = getNetworkInfo();
        if (info != null && info.isConnected()) {
            return info.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    public boolean isMobileAvailable() {
        final NetworkInfo info = getNetworkInfo();
        if (info != null && info.isConnected()) {
            return info.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }

    private NetworkInfo getNetworkInfo() {
        final ConnectivityManager connMgr = (ConnectivityManager) SmartToLetApp
                .getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }

    public boolean isReallyConnected() {
        String command = "ping -c 1 google.com";
        try {
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
