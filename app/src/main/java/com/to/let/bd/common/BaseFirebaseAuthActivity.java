package com.to.let.bd.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.to.let.bd.R;
import com.to.let.bd.fcm.DeleteTokenService;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;
import com.to.let.bd.utils.MyAnalyticsUtil;

import java.util.HashMap;

public abstract class BaseFirebaseAuthActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseFirebaseAuthActivity.class.getSimpleName();

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;
    protected MyAnalyticsUtil myAnalyticsUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        myAnalyticsUtil = new MyAnalyticsUtil(this);
        initFirebase();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        onCreate();
    }

    // Google login
    private void googleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, AppConstants.GOOGLE_SIGN_IN);
    }

    // Google sign out
    public void googleSignOut() {
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
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == AppConstants.GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.getStatus().isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();

                if (account != null) {
                    firebaseLoginWithCredential(account);
                }
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyGoogleLogin, "succeed");
            } else {
                showToast(getString(R.string.google_login_failed));
                myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyGoogleLogin, "failed");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    // [END onActivityResult]

    private void firebaseLoginWithCredential(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
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
                            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyFirebaseLogin, "failed");
                        } else {
                            myAnalyticsUtil.sendEvent(MyAnalyticsUtil.keyFirebaseLogin, "succeed");
                            updateFirebaseUser(acct);
                        }
                    }
                });
    }

    private void updateFirebaseUser(GoogleSignInAccount acct) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(acct.getDisplayName())
                .setPhotoUri(acct.getPhotoUrl())
                .build();
        firebaseUser.updateProfile(profileUpdates);
        writeNewUser();
        setEmailAddress();
    }

    private FirebaseAuth mAuth;
    protected FirebaseUser firebaseUser;

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }

    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        showLog("Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        showLog("Play services connection suspended");
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    protected void logoutAndAnonymousLogin() {
        startService(new Intent(this, DeleteTokenService.class));
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                finish();
            } else {
                mAuth.signOut();
                signInAnonymously();
            }
        } else {
            finish();
        }
    }

    protected void signInAnonymously() {
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
                afterSuccessfulAnonymousLogin();
            }
        });
    }

    private DatabaseReference databaseReference;

    private void writeNewUser() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null)
            return;

        if (databaseReference == null)
            databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put(DBConstants.userId, getUid());
        userValues.put(DBConstants.fcmToken, FirebaseInstanceId.getInstance().getToken());

        if (firebaseUser.isAnonymous()) {
            databaseReference
                    .child(DBConstants.users)
                    .child(DBConstants.anonymousUsers)
                    .child(firebaseUser.getUid())
                    .updateChildren(userValues);
        } else {
            userValues.put(DBConstants.userEmail, firebaseUser.getEmail());
            userValues.put(DBConstants.userDisplayName, firebaseUser.getDisplayName());

            if (firebaseUser.getPhoneNumber() != null && !firebaseUser.getPhoneNumber().isEmpty())
                userValues.put(DBConstants.userPhoneNumber, firebaseUser.getPhoneNumber());

            if (firebaseUser.getPhotoUrl() != null)
                userValues.put(DBConstants.userProfilePic, firebaseUser.getPhotoUrl().toString());

            databaseReference
                    .child(DBConstants.users)
                    .child(DBConstants.registeredUsers)
                    .child(firebaseUser.getUid())
                    .updateChildren(userValues);
        }
    }

    protected abstract void afterSuccessfulAnonymousLogin();

    protected abstract int getLayoutResourceId();

    protected abstract void onCreate();

    protected abstract void setEmailAddress();
}
