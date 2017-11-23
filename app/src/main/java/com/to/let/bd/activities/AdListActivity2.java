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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.to.let.bd.R;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.fragments.FamilyFlatList;
import com.to.let.bd.fragments.MessFlatList;
import com.to.let.bd.fragments.OthersFlatList;
import com.to.let.bd.fragments.SubletFlatList;
import com.to.let.bd.model.SubletInfo;

public class AdListActivity2 extends BaseActivity {

    private static final String TAG = AdListActivity2.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_list2);

//        // Create the adapter that will return a fragment for each section
//        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
//            private final Fragment[] mFragments = new Fragment[]{
//                    new FamilyFlatList(),
//                    new MessFlatList(),
//                    new SubletFlatList(),
//                    new OthersFlatList()
//            };
//            private final String[] mFragmentNames = new String[]{
//                    getString(R.string.family),
//                    getString(R.string.mess_member),
//                    getString(R.string.sublet),
//                    getString(R.string.others)
//            };
//
//            @Override
//            public Fragment getItem(int position) {
//                return mFragments[position];
//            }
//
//            @Override
//            public int getCount() {
//                return mFragments.length;
//            }
//
//            @Override
//            public CharSequence getPageTitle(int position) {
//                return mFragmentNames[position];
//            }
//        };
        // Set up the ViewPager with the sections adapter.

        initTitle();
        init();
    }

    private ViewPager mViewPager;
    private CategoryPagerAdapter categoryPagerAdapter;

    private void init() {
        categoryPagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(categoryPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(0);
    }

    private String[] titles = new String[4];

    private void initTitle() {
        titles[0] = getString(R.string.family);
        titles[1] = getString(R.string.mess_member);
        titles[2] = getString(R.string.sublet);
        titles[3] = getString(R.string.others);
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
