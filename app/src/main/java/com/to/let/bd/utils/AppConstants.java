package com.to.let.bd.utils;

public class AppConstants {
    public static final long textWatcherDelay = 2000;


    //-------------methods-----------
    // mobile number validation
    public static boolean isMobileNumberValid(String mobileNumber) {
        if (mobileNumber.length() < 11)
            return false;
        if (mobileNumber.startsWith("+88")) {
            mobileNumber = mobileNumber.replace("+88", "");
        }
        if (mobileNumber.startsWith("88")) {
            mobileNumber = mobileNumber.replace("88", "");
        }

        return mobileNumber.length() >= 11 && (mobileNumber.startsWith("016") || mobileNumber.startsWith("017") || mobileNumber.startsWith("018") || mobileNumber.startsWith("019"));

    }

    private boolean mobileStartingValidation() {

        return false;
    }
}
