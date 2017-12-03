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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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
import com.to.let.bd.R;
import com.to.let.bd.common.BaseFirebaseAuthActivity;
import com.to.let.bd.fragments.FamilyFlatList;
import com.to.let.bd.fragments.MessFlatList;
import com.to.let.bd.fragments.OthersFlatList;
import com.to.let.bd.fragments.SubletFlatList;

public class AdListActivity2 extends BaseFirebaseAuthActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = AdListActivity2.class.getSimpleName();
    public static String firebaseUserId = null;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_ad_list2;
    }

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
        updateNavHeader();
        firebaseUserId = getUid();
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

        navPostYourAdd.setOnClickListener(this);
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
    public void onClick(View view) {

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navLogout) {
            logoutAndAnonymousLogin();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void afterSuccessfulAnonymousLogin() {
        closeProgressDialog();
        updateNavHeader();
        firebaseUserId = getUid();
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
}
