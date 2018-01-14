/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.to.let.bd.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseFirebaseAuthActivity;
import com.to.let.bd.fragments.FamilyFlatList;
import com.to.let.bd.fragments.MessFlatList;
import com.to.let.bd.fragments.OthersFlatList;
import com.to.let.bd.fragments.SubletFlatList;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.DateUtils;

import java.util.Calendar;

public class AdListActivity2 extends BaseFirebaseAuthActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = AdListActivity2.class.getSimpleName();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_ad_list2;
    }

    private Calendar newDate;

    @Override
    protected void onCreate() {
        initTitle();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getActivityTitle());
        }

        init();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        initNavigationDrawer();
        initInterstitialAd();
    }

    private NavigationView navigationView;
    private LinearLayout profileInfoLay;
    private ImageView userPic;
    private TextView userName, contactInfo;
    private Button navPostYourAdd;

    private void initNavigationDrawer() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationViewHeaderView = navigationView.getHeaderView(0);

        profileInfoLay = navigationViewHeaderView.findViewById(R.id.profileInfoLay);
        userPic = navigationViewHeaderView.findViewById(R.id.userPic);
        userName = navigationViewHeaderView.findViewById(R.id.userName);
        contactInfo = navigationViewHeaderView.findViewById(R.id.contactInfo);
        navPostYourAdd = navigationViewHeaderView.findViewById(R.id.postYourAdd);

        navPostYourAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignOut();
//                facebookAccountKit();
//                phoneNumberVerification("1674547477");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
    }

//    private void facebookAccountKit() {
//        AccessToken accessToken = AccountKit.getCurrentAccessToken();
//
//        if (accessToken != null) {
//            //Handle Returning User
//        } else {
//            //Handle new or logged out userCopy Code
//        }
//    }
//
//    public void phoneNumberVerification(String selectedPhoneNumber) {
//        final Intent intent = new Intent(this, AccountKitActivity.class);
//        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
//                new AccountKitConfiguration.AccountKitConfigurationBuilder(
//                        LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
//        PhoneNumber phoneNumber = new PhoneNumber("+880", selectedPhoneNumber, "BD");
//        configurationBuilder.setInitialPhoneNumber(phoneNumber);
//        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
//                configurationBuilder.build());
//        startActivityForResult(intent, SMS_REQUEST_CODE);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == SMS_REQUEST_CODE) { // confirm that this response matches your request
//            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
//            String toastMessage;
//            getCurrentAccount();
//            if (loginResult.getError() != null) {
//                toastMessage = loginResult.getError().getErrorType().getMessage();
//            } else if (loginResult.wasCancelled()) {
//                toastMessage = "Cancelled";
//            } else {
//                if (loginResult.getAccessToken() != null) {
//                    toastMessage = "Success:" + loginResult.getAccessToken().getAccountId();
//                } else {
//                    toastMessage = String.format("Success:%s...", loginResult.getAuthorizationCode().substring(0, 10));
//                }
//                // Success! Start your next activity...
////                loginResult.getAuthorizationCode()
//
//                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
//                    @Override
//                    public void onSuccess(final Account account) {
//                        String accountKitId = account.getId();
//                        PhoneNumber phoneNumber = account.getPhoneNumber();
//                        String phoneNumberString = phoneNumber.toString();
//                    }
//
//                    @Override
//                    public void onError(final AccountKitError error) {
//                        // Handle Error
//                    }
//                });
//                return;
//            }
//            showToast(toastMessage);
//        }
//    }
//
//    private void getCurrentAccount() {
//        AccessToken accessToken = AccountKit.getCurrentAccessToken();
//        if (accessToken != null) {
//            //Handle Returning User
//            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
//                @Override
//                public void onSuccess(final Account account) {
//                    // Get Account Kit ID
//                    String accountKitId = account.getId();
//                    showLog("Account Kit Id " + accountKitId);
//
//                    if (account.getPhoneNumber() != null) {
//                        showLog("CountryCode" + "" + account.getPhoneNumber().getCountryCode());
//                        showLog("PhoneNumber" + "" + account.getPhoneNumber().getPhoneNumber());
//
//                        // Get phone number
//                        PhoneNumber phoneNumber = account.getPhoneNumber();
//                        String phoneNumberString = phoneNumber.toString();
//                        showLog("NumberString" + phoneNumberString);
//                    }
//
//                    if (account.getEmail() != null)
//                        showLog("Email" + account.getEmail());
//                }
//
//                @Override
//                public void onError(final AccountKitError error) {
//                    // Handle Error
//                    showLog(TAG + error.toString());
//                }
//            });
//        } else {
//            //Handle new or logged out user
//            showLog(TAG + "Logged Out");
//        }
//    }

    private final int SMS_REQUEST_CODE = 99;

    private void updateNavHeader() {
        Menu menuNav = navigationView.getMenu();

        MenuItem logoutItem = menuNav.findItem(R.id.navLogout);
//        logoutItem.setIcon(R.drawable.ic_action_log_out);
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                profileInfoLay.setVisibility(View.GONE);
                navPostYourAdd.setVisibility(View.VISIBLE);

                logoutItem.setTitle(R.string.exit);
            } else {
                profileInfoLay.setVisibility(View.VISIBLE);
                navPostYourAdd.setVisibility(View.GONE);

                String displayName = firebaseUser.getDisplayName();
                if (displayName != null) {
                    userName.setText(displayName);
                }

                String email = firebaseUser.getEmail();
                if (email != null) {
                    contactInfo.setText(email);
                }

                if (firebaseUser.getPhotoUrl() != null)
                    Glide.with(this)
                            .load(firebaseUser.getPhotoUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .into(userPic);

                logoutItem.setTitle(R.string.logout);
            }
        } else {
            showProgressDialog();
            signInAnonymously();
        }
    }

    private String getActivityTitle() {
        return getString(R.string.ad_list);
    }

    @Override
    protected void setEmailAddress() {
        updateNavHeader();
    }

    private void init() {
        Button postYourAdd = findViewById(R.id.postYourAdd);
        postYourAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewAdActivity();
            }
        });

        CategoryPagerAdapter categoryPagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(categoryPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(0);
    }

    private void startNewAdActivity() {
        Intent newAdIntent = new Intent(this, NewAdActivity2.class);
        newAdIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(newAdIntent);
    }

    private String[] titles = new String[4];

    private void initTitle() {
        titles[0] = getString(R.string.family);
        titles[1] = getString(R.string.mess_member);
        titles[2] = getString(R.string.sublet);
        titles[3] = getString(R.string.others);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navLogout) {
            logoutAndAnonymousLogin();
        } else if (id == R.id.navMyAds) {
            startSubAdListActivity(AppConstants.subQueryFav);
        } else if (id == R.id.navAllAds) {
            startSubAdListActivity(AppConstants.subQueryAll);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startSubAdListActivity(int subAdListType) {
        Intent subAdListIntent = new Intent(this, SubAdListActivity.class);
        subAdListIntent.putExtra(AppConstants.keySubAdListType, subAdListType);
        subAdListIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(subAdListIntent);
    }

    @Override
    protected void afterSuccessfulAnonymousLogin() {
        closeProgressDialog();
        updateNavHeader();
    }

    private class CategoryPagerAdapter extends FragmentPagerAdapter {

        CategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                return MessFlatList.newInstance(position);
            } else if (position == 2) {
                return SubletFlatList.newInstance(position);
            } else if (position == 3) {
                return OthersFlatList.newInstance(position);
            } else {
                return FamilyFlatList.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shareAction:
                shareAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    private InterstitialAd interstitialAd;

    private void initInterstitialAd() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.ad_mob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                showLog("Code to be executed when an ad finishes loading.");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                showLog("Code to be executed when an ad request fails.");
            }

            @Override
            public void onAdOpened() {
                showLog("Code to be executed when the ad is displayed.");
            }

            @Override
            public void onAdLeftApplication() {
                showLog("Code to be executed when the user has left the app.");
            }

            @Override
            public void onAdClosed() {
                showLog("Code to be executed when when the interstitial ad is closed.");
            }
        });
    }

    private Dialog searchDialog;

    private void showSearchWindow() {
        searchDialog = new Dialog(this);
        searchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        searchDialog.setContentView(R.layout.dialog_search_date_price_range);
        Window window = searchDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        }

        searchDialog.show();

        TextView title = searchDialog.findViewById(R.id.title);
        title.setText(getString(R.string.search));

        final Button fromMonth, toMonth;
        fromMonth = searchDialog.findViewById(R.id.fromMonth);
        toMonth = searchDialog.findViewById(R.id.toMonth);

        final int[] dateAsArray = DateUtils.getTodayDateAsArray();
        fromMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromMonth.getText().toString().equalsIgnoreCase(getString(R.string.any))) {
                    showDatePickerDialog(fromMonth, dateAsArray[0], dateAsArray[1], dateAsArray[2]);
                } else {
                    int[] dateAsArray = DateUtils.splittedDate(fromMonth.getText().toString());
                    showDatePickerDialog(fromMonth, dateAsArray[0], dateAsArray[1], dateAsArray[2]);
                }
            }
        });
        toMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toMonth.getText().toString().equalsIgnoreCase(getString(R.string.any))) {
                    showDatePickerDialog(toMonth, dateAsArray[0], dateAsArray[1], dateAsArray[2]);
                } else {
                    int[] dateAsArray = DateUtils.splittedDate(toMonth.getText().toString());
                    showDatePickerDialog(toMonth, dateAsArray[0], dateAsArray[1], dateAsArray[2]);
                }
            }
        });

        final EditText rentMin, rentMax;
        rentMin = searchDialog.findViewById(R.id.rentMin);
        rentMax = searchDialog.findViewById(R.id.rentMax);

        searchDialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long fromDateLong = 0;

                long toDateLong = 0;

                long rentMinLong = 0;
                if (!rentMin.getText().toString().trim().isEmpty()) {
                    rentMinLong = Long.parseLong(rentMin.getText().toString());
                }

                long rentMaxLong = 0;
                if (!rentMax.getText().toString().trim().isEmpty()) {
                    rentMaxLong = Long.parseLong(rentMax.getText().toString());
                }

                updateSearchedData(fromDateLong, toDateLong, rentMinLong, rentMaxLong);
                searchDialog.dismiss();
            }
        });

        searchDialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.dismiss();
            }
        });
    }

    private void updateSearchedData(long fromDateLong, long toDateLong, long rentMinLong, long rentMaxLong) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(DBConstants.adList)
                .orderByChild(DBConstants.startingFinalDate)
                .startAt(rentMinLong)
                .endAt(rentMaxLong);
    }

    private void showDatePickerDialog(final Button view, int year, int month, int dayOfMonth) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                view.setText(AppConstants.twoDigitIntFormatter(year)
                        + AppConstants.twoDigitIntFormatter(monthOfYear + 1) +
                        AppConstants.twoDigitIntFormatter(dayOfMonth));
            }
        }, year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}
