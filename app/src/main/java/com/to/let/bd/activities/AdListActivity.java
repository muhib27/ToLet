package com.to.let.bd.activities;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.HintRequest;
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
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.pick_photo.PickConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AdListActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private Button postYourAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initGoogleLogin();

        postYourAdd = findViewById(R.id.postYourAdd);
        postYourAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewAdActivity();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
//        requestHint();
        Intent newAdIntent = new Intent(this, NewAdActivity2.class);
        newAdIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(newAdIntent);
    }

    private NavigationView navigationView;
    private LinearLayout profileInfoLay;
    private ImageView userPic;
    private TextView userName, contactInfo;
    private Button navPostYourAdd;

    private void initNavigationDrawer() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationViewHeaderView = navigationView.getHeaderView(0);

        profileInfoLay = (LinearLayout) navigationViewHeaderView.findViewById(R.id.profileInfoLay);
        userPic = (ImageView) navigationViewHeaderView.findViewById(R.id.userPic);
        userName = (TextView) navigationViewHeaderView.findViewById(R.id.userName);
        contactInfo = (TextView) navigationViewHeaderView.findViewById(R.id.contactInfo);
        navPostYourAdd = (Button) navigationViewHeaderView.findViewById(R.id.postYourAdd);

        navPostYourAdd.setOnClickListener(this);
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
                .addApi(Auth.CREDENTIALS_API)
                .build();
        mGoogleApiClient.connect();
    }

    // Google login
    private void googleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, AppConstants.GOOGLE_SIGN_IN);
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
        if (requestCode == AppConstants.GOOGLE_SIGN_IN) {
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

    @Override
    public void onClick(View view) {
        if (view == navPostYourAdd) {
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

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                AdInfo adInfo = postSnapshot.getValue(AdInfo.class);
                adList.add(adInfo);
            }

            Collections.sort(adList, new Comparator<AdInfo>() {
                @Override
                public int compare(AdInfo o1, AdInfo o2) {
                    return o2.getAdId().compareTo(o1.getAdId());
                }
            });
            closeProgressDialog();
            displayAdList();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            closeProgressDialog();
        }
    };

    private void loadAllData() {
        Query recentAd = databaseReference.child(DBConstants.adList).orderByChild(DBConstants.createdTime).limitToLast(100);
        showProgressDialog();
        recentAd.addListenerForSingleValueEvent(valueEventListener);
    }

    private void loadData(String flatType, long startRange, long endRange) {
//        databaseReference.removeEventListener(valueEventListener);

        Query recentAd = null;

        if (flatType != null) {
            recentAd = databaseReference.child(DBConstants.adList).orderByChild(DBConstants.flatType).equalTo(flatType);
        }

        if (recentAd == null)
            return;

//        if (!(startRange == 0 && endRange == 0)) {
//            if (startRange == 0) {
//                recentAd.orderByChild(DBConstants.flatRent).endAt(endRange);
//            } else if (endRange == 0) {
//                recentAd.orderByChild(DBConstants.flatRent).startAt(startRange);
//            } else {
//                recentAd.orderByChild(DBConstants.flatRent).startAt(startRange).endAt(endRange);
//            }
//        }
//
//        recentAd = recentAd.limitToLast(100);
        adList.clear();
        showProgressDialog();
        recentAd.addListenerForSingleValueEvent(valueEventListener);
    }

    private void displayAdList() {
//        if (adAdapter == null) {
//            adAdapter = new AdAdapter(AdListActivity.this, adList, new AdAdapter.ClickListener() {
//                @Override
//                public void onItemClick(View view, int position, AdInfo adInfo) {
//                    startAdDetailsActivity(adInfo);
//                }
//
//                @Override
//                public void onFavClick(View view, int position, AdInfo adInfo) {
//                    if (view instanceof ImageView) {
//                        ((ImageView) view).setImageResource(R.drawable.ic_fav_selected);
//                    }
//                }
//            });
//            adRecyclerView.setAdapter(adAdapter);
//        } else {
//            adAdapter.setData(adList);
//            adAdapter.notifyDataSetChanged();
//        }
    }

    private static ArrayList<AdInfo> adList;
    private RecyclerView adRecyclerView;
    private AdAdapter adAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RequestManager manager;

    private class ViewItemDecoration extends RecyclerView.ItemDecoration {

        ViewItemDecoration() {
        }

        @Override
        public void getItemOffsets(Rect outRect, final View view, final RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int spanIndex = ((StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();

            float space = SplashActivity.metrics.density * 2.5f;
            if (spanIndex == 0) {
                outRect.right = (int) space;
            } else {
                outRect.left = (int) space;
            }
        }
    }

    private void init() {
        manager = Glide.with(this);
        adRecyclerView = findViewById(R.id.adRecyclerView);
        adRecyclerView.setHasFixedSize(true);
        adRecyclerView.addItemDecoration(new ViewItemDecoration());
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setAutoMeasureEnabled(true);
        adRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        adRecyclerView.addOnScrollListener(scrollListener);

        if (adList == null)
            adList = new ArrayList<>();

        if (adList.isEmpty())
            loadAllData();
        else
            displayAdList();
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (Math.abs(dy) > PickConfig.SCROLL_THRESHOLD) {
                manager.pauseRequests();
            } else {
                manager.resumeRequests();
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                manager.resumeRequests();
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ad_list_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.searchAction) {
            showSearchWindow();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Dialog searchDialog;

    private void showSearchWindow() {
        searchDialog = new Dialog(this);
        searchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        searchDialog.setContentView(R.layout.dialog_search);
        Window window = searchDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        }

        searchDialog.show();

        TextView title = searchDialog.findViewById(R.id.title);
        title.setText(getString(R.string.search));

        final RadioGroup rentType, rentTypeOthers;
        rentType = searchDialog.findViewById(R.id.rentType);
        rentTypeOthers = searchDialog.findViewById(R.id.rentTypeOthers);

        rentType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (rentType.getCheckedRadioButtonId() == R.id.others) {
                    rentTypeOthers.setVisibility(View.VISIBLE);
                } else {
                    rentTypeOthers.setVisibility(View.GONE);
                }
            }
        });

        final EditText rentMin, rentMax;
        rentMin = searchDialog.findViewById(R.id.rentMin);
        rentMax = searchDialog.findViewById(R.id.rentMax);

        searchDialog.findViewById(R.id.okBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String rentTypeString = null;
                if (rentType.getCheckedRadioButtonId() == R.id.family) {
                    rentTypeString = getString(R.string.family);
                } else if (rentType.getCheckedRadioButtonId() == R.id.mess) {
                    rentTypeString = getString(R.string.mess_member);
                } else if (rentType.getCheckedRadioButtonId() == R.id.sublet) {
                    rentTypeString = getString(R.string.sublet);
                } else {
                    if (rentTypeOthers.getCheckedRadioButtonId() == R.id.officeSpace) {
                        rentTypeString = getString(R.string.office_space);
                    } else if (rentTypeOthers.getCheckedRadioButtonId() == R.id.commercialSpace) {
                        rentTypeString = getString(R.string.commercial_space);
                    } else if (rentTypeOthers.getCheckedRadioButtonId() == R.id.miniShop) {
                        rentTypeString = getString(R.string.mini_shop);
                    } else if (rentTypeOthers.getCheckedRadioButtonId() == R.id.marketPlace) {
                        rentTypeString = getString(R.string.market_place);
                    } else if (rentTypeOthers.getCheckedRadioButtonId() == R.id.godown) {
                        rentTypeString = getString(R.string.godown);
                    } else {
                        rentTypeString = getString(R.string.others);
                    }
                }

                long rentMinLong = 0;
                if (!rentMin.getText().toString().trim().isEmpty()) {
                    rentMinLong = Long.parseLong(rentMin.getText().toString());
                }

                long rentMaxLong = 0;
                if (!rentMax.getText().toString().trim().isEmpty()) {
                    rentMaxLong = Long.parseLong(rentMax.getText().toString());
                }

                loadData(rentTypeString, rentMinLong, rentMaxLong);
                searchDialog.dismiss();
            }
        });

        searchDialog.findViewById(R.id.noBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchDialog.dismiss();
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
        if (firebaseUser.isAnonymous())
            childUpdates.put("/" + DBConstants.users + "/" + DBConstants.anonymousUsers + "/" + getUid(), userValues);
        else
            childUpdates.put("/" + DBConstants.users + "/" + DBConstants.registeredUsers + "/" + getUid(), userValues);
        mDatabase.updateChildren(childUpdates);
    }

    // https://developers.google.com/identity/sms-retriever/overview
    // Construct a request for phone numbers and show the picker
    private void requestHint() {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), 1, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }
}
