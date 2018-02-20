package com.to.let.bd.common;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.to.let.bd.R;
import com.to.let.bd.activities.AdDetailsActivity;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.activities.SubAdListActivity;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DateUtils;
import com.to.let.bd.utils.MyAnalyticsUtil;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected void updateTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setSelected(true);
            toolbarTitle.setText(title);
        }
    }

    private ProgressDialog progressDialog;

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
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, title, message);
            progressDialog.setCancelable(cancelable);
        } else {
            progressDialog.show();
        }
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showLog(String message) {
        if (message == null)
            message = "null";
        Log.d(TAG, message);
    }

    public void showLog() {
        showLog("Test log");
    }

    public void showToast() {
        showToast(R.string.app_name);
    }

    public void showToast(int resourceId) {
        showToast(getString(resourceId));
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean isEmailValid(String emailAddress) {
        return Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
    }

    public boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
//        LoginManager.getInstance().logOut();
    }

//    public void firebaseAppInvites() {
//        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.app_name))
//                .setMessage(getString(R.string.invitation_message))
//                .setDeepLink(Uri.parse("https://z55f5.app.goo.gl/eaFH"))
//                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
//                .setCallToActionText("CallToActionText")
//                .build();
//        startActivityForResult(intent, KgbhsConstants.firebaseInviteType);
//    }
//
//    public void sendGraphRequest() {
//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/{app-request-id}",
//                null,
//                HttpMethod.GET,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//            /* handle the result */
//                    }
//                }
//        ).executeAsync();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//
//        MenuItem loginLogoutItem = menu.findItem(R.id.action_login_logout);
//        if (!KgbhsConstants.isValidUserForAction()) {
//            loginLogoutItem.setIcon(R.drawable.login_icon);
//            loginLogoutItem.setTitle(R.string.login);
//        } else {
//            loginLogoutItem.setIcon(R.drawable.logout_icon);
//            loginLogoutItem.setTitle(R.string.log_out);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_login_logout) {
//            if (!KgbhsConstants.isValidUserForAction()) {
//                startLogin();
//            } else {
//                logout();
//                finish();
//            }
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    public void startLogin() {
//        Intent authenticationIntent = new Intent(this, LoginActivity.class);
//        authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivityForResult(authenticationIntent, KgbhsConstants.LOGIN_RESULT);
//    }

    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getUid();
    }

    public static boolean isRegisteredUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && !user.isAnonymous();
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

//    public void gotoSpecificActivity(Intent intent, int type) {
//        FirebaseUser firebaseUser = getCurrentUser();
//        if (firebaseUser == null || firebaseUser.isAnonymous()) {
//            Intent loginIntent = new Intent(this, LoginActivity.class);
//            startActivityForResult(loginIntent, type);
//            return;
//        }
//        startActivity(intent);
//    }

    protected void startNewAdActivity() {
        Intent newAdIntent = new Intent(this, NewAdActivity2.class);
        newAdIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(newAdIntent, AppConstants.newAdType);
    }

    public void startAdDetailsActivity(AdInfo adInfo) {
        Intent adDetailsIntent = new Intent(this, AdDetailsActivity.class);
//        adDetailsIntent.putExtra(DBConstants.adId, adInfo.getAdId());
//        adDetailsIntent.putExtra(DBConstants.flatRent, adInfo.getFlatRent());
//        adDetailsIntent.putExtra(DBConstants.othersFee, adInfo.getOthersFee());

//        adDetailsIntent.putExtra(DBConstants.bedRoom, adInfo.getBedRoom());
//        adDetailsIntent.putExtra(DBConstants.bathroom, adInfo.getBathroom());
//        adDetailsIntent.putExtra(DBConstants.balcony, adInfo.getBalcony());

//        adDetailsIntent.putExtra(DBConstants.startingDate, adInfo.getStartingDate());
//        adDetailsIntent.putExtra(DBConstants.startingMonth, adInfo.getStartingMonth());
//        adDetailsIntent.putExtra(DBConstants.startingYear, adInfo.getStartingYear());
//
//        adDetailsIntent.putExtra(DBConstants.latitude, adInfo.getLatitude());
//        adDetailsIntent.putExtra(DBConstants.longitude, adInfo.getLongitude());
//        adDetailsIntent.putExtra(DBConstants.flatSpace, adInfo.getFlatSpace());
//
//        adDetailsIntent.putExtra(DBConstants.fullAddress, adInfo.getFullAddress());
//
//        if (!(adInfo.getImages() == null || adInfo.getImages().isEmpty())) {
//            String[] images = new String[adInfo.getImages().size()];
//            for (int i = 0; i < adInfo.getImages().size(); i++) {
//                images[i] = adInfo.getImages().get(i).getDownloadUrl();
//            }
//            adDetailsIntent.putExtra(DBConstants.images, images);
//        }
//
//        if (adInfo.getMap() != null) {
//            adDetailsIntent.putExtra(DBConstants.map, adInfo.getMap().getDownloadUrl());
//        }
        adDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        adDetailsIntent.putExtra(AppConstants.keyAdInfo, adInfo);
        startActivity(adDetailsIntent);
    }

    protected void shareAction() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey check out the latest To-Let app:\nhttps://play.google.com/store/apps/details?id=" +
                getPackageName());
        sendIntent.setType("text/plain");
        startActivityForResult(sendIntent, AppConstants.shareApp);
    }

    public void showSimpleDialog(String message) {
        showSimpleDialog("Alert", message);
    }

    public void showSimpleDialog(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(R.mipmap.ic_launcher_round);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void showSimpleDialog(int messageResourceId) {
        showSimpleDialog(getString(messageResourceId));
    }

    public void showSimpleDialog(int titleResourceId, int messageResourceId) {
        showSimpleDialog(getString(titleResourceId), getString(messageResourceId));
    }

    private Dialog searchDialog;
    public static String[] childArray;
    public static long fromDateTime = 0;
    public static long toDateTime = 0;
    public static long rentMinLong = 0;
    public static long rentMaxLong = 0;

    public void showFilterWindow() {
        if (searchDialog == null) {
            searchDialog = new Dialog(this);
            searchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            searchDialog.setContentView(R.layout.dialog_search_date_price_range);
            Window window = searchDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            }
        }
        searchDialog.show();

        TextView title = searchDialog.findViewById(R.id.title);
        title.setText(getString(R.string.filter_what_you_like_to_want));

        final TextView fromMonth, toMonth;
        fromMonth = searchDialog.findViewById(R.id.fromMonth);
        if (fromDateTime > 0) {
            String formattedDate = DateUtils.getFormattedDateString(DateUtils.getDateIncreaseMonth(fromDateTime), DateUtils.format2);
            fromMonth.setText(formattedDate);
        }
        toMonth = searchDialog.findViewById(R.id.toMonth);
        if (toDateTime > 0) {
            String formattedDate = DateUtils.getFormattedDateString(DateUtils.getDateIncreaseMonth(toDateTime), DateUtils.format2);
            toMonth.setText(formattedDate);
        }

        final int[] fromDateAsArray = DateUtils.getTodayDateAsArray();
        final int[] toDateAsArray = DateUtils.getTodayDateAsArray();

        fromMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromMonth.getText().toString().equalsIgnoreCase(getString(R.string.any))) {
                    showDatePickerDialog(fromMonth, fromDateAsArray[0], fromDateAsArray[1], fromDateAsArray[2]);
                } else {
                    int[] dateAsArray = DateUtils.getDateAsArray(DateUtils.getDate(fromMonth.getText().toString(), DateUtils.format2));
                    showDatePickerDialog(fromMonth, dateAsArray[0], dateAsArray[1], dateAsArray[2]);
                }
            }
        });

        fromMonth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                fromMonth.setText(R.string.any);
                return true;
            }
        });

        toMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toMonth.getText().toString().equalsIgnoreCase(getString(R.string.any))) {
                    showDatePickerDialog(toMonth, toDateAsArray[0], toDateAsArray[1], toDateAsArray[2]);
                } else {
                    int[] dateAsArray = DateUtils.getDateAsArray(DateUtils.getDate(toMonth.getText().toString(), DateUtils.format2));
                    showDatePickerDialog(toMonth, dateAsArray[0], dateAsArray[1], dateAsArray[2]);
                }
            }
        });

        toMonth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toMonth.setText(R.string.any);
                return true;
            }
        });

        final EditText rentMin, rentMax;
        rentMin = searchDialog.findViewById(R.id.rentMin);
        if (rentMinLong > 0) {
            rentMin.setText(String.valueOf(rentMinLong));
            rentMin.setSelection(rentMin.getText().length());
        }
        rentMin.requestFocus();

        rentMax = searchDialog.findViewById(R.id.rentMax);
        if (rentMaxLong > 0) {
            rentMax.setText(String.valueOf(rentMaxLong));
            rentMax.setSelection(rentMax.getText().length());
        }

        searchDialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromDateTime = DateUtils.getDate(fromMonth.getText().toString(), DateUtils.format2).getTime();
                if (fromDateTime > 0) {
                    fromDateTime = DateUtils.getDateForQuery(fromDateTime);
                }
                toDateTime = DateUtils.getDate(toMonth.getText().toString(), DateUtils.format2).getTime();
                if (toDateTime > 0) {
                    toDateTime = DateUtils.getDateForQuery(toDateTime);
                }

                rentMinLong = 0;
                if (!rentMin.getText().toString().trim().isEmpty()) {
                    rentMinLong = Long.parseLong(rentMin.getText().toString());
                }

                rentMaxLong = 0;
                if (!rentMax.getText().toString().trim().isEmpty()) {
                    rentMaxLong = Long.parseLong(rentMax.getText().toString());
                }

                Bundle bundle = new Bundle();
                bundle.putString(MyAnalyticsUtil.keySearchType, MyAnalyticsUtil.searchTypeNormal);
                bundle.putDouble(MyAnalyticsUtil.keyFromDateTime, fromDateTime);
                bundle.putDouble(MyAnalyticsUtil.keyToDateTime, toDateTime);
                bundle.putDouble(MyAnalyticsUtil.keyRentMinLong, rentMinLong);
                bundle.putDouble(MyAnalyticsUtil.keyRentMaxLong, rentMaxLong);
                MyAnalyticsUtil myAnalyticsUtil = new MyAnalyticsUtil(BaseActivity.this);
                myAnalyticsUtil.searchEvent(bundle);

                if (fromDateTime == 0 && toDateTime == 0 && rentMinLong == 0 && rentMaxLong == 0) {
                    showSimpleDialog(R.string.please_insert_valid_data);
                    return;
                }

                if (fromDateTime > 0 && toDateTime > 0 && fromDateTime > toDateTime &&
                        rentMinLong > 0 && rentMaxLong > 0 && rentMinLong > rentMaxLong) {
                    showSimpleDialog(R.string.please_insert_valid_date_range_and_rent_range);
                    return;
                } else {
                    if (fromDateTime > 0 && toDateTime > 0 && fromDateTime > toDateTime) {
                        showSimpleDialog(R.string.please_insert_valid_date_range);
                        return;
                    } else if (rentMinLong > 0 && rentMaxLong > 0 && rentMinLong > rentMaxLong) {
                        showSimpleDialog(R.string.please_insert_valid_rent_range);
                        return;
                    } else {
                        startSubAdListActivity(AppConstants.subQueryQuery);
                        searchDialog.dismiss();
                    }
                }
                showLog();
            }
        });

        searchDialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyAnalyticsUtil myAnalyticsUtil = new MyAnalyticsUtil(BaseActivity.this);
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.searchTypeNormal, "canceled");
                searchDialog.dismiss();
            }
        });
    }

//    private void updateSearchedData(long fromDateTime, long toDateTime, long rentMinLong, long rentMaxLong) {
//        int selectedTabPosition = tabLayout.getSelectedTabPosition();
//        String flatType;
//        if (selectedTabPosition == 1) {
//            flatType = DBConstants.keyMess;
//        } else if (selectedTabPosition == 2) {
//            flatType = DBConstants.keySublet;
//        } else if (selectedTabPosition == 3) {
//            flatType = DBConstants.keyOthers;
//        } else {
//            flatType = DBConstants.keyFamily;
//        }
//
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        Query query = databaseReference.child(DBConstants.adList)
//                .child(flatType)
//                .orderByChild(DBConstants.flatRent);
//
//        query.startAt(rentMinLong)
//                .endAt(rentMaxLong)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//    }

//    private void loadData(Query query, final long fromDateTime, final long toDateTime) {
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                ArrayList<AdInfo> adList = new ArrayList<>();
//                adList.clear();
//                for (DataSnapshot ad : dataSnapshot.getChildren()) {
//                    AdInfo adInfo = ad.getValue(AdInfo.class);
//
//                    if (adInfo == null)
//                        continue;
//
//                    if (fromDateTime > 0 || toDateTime > 0) {
//                        if (fromDateTime > 0 && toDateTime > 0) {
//                            if (adInfo.startingFinalDate >= fromDateTime && adInfo.startingFinalDate <= toDateTime)
//                                adList.add(adInfo);
//                        } else {
//                            if (toDateTime > 0) {
//                                if (adInfo.startingFinalDate <= toDateTime)
//                                    adList.add(adInfo);
//                            } else {// fromDateTime > 0
//                                if (adInfo.startingFinalDate >= fromDateTime)
//                                    adList.add(adInfo);
//                            }
//                        }
//                    } else {
//                        adList.add(adInfo);
//                    }
//                }
//
//                showLog("adList size: " + adList.size());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void showDatePickerDialog(final TextView view, int year, int month, int dayOfMonth) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                String dateAsString = year + "-" + AppConstants.twoDigitIntFormatter(monthOfYear + 1)
                        + "-" + AppConstants.twoDigitIntFormatter(dayOfMonth);
                String formattedDate = DateUtils.getFormattedDateString(DateUtils.getDate(dateAsString, DateUtils.format4), DateUtils.format2);
                view.setText(formattedDate);
            }
        }, year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

//    protected void reloadList(int i) {
//
//    }

    public void startSubAdListActivity(int subAdListType) {
//        if (this instanceof SubAdListActivity) {
//            reloadList(1);
//            return;
//        }
        Intent subAdListIntent = new Intent(this, SubAdListActivity.class);
        subAdListIntent.putExtra(AppConstants.keySubAdListType, subAdListType);
        subAdListIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(subAdListIntent);
    }

//    public void startSubAdListActivity(String[] childArray, long fromDateTime, long toDateTime, long rentMinLong, long rentMaxLong) {
//        Intent subAdListIntent = new Intent(this, SubAdListActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putInt(AppConstants.keySubAdListType, AppConstants.subQueryQuery);
//
//        bundle.putStringArray(AppConstants.keyChildArray, childArray);
//        bundle.putLong(AppConstants.keyFromDateTime, fromDateTime);
//        bundle.putLong(AppConstants.keyToDateTime, toDateTime);
//        bundle.putLong(AppConstants.keyRentMinLong, rentMinLong);
//        bundle.putLong(AppConstants.keyRentMaxLong, rentMaxLong);
//
//        subAdListIntent.putExtras(bundle);
//        subAdListIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(subAdListIntent);
//    }
}
