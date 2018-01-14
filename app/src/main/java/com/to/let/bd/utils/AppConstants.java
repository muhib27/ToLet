package com.to.let.bd.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.to.let.bd.R;
import com.to.let.bd.activities.AdDetailsActivity;
import com.to.let.bd.model.AdInfo;

import java.text.DecimalFormat;
import java.util.Calendar;

public class AppConstants {
    public static final long autoScrollDuration = 5000;
    public static final int GOOGLE_SIGN_IN = 1001;
    public static final int notifyIdUpload = 1002;
    public static final int adImageType = 1003;
    public static final int adMapImageType = 1004;
    public static final int placeAutoComplete = 1005;
    public static final int phoneHint = 1006;
    public static final int REQUEST_CHECK_SETTINGS = 1007;

    public static final double defaultLatitude = 23.8103d;
    public static final double defaultLongitude = 90.4125d;

    public static final String firebaseAccountConflictMessage1 = "This credential is already associated with a different user account.";
    public static final String firebaseAccountConflictMessage2 = "different user account";
    public static final String mediaExtra = "mediaExtra";
    public static final String keyType = "keyType";
    public static final String keySubAdListType = "keySubAdListType";
    public static final String keyImageList = "keyImageList";

    public static final int subQueryFav = 0;
    public static final int subQueryMy = 1;
    public static final int subQueryNearest = 2;
    public static final int subQuerySmart = 3;
    public static final int subQueryAll = 100;

    // maximum image you can upload for single ad
    public static final int maximumImage = 3;

    public static final String actionUpload = "actionUpload";
    public static final String fileUri = "fileUri";
    public static final String photos = "photos";
    public static final String adPhotos = "adPhotos";
    public static final String uploadComplete = "uploadComplete";
    public static final String uploadProgress = "uploadProgress";
    public static final String uploadError = "uploadError";
    public static final String imageContents = "imageContents";
    public static final String downloadUrl = "downloadUrl";
    public static final String imageIndex = "imageIndex";
    public static final String imageName = "imageName";
    public static final String imagePath = "imagePath";
    public static final String progress = "progress";

    public static final String keyAdInfo = "adInfo";

    // predefined rent type
    public static final long textWatcherDelay = 2000;

    //-------------methods-----------
    public static Bitmap writeOnDrawable(Context context, int drawableId, String smallText) {

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        final float scale = context.getResources().getDisplayMetrics().density;

        int size = (int) (15.0 * scale);

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size);
        paint.setColor(Color.WHITE);
        Typeface tf = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
        paint.setTypeface(tf);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(smallText, bm.getWidth() / 2, (bm.getHeight() / 10) * 4, paint);

        return bm;
    }

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
        if (mobileNumber.startsWith("088")) {
            mobileNumber = mobileNumber.replace("088", "");
        }

        return mobileNumber.length() == 11 && (mobileNumber.startsWith("016") ||
                mobileNumber.startsWith("017") || mobileNumber.startsWith("018") ||
                mobileNumber.startsWith("019"));
    }

    // format phone number as simple
    // like "+8801670688688" to "01670688688"
    public static String formatAsSimplePhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("+88")) {
            phoneNumber = phoneNumber.replace("+88", "");
        }
        if (phoneNumber.startsWith("088")) {
            phoneNumber = phoneNumber.replace("088", "");
        }
        if (phoneNumber.startsWith("88")) {
            phoneNumber = phoneNumber.replace("88", "");
        }
        return phoneNumber;
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

    public interface PopupMenuClickListener {
        void onItemClick(String viewType, int selectedPosition);
    }

    public static void addParticularView(final Context context, LinearLayout parentLay,
                                         final String[] viewTypes,
                                         final int[] startPositions,
                                         final int[] endPositions,
                                         final int[] defaultPositions,
                                         final PopupMenuClickListener popupMenuClickListener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i = 0; i < viewTypes.length; i++) {
            final View inflatedView = inflater.inflate(R.layout.row_particular_view, parentLay, false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            inflatedView.setLayoutParams(layoutParams);
            final int selectedPosition = i;
            inflatedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupMenu(context, inflatedView, viewTypes[selectedPosition],
                            startPositions[selectedPosition], endPositions[selectedPosition],
                            popupMenuClickListener);
                }
            });

            int count = (startPositions[i] + defaultPositions[i]);
            inflatedView.setTag(viewTypes[i]);
            updatePickerView(inflatedView, viewTypes[i], (count + " " + viewTypes[i] + (count > 1 ? "'s" : "")));
            parentLay.addView(inflatedView);
        }
    }

    public static void updatePickerView(View v, String title, String subTitle) {
        if (v instanceof ViewGroup) {
            ((TextView) v.findViewById(R.id.title)).setText(title);
            ((TextView) v.findViewById(R.id.subTitle)).setText(subTitle);
        }
    }

    private static void popupMenu(Context context, final View view,
                                  final String viewType,
                                  final int startPosition,
                                  final int endPosition,
                                  final PopupMenuClickListener popupMenuClickListener) {
        PopupMenu popup = new PopupMenu(context, view);

        for (int numberOfView = startPosition; numberOfView < endPosition; numberOfView++) {
            String s;
            if (numberOfView < 1) {
                s = "No " + viewType;
            } else if (numberOfView < 2) {
                s = numberOfView + " " + viewType;
            } else {
                s = numberOfView + " " + viewType + "'s";
            }

            int position = numberOfView - startPosition;
            popup.getMenu().add(0, position, Menu.NONE, s);
        }

        //popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String subtitle = String.valueOf(item.getTitle());
                updatePickerView(view, viewType, subtitle);

                if (popupMenuClickListener != null)
                    popupMenuClickListener.onItemClick(viewType, item.getItemId());
                return true;
            }
        });
        popup.show(); //showing popup menu
    }

    public static String rentFormatter(long rent) {
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        return formatter.format(rent);
    }

    public static String twoDigitIntFormatter(int number) {
        DecimalFormat formatter = new DecimalFormat("00");
        return formatter.format(number);
    }

    public static String flatDescription(Context context, AdInfo adInfo) {
        String adDescription = "";
        if (adInfo.familyInfo != null) {
            boolean isItDulpex = adInfo.familyInfo.isItDuplex;
            if (isItDulpex)
                adDescription = "Duplex house with ";
            adDescription = adDescription + adInfo.familyInfo.bedRoom + "bed " +
                    adInfo.familyInfo.bathroom + "bath ";

            if (context instanceof AdDetailsActivity)
                if (adInfo.familyInfo.balcony > 0) {
                    adDescription = adDescription + adInfo.familyInfo.balcony + "balcony";
                }

            if (adInfo.flatSpace > 0) {
                adDescription = adDescription + " " + adInfo.flatSpace + "sqft";
            }

            if (adInfo.familyInfo.hasDrawingDining) {
                adDescription = adDescription + " with drawing & dining";
            }
        } else if (adInfo.messInfo != null) {
            String[] messTypeArray = context.getResources().getStringArray(R.array.mess_member_type_array);
            if (!(context instanceof AdDetailsActivity))
                adDescription = messTypeArray[adInfo.messInfo.memberType] + " member ";
            adDescription += adInfo.messInfo.numberOfSeat + "seat in " +
                    adInfo.messInfo.numberOfRoom + "room ";
        } else if (adInfo.subletInfo != null) {
            String[] subletTypeArray = context.getResources().getStringArray(R.array.sublet_type_array);
            adDescription = adInfo.subletInfo.subletType >= 3 ? adInfo.subletInfo.subletTypeOthers :
                    subletTypeArray[adInfo.subletInfo.subletType];

            adDescription += " with ";

            String[] subletBathTypeArray = context.getResources().getStringArray(R.array.sublet_bath_type_array);
            adDescription += subletBathTypeArray[adInfo.subletInfo.bathroomType] + " bath";
        }
        return adDescription;
    }
}
