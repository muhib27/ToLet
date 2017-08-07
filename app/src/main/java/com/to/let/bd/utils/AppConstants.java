package com.to.let.bd.utils;

import android.content.Context;
import android.widget.EditText;

import com.to.let.bd.R;

public class AppConstants {
    public static final long textWatcherDelay = 2000;


    //-------------methods-----------
    // mobile number validation
    private static boolean isMobileNumberValid(String mobileNumber) {
        if (mobileNumber.length() < 11)
            return false;
        if (mobileNumber.startsWith("+88")) {
            mobileNumber = mobileNumber.replace("+88", "");
        }
        if (mobileNumber.startsWith("88")) {
            mobileNumber = mobileNumber.replace("88", "");
        }

        return mobileNumber.length() == 11 && (mobileNumber.startsWith("016") ||
                mobileNumber.startsWith("017") || mobileNumber.startsWith("018") ||
                mobileNumber.startsWith("019"));
    }

    public static boolean isMobileNumberValid(Context context, EditText mobileNumber) {
        if (mobileNumber == null)
            return false;

        if (mobileNumber.getText().length() == 0) {
            mobileNumber.setError(context.getString(R.string.error_field_required));
            mobileNumber.requestFocus();
            return false;
        }

        if (!isMobileNumberValid(mobileNumber.getText().toString())) {
            mobileNumber.setError(context.getString(R.string.error_valid_mobile_number));
            mobileNumber.requestFocus();
            return false;
        }

        return true;
    }

    private boolean mobileStartingValidation() {

        return false;
    }
}
