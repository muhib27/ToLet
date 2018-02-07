package com.to.let.bd.common;

import android.support.v4.app.Fragment;
import android.util.Log;

import com.to.let.bd.R;

public class BaseFragment extends Fragment {
    private final String TAG = BaseFragment.class.getSimpleName();

    protected void showLog() {
        showLog(R.string.app_name);
    }

    protected void showLog(int resourceId) {
        showLog(getString(resourceId));
    }

    protected void showLog(String message) {
        Log.v(TAG, message);
    }

    public void showProgressDialog() {
        showProgressDialog("", getString(R.string.loading));
    }

    public void showProgressDialog(String message) {
        showProgressDialog("", message);
    }

    public void showProgressDialog(String title, String message) {
        showProgressDialog(title, message, false);
    }

    public void showProgressDialog(String title, String message, boolean cancelable) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showProgressDialog(title, message, cancelable);
    }

    public void closeProgressDialog() {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).closeProgressDialog();
    }

    public void showSimpleDialog(String message) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showSimpleDialog(message);
    }

    public void showSimpleDialog(int messageResourceId) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showSimpleDialog(messageResourceId);
    }
}
