package com.to.let.bd.utils;

import java.text.ParseException;
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
            case format3:
                return new SimpleDateFormat("MMM yyyy", Locale.US).format(date);
            default:
                return null;
        }
    }

    public static Date getDate(final String date, final String requestFor) {
        SimpleDateFormat simpleDateFormat = null;
        switch (requestFor) {
            case format1:
                simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            case format2:
                simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            case format3:
                simpleDateFormat = new SimpleDateFormat("MMM yyyy", Locale.US);
            case format4:
                simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        }

        if (simpleDateFormat != null) {
            try {
                return simpleDateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return new Date(0);
    }

    public static String getRentDateString(long yearMonthDay) {
        String value = String.valueOf(yearMonthDay);
        if (value.length() == 8) {
            String year = value.substring(0, 4);
            String month = value.substring(4, 6);
            String day = value.substring(6);

            return getFormattedDateString(getDate(year + "-" + month + "-" + day, format4), format2);
        }
        return null;
    }
}
