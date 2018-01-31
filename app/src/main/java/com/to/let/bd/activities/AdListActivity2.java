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
import android.content.DialogInterface;
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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseFirebaseAuthActivity;
import com.to.let.bd.fragments.AdListBaseFragment;
import com.to.let.bd.fragments.FamilyFlatList;
import com.to.let.bd.fragments.MessFlatList;
import com.to.let.bd.fragments.OthersFlatList;
import com.to.let.bd.fragments.SubletFlatList;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.DateUtils;

import java.util.ArrayList;

public class AdListActivity2 extends BaseFirebaseAuthActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = AdListActivity2.class.getSimpleName();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_ad_list2;
    }

    @Override
    protected void onCreate() {
        initTitle();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateTitle(getActivityTitle());
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
        navigationView.setCheckedItem(R.id.navAllAds);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
        navigationView.setCheckedItem(R.id.navAllAds);
    }

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

        int pagerSelection = mViewPager.getCurrentItem();

        for (int i = pagerSelection - 1; i < pagerSelection + 1; i++) {
            if (i == 0)
                continue;
            if (i >= mViewPager.getChildCount())
                continue;

            Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + i);

            if (fragment != null && fragment instanceof AdListBaseFragment && fragment.isVisible()) {
                ((AdListBaseFragment) fragment).reload();
            }
        }
    }

    private ViewPager mViewPager;

    private void init() {
        findViewById(R.id.postYourAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewAdActivity();
            }
        });

        CategoryPagerAdapter categoryPagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(categoryPagerAdapter);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(1);
    }

    private TabLayout tabLayout;

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

        if (id == R.id.navAllAds) {
            if (mViewPager.getCurrentItem() != 0) {
                mViewPager.setCurrentItem(0, true);
            }
        } else if (id == R.id.navLogout) {
            if (firebaseUser.isAnonymous()) {
                showLoginAlert(-1, getString(R.string.exit_alert));
            } else {
                showLoginAlert(0, getString(R.string.logout_alert));
            }
            return true;
        } else if (id == R.id.navNearestAds) {
            startNearestActivity();
        } else if (id == R.id.navSmartAds) {
//            startSubAdListActivity(AppConstants.subQuerySmart);
            showSimpleDialog(R.string.working_progress);
            return true;
        } else if (id == R.id.navMyAds) {
            if (firebaseUser.isAnonymous()) {
                showLoginAlert(1, getString(R.string.login_alert));
            } else {
                startSubAdListActivity(AppConstants.subQueryMy);
            }
        } else if (id == R.id.navFavAds) {
            if (firebaseUser.isAnonymous()) {
                showLoginAlert(1, getString(R.string.login_alert));
            } else {
                startSubAdListActivity(AppConstants.subQueryFav);
            }
        } else if (id == R.id.navShare) {
            shareAction();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLoginAlert(final int type, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(R.mipmap.ic_launcher_round);
        alertDialog.setTitle(R.string.alert);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (type == 1)
                            navPostYourAdd.performClick();
                        else if (type == 0)
                            logoutAndAnonymousLogin();
                        else
                            onBackPressed();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.show();
    }

    private void startNearestActivity() {
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mapIntent);
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
        getMenuInflater().inflate(R.menu.ad_list_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filterAction:
                showFilterWindow();
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

    private void showFilterWindow() {
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
        title.setText(getString(R.string.filter_the_selected_list));

        final TextView fromMonth, toMonth;
        fromMonth = searchDialog.findViewById(R.id.fromMonth);
        toMonth = searchDialog.findViewById(R.id.toMonth);

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
        rentMax = searchDialog.findViewById(R.id.rentMax);

        searchDialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long fromDateTime = DateUtils.getDate(fromMonth.getText().toString(), DateUtils.format2).getTime();
                if (fromDateTime > 0) {
                    fromDateTime = DateUtils.getDateForQuery(fromDateTime);
                }
                long toDateTime = DateUtils.getDate(toMonth.getText().toString(), DateUtils.format2).getTime();
                if (toDateTime > 0) {
                    toDateTime = DateUtils.getDateForQuery(toDateTime);
                }

                long rentMinLong = 0;
                if (!rentMin.getText().toString().trim().isEmpty()) {
                    rentMinLong = Long.parseLong(rentMin.getText().toString());
                }

                long rentMaxLong = 0;
                if (!rentMax.getText().toString().trim().isEmpty()) {
                    rentMaxLong = Long.parseLong(rentMax.getText().toString());
                }

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
                        int selectedTabPosition = tabLayout.getSelectedTabPosition();
                        String flatType;
                        if (selectedTabPosition == 1) {
                            flatType = DBConstants.keyMess;
                        } else if (selectedTabPosition == 2) {
                            flatType = DBConstants.keySublet;
                        } else if (selectedTabPosition == 3) {
                            flatType = DBConstants.keyOthers;
                        } else {
                            flatType = DBConstants.keyFamily;
                        }

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        Query query = databaseReference.child(DBConstants.adList)
                                .child(flatType);
                        if ((fromDateTime == 0 && toDateTime == 0) || (rentMinLong == 0 && rentMaxLong == 0)) {
                            if (fromDateTime == 0 && toDateTime == 0) {
                                query = query.orderByChild(DBConstants.flatRent);
                                if (rentMinLong > 0 && rentMaxLong > 0) {
                                    query = query.startAt(rentMinLong).endAt(rentMaxLong);
                                } else {
                                    if (rentMinLong == 0) {
                                        query = query.endAt(rentMaxLong);
                                    } else {
                                        query = query.startAt(rentMinLong);
                                    }
                                }
                            } else {
                                query = query.orderByChild(DBConstants.startingFinalDate);
                                if (fromDateTime > 0 && toDateTime > 0) {
                                    query = query.startAt(fromDateTime).endAt(toDateTime);
                                } else {
                                    if (fromDateTime == 0) {
                                        query = query.endAt(toDateTime);
                                    } else {
                                        query = query.startAt(fromDateTime);
                                    }
                                }
                            }
                            loadData(query, 0, 0);
                            searchDialog.dismiss();
                            return;
                        } else {
                            query = query.orderByChild(DBConstants.flatRent);
                            if (rentMinLong > 0 && rentMaxLong > 0) {
                                query = query.startAt(rentMinLong).endAt(rentMaxLong);
                            } else {
                                if (rentMinLong == 0) {
                                    query = query.endAt(rentMaxLong);
                                } else {
                                    query = query.startAt(rentMinLong);
                                }
                            }
                            loadData(query, fromDateTime, toDateTime);
                            searchDialog.dismiss();
                            return;
                        }
                    }
                }
//                updateSearchedData(fromDateLong, toDateLong, rentMinLong, rentMaxLong);
//                searchDialog.dismiss();
//                showLog();
            }
        });

        searchDialog.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.dismiss();
            }
        });
    }

    private void updateSearchedData(long fromDateTime, long toDateTime, long rentMinLong, long rentMaxLong) {
        int selectedTabPosition = tabLayout.getSelectedTabPosition();
        String flatType;
        if (selectedTabPosition == 1) {
            flatType = DBConstants.keyMess;
        } else if (selectedTabPosition == 2) {
            flatType = DBConstants.keySublet;
        } else if (selectedTabPosition == 3) {
            flatType = DBConstants.keyOthers;
        } else {
            flatType = DBConstants.keyFamily;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(DBConstants.adList)
                .child(flatType)
                .orderByChild(DBConstants.flatRent);

        query.startAt(rentMinLong)
                .endAt(rentMaxLong)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadData(Query query, final long fromDateTime, final long toDateTime) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<AdInfo> adList = new ArrayList<>();
                adList.clear();
                for (DataSnapshot ad : dataSnapshot.getChildren()) {
                    AdInfo adInfo = ad.getValue(AdInfo.class);

                    if (adInfo == null)
                        continue;

                    if (fromDateTime > 0 || toDateTime > 0) {
                        if (fromDateTime > 0 && toDateTime > 0) {
                            if (adInfo.startingFinalDate >= fromDateTime && adInfo.startingFinalDate <= toDateTime)
                                adList.add(adInfo);
                        } else {
                            if (toDateTime > 0) {
                                if (adInfo.startingFinalDate <= toDateTime)
                                    adList.add(adInfo);
                            } else {// fromDateTime > 0
                                if (adInfo.startingFinalDate >= fromDateTime)
                                    adList.add(adInfo);
                            }
                        }
                    } else {
                        adList.add(adInfo);
                    }
                }

                showLog("adList size: " + adList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
}
