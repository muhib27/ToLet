package com.to.let.bd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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
import com.to.let.bd.utils.SmartToLetConstants;

import java.util.ArrayList;
import java.util.HashMap;

public class AdListActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initGoogleLogin();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewAdActivity();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        initFirebase();
        initNavigationDrawer();
        updateNavHeader();
        init();
    }

    private void startNewAdActivity() {
        Intent newAdIntent = new Intent(this, NewAdActivity.class);
        startActivity(newAdIntent);
    }

    private NavigationView navigationView;
    private LinearLayout profileInfoLay;
    private TextView userName, contactInfo;
    private Button postYourAdd;

    private void initNavigationDrawer() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationViewHeaderView = navigationView.getHeaderView(0);

        profileInfoLay = (LinearLayout) navigationViewHeaderView.findViewById(R.id.profileInfoLay);
        userName = (TextView) navigationViewHeaderView.findViewById(R.id.userName);
        contactInfo = (TextView) navigationViewHeaderView.findViewById(R.id.contactInfo);
        postYourAdd = (Button) navigationViewHeaderView.findViewById(R.id.postYourAdd);

        postYourAdd.setOnClickListener(this);
    }

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    private void initGoogleLogin() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }

    // Google login
    private void googleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SmartToLetConstants.GOOGLE_SIGN_IN);
    }

    // Google sign out
    private void googleSignOut() {
        if (mGoogleApiClient.isConnected())
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            googleLogin();
                        }
                    });
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == SmartToLetConstants.GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.getStatus().isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                showToast(getString(R.string.google_login_failed));
            }
        }
    }
    // [END onActivityResult]

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseLoginWithGoogle(credential);
    }

    private void firebaseLoginWithGoogle(AuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showLog("signInWithCredential:onComplete:" + task.isSuccessful());
                        closeProgressDialog();

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        //showLog("task Result: " + task.getResult().toString());
                        if (!task.isSuccessful()) {
                            String message = task.getException().getMessage();
                            showToast(message);
                        } else {
                            writeNewUser();
                            updateNavHeader();
                        }
                    }
                });
    }

//    private void linkedCredential(final AuthCredential credential) {
//        showProgressDialog();
//        firebaseUser.linkWithCredential(credential).addOnCompleteListener(this,
//                new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            updateNavHeader();
//                            closeProgressDialog();
//                        } else {
//                            try {
//                                String message = task.getException().getMessage();
//                                if (message != null && (message.toLowerCase().equals(SmartToLetConstants.firebaseAccountConflictMessage1.toLowerCase()) ||
//                                        message.toLowerCase().contains(SmartToLetConstants.firebaseAccountConflictMessage2.toLowerCase()))) {
//                                    firebaseLoginWithGoogle(credential);
//                                } else {
//                                    showToast(message);
//                                    closeProgressDialog();
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                closeProgressDialog();
//                            }
//                        }
//                    }
//                });
//    }

    @Override
    public void onClick(View view) {
        if (view == postYourAdd) {
            googleSignOut();
        }
    }

    private void updateNavHeader() {
        Menu menuNav = navigationView.getMenu();

        MenuItem logoutItem = menuNav.findItem(R.id.navLogout);
//        logoutItem.setIcon(R.drawable.ic_action_log_out);
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                profileInfoLay.setVisibility(View.GONE);
                postYourAdd.setVisibility(View.VISIBLE);

                logoutItem.setTitle(R.string.exit);
            } else {
                profileInfoLay.setVisibility(View.VISIBLE);
                postYourAdd.setVisibility(View.GONE);

                String displayName = firebaseUser.getDisplayName();
                if (displayName != null) {
                    userName.setText(displayName);
                }

                String email = firebaseUser.getEmail();
                if (email != null) {
                    contactInfo.setText(email);
                }

                logoutItem.setTitle(R.string.logout);
            }
        }
    }

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference databaseReference;

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    showLog("onAuthStateChanged:signed_in:" + firebaseUser.getUid());
                } else {
                    showLog("onAuthStateChanged:signed_out");
                }
            }
        };
        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void loadData() {
        Query recentAd = databaseReference.child(DBConstants.adList).limitToFirst(100);
//        showProgressDialog();
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

        if (id == R.id.navLogout) {
            logoutAndAnonymousLogin();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutAndAnonymousLogin() {
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                finish();
            } else {
                mAuth.signOut();
                signInAnonymously();
            }
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                showLog("signInAnonymously:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()) {
                    showToast("Authentication failed: " + task.getException().getMessage());
                    finish();
                    return;
                }
                writeNewUser();
                updateNavHeader();
            }
        });
    }

    private DatabaseReference mDatabase;

    private void writeNewUser() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance().getReference();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put(DBConstants.userId, getUid());
        if (!firebaseUser.isAnonymous() && firebaseUser.getEmail() != null) {
            userValues.put(DBConstants.userEmail, firebaseUser.getEmail());
        }

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DBConstants.user + "/" + getUid(), userValues);
        mDatabase.updateChildren(childUpdates);
    }
}
