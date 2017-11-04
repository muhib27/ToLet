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

public class AppConstants {
    public static final long autoScrollDuration = 5000;
    public static final int GOOGLE_SIGN_IN = 1001;
    public static final int notifyIdUpload = 1002;
    public static final int adImageType = 1003;
    public static final int adMapImageType = 1004;

    public static final double defaultLatitude = 23.8103d;
    public static final double defaultLongitude = 90.4125d;

    public static final String firebaseAccountConflictMessage1 = "This credential is already associated with a different user account.";
    public static final String firebaseAccountConflictMessage2 = "different user account";
    public static final String mediaExtra = "mediaExtra";
    public static final String keyType = "keyType";
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
    public static final String storageCommonFolderName = "-KpcxfSHO2AOT3yCKgqd";
    public static final String imageName = "imageName";
    public static final String imagePath = "imagePath";
    public static final String progress = "progress";

    public static final long textWatcherDelay = 2000;

    //-------------methods-----------
    public Bitmap writeOnDrawable(Context context, int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);

        final float scale = context.getResources().getDisplayMetrics().density;

        int size = (int) (12.0 * scale + 0.5f);

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(size);
        paint.setColor(Color.WHITE);

        Typeface tf = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        paint.setTypeface(tf);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(text, bm.getWidth() / 2, (float) ((bm.getHeight() / 2) - 1.5), paint);

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
            final View inflatedView = inflater.inflate(R.layout.row_perticular_view, parentLay, false);
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
                String title = (subtitle.split(" "))[1].replace("'s", "");
                updatePickerView(view, title, subtitle);

                if (popupMenuClickListener != null)
                    popupMenuClickListener.onItemClick(viewType, item.getItemId());
                return true;
            }
        });
        popup.show(); //showing popup menu
    }
}
