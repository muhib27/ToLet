package com.to.let.bd.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.to.let.bd.R;
import com.to.let.bd.adapters.AdAdapter;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.DBConstants;

import java.util.ArrayList;

public class AdListActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        init();
    }

    private DatabaseReference databaseReference;

    private void initFirebase() {
        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void loadData() {
        initFirebase();
        Query recentAd = databaseReference.child(DBConstants.adList).limitToFirst(100);
        showProgressDialog();
        recentAd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    AdInfo adInfo = postSnapshot.getValue(AdInfo.class);
                    adList.add(adInfo);
                }
                closeProgressDialog();
                if (adAdapter == null) {
                    adAdapter = new AdAdapter(AdListActivity.this, adList);
                    adRecyclerView.setAdapter(adAdapter);
                } else {
                    adAdapter.setData(adList);
                    adAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                closeProgressDialog();
            }
        });
    }

    private ArrayList<AdInfo> adList = new ArrayList<>();
    private RecyclerView adRecyclerView;
    private AdAdapter adAdapter;

    private void init() {
        adRecyclerView = (RecyclerView) findViewById(R.id.adRecyclerView);
        adRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        adRecyclerView.setLayoutManager(layoutManager);

//        adRecyclerView.addItemDecoration(new SpaceItemDecoration(PickUtils.getInstance(this).dp2px(PickConfig.ITEM_SPACE), pickData.getSpanCount()));
//        adRecyclerView.addOnScrollListener(scrollListener);
//        PickPhotoHelper helper = new PickPhotoHelper(this, new PickPhotoListener() {
//            @Override
//            public void pickSuccess() {
//                GroupImage groupImage = PickPreferences.getInstance(MediaActivity.this).getListImage();
//                allPhotos = groupImage.mGroupMap.get(PickConfig.ALL_PHOTOS);
//                if (allPhotos == null) {
//                    showLog("PickPhotoView:" + "Image is Empty");
//                } else {
//                    showLog("All photos size: " + String.valueOf(allPhotos.size()));
//                }
//                if (allPhotos != null && !allPhotos.isEmpty()) {
//                    pickGridAdapter = new PickGridAdapter(MediaActivity.this, allPhotos, pickData);
//                    adRecyclerView.setAdapter(pickGridAdapter);
//                }
//            }
//        });
//        helper.getImages(pickData.isShowGif());
        if (adList.isEmpty()) {
            loadData();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation, menu);
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
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
