package com.to.let.bd.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.to.let.bd.R;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.fragments.SubAdList;
import com.to.let.bd.utils.ActivityUtils;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;

public class SubAdListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_ad_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        iniView();
    }

    private void iniView() {
        findViewById(R.id.postYourAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewAdActivity();
            }
        });
        showSubAdListFragment();
    }

    public static boolean needToRefreshData = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (needToRefreshData) {
            reloadData();
        }

        needToRefreshData = false;
    }

//    @Override
//    protected void reloadList(int i) {
//        super.reloadList(i);
//        reloadData();
//    }

    private void reloadData() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SubAdList subAdListFragment = (SubAdList) fragmentManager.findFragmentByTag(SubAdList.TAG);
        subAdListFragment.reload();
    }

    private void showSubAdListFragment() {
        int subAdListType = getIntent().getIntExtra(AppConstants.keySubAdListType, 0);
        String title = getString(R.string.my_favorite_list);
        if (subAdListType == AppConstants.subQueryMy) {
            title = getString(R.string.my_ad_list);
        } else if (subAdListType == AppConstants.subQueryNearest) {
            title = getString(R.string.my_nearest_list);
        } else if (subAdListType == AppConstants.subQuerySmart) {
            title = getString(R.string.smart_ads);
        } else if (subAdListType == AppConstants.subQueryAll) {
            title = getString(R.string.all_ad_list);
        } else if (subAdListType == AppConstants.subQueryQuery) {
            title = getString(R.string.filtered_list);
        }

        updateTitle(title);
        SubAdList fragment = SubAdList.newInstance();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            bundle = new Bundle();
        bundle.putInt(AppConstants.keySubAdListType, subAdListType);
        fragment.setArguments(bundle);

        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                fragment, R.id.fragmentContainer, SubAdList.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sublist_activity, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.filterAction:
                Bundle bundle = getIntent().getExtras();
                if (bundle != null && bundle.getInt(AppConstants.keySubAdListType, 0) > 0) {
//                    long fromDateTime = bundle.getLong(AppConstants.keyFromDateTime, 0);
//                    long toDateTime = bundle.getLong(AppConstants.keyToDateTime, 0);
//                    long rentMinLong = bundle.getLong(AppConstants.keyRentMinLong, 0);
//                    long rentMaxLong = bundle.getLong(AppConstants.keyRentMaxLong, 0);

                    int subAdListType = bundle.getInt(AppConstants.keySubAdListType, 0);
                    if (subAdListType == AppConstants.subQueryFav) {
                        BaseActivity.childArray = new String[]{DBConstants.userFavAdList, getUid()};
                    } else if (subAdListType == AppConstants.subQueryMy) {
                        BaseActivity.childArray = new String[]{DBConstants.userAdList, getUid()};
                    }
                    showFilterWindow();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
