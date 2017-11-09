package com.to.let.bd.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by MAKINUL on 11/7/17.
 */

public class DateUtils {
    public static final String TAG = DateUtils.class.getName();

    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    public static Calendar getCalendar(final Date date) {
        final Calendar cal = getCalendar();
        cal.setTime(date);

        return cal;
    }

    public static Date getCurrentDate() {
        return getCalendar().getTime();
    }

    public static Date getCurrentDate(long currentTime) {
        return getCalendar(new Date(currentTime)).getTime();
    }

    public static Date getCurrentDateWithMillisecond() {
        return Calendar.getInstance().getTime();
    }

    public static final String format1 = "format1";
    public static final String format2 = "format2";
    public static final String format3 = "format3";
    public static final String format4 = "format4";

    public static String getFormattedDateString(final Date date, final String requestFor) {
        switch (requestFor) {
            case format1:
                return new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(date);
            case format2:
                return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date);
            default:
                return null;
        }
    }
}
