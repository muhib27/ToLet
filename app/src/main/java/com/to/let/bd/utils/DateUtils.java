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

    public static final String format1 = "format1";//dd-MM-yyyy
    public static final String format2 = "format2";//MMM dd, yyyy
    public static final String format3 = "format3";//MMM yyyy
    public static final String format4 = "format4";//yyyy-MM-dd

    public static String getFormattedDateString(final Date date, final String requestFor) {
        switch (requestFor) {
            case format1:
                return new SimpleDateFormat(dateFormat1, Locale.US).format(date);
            case format2:
                return new SimpleDateFormat(dateFormat2, Locale.US).format(date);
            case format3:
                return new SimpleDateFormat(dateFormat3, Locale.US).format(date);
            case format4:
                return new SimpleDateFormat(dateFormat4, Locale.US).format(date);
            default:
                return null;
        }
    }

    private static final String dateFormat1 = "dd-MM-yyyy";
    private static final String dateFormat2 = "MMMM dd, yyyy";
    private static final String dateFormat3 = "MMMM yyyy";
    private static final String dateFormat4 = "yyyy-MM-dd";

    public static Date getDate(final String[] dateArray, final String requestFor) {
        String date = dateArray[0] + "-" + dateArray[1] + "-" + dateArray[2];
        return getDate(date, requestFor);
    }

    public static Date getDate(final String date, final String requestFor) {
        SimpleDateFormat simpleDateFormat = null;
        switch (requestFor) {
            case format1:
                simpleDateFormat = new SimpleDateFormat(dateFormat1, Locale.US);
            case format2:
                simpleDateFormat = new SimpleDateFormat(dateFormat2, Locale.US);
            case format3:
                simpleDateFormat = new SimpleDateFormat(dateFormat3, Locale.US);
            case format4:
                simpleDateFormat = new SimpleDateFormat(dateFormat4, Locale.US);
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

    public static String[] splittedDate(long yearMonthDay) {//year=0, month=1, day=2
        String value = String.valueOf(yearMonthDay);
        String[] splittedDate = new String[3];
        if (value.length() == 8) {
            splittedDate[0] = value.substring(0, 4);
            splittedDate[1] = value.substring(4, 6);
            splittedDate[2] = value.substring(6);
        }
        return splittedDate;
    }

    public static String getRentDateString(long yearMonthDay) {
        String[] splittedDate = splittedDate(yearMonthDay);
        return getFormattedDateString(getDate(splittedDate, format4), format2);
    }

    public static long differenceBetweenToday(long selectedDateTimestamp) {
        long differenceTime = selectedDateTimestamp - System.currentTimeMillis();
        long elapsedDays = 0;
        if (differenceTime > 1) {
            elapsedDays = (differenceTime / (60 * 60 * 24 * 1000)) + 1;
        }

        return elapsedDays;
    }
}
