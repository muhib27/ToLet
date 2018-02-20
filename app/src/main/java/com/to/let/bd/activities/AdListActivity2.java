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

import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.common.BaseFirebaseAuthActivity;
import com.to.let.bd.fragments.AdListBaseFragment;
import com.to.let.bd.fragments.FamilyFlatList;
import com.to.let.bd.fragments.MessFlatList;
import com.to.let.bd.fragments.OthersFlatList;
import com.to.let.bd.fragments.SubletFlatList;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.MyAnalyticsUtil;

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

    public static boolean needToRefreshData = false;

    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
        navigationView.setCheckedItem(R.id.navAllAds);

        if (needToRefreshData) {
            updateListItem();
        }

        needToRefreshData = false;
        SubAdListActivity.needToRefreshData = false;
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
        updateListItem();
    }

    private void updateListItem() {
        int pagerSelection = mViewPager.getCurrentItem();

        for (int i = pagerSelection - 1; i < pagerSelection + 1; i++) {
            if (i == -1)
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
            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySmartListEvent, "try");
            showSimpleDialog(R.string.smart_ad_title, R.string.coming_soon);
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
                        if (type == 1) {
                            navPostYourAdd.performClick();
                        } else if (type == 0) {
                            logoutAndAnonymousLogin();
                            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyLogoutEvent, "true");
                        } else {
                            onBackPressed();
                        }
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (type == 0)
                            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyLogoutEvent, "false");
                    }
                });
        alertDialog.show();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (type == 0)
                    myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyLogoutEvent, "false");
            }
        });
    }

    private void startNearestActivity() {
        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyNearestAdEvent, "true");
        Intent mapIntent = new Intent(this, MapActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mapIntent);
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
                BaseActivity.childArray = new String[]{DBConstants.adList, flatType};
                showFilterWindow();
                return true;
            case R.id.sortByRent:
                if (sortType != 0)
                    sortType = 0;
                else
                    sortType = 1;
                sort();
                return true;
            case R.id.sortByDate:
                if (sortType != 2)
                    sortType = 2;
                else
                    sortType = 3;
                sort();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private int sortType = 0;

    private void sort() {
        int pagerSelection = mViewPager.getCurrentItem();
        myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keySortEvent,
                "pager position: " + pagerSelection
                        + " sort type: " + sortType);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + pagerSelection);
        if (fragment != null && fragment instanceof AdListBaseFragment && fragment.isVisible()) {
            ((AdListBaseFragment) fragment).sort(sortType);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.shareApp) {
            if (resultCode == RESULT_OK)
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyShareEvent, "true");
            else
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyShareEvent, "false");
        }
    }
}
