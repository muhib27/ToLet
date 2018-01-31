package com.to.let.bd.activities;

import android.os.Bundle;
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

    private void showSubAdListFragment() {
        int subListType = getIntent().getIntExtra(AppConstants.keySubAdListType, 0);
        String title = getString(R.string.my_favorite_list);
        if (subListType == AppConstants.subQueryMy) {
            title = getString(R.string.my_ad_list);
        } else if (subListType == AppConstants.subQueryNearest) {
            title = getString(R.string.my_nearest_list);
        } else if (subListType == AppConstants.subQuerySmart) {
            title = getString(R.string.my_nearest_list);
        } else if (subListType == AppConstants.subQueryAll) {
            title = getString(R.string.all_ad_list);
        }

        updateTitle(title);
        SubAdList fragment = SubAdList.newInstance(subListType);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                fragment, R.id.fragmentContainer, SubAdList.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
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
            case R.id.shareAction:
                shareAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
