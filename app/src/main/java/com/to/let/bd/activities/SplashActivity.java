package com.to.let.bd.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.to.let.bd.R;
import com.to.let.bd.common.BaseActivity;
import com.to.let.bd.utils.DBConstants;

import java.util.HashMap;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        firebaseInit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        handler.postDelayed(delayStartActivity, 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(delayStartActivity);
        handler.postDelayed(delayStartActivity, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(delayStartActivity);
    }

    private Handler handler = new Handler();

    private void delayedHide(int delayMillis) {
        handler.removeCallbacks(startActivityRunnable);
        handler.postDelayed(startActivityRunnable, delayMillis);
    }

    private Runnable startActivityRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };

    private Runnable delayStartActivity = new Runnable() {
        @Override
        public void run() {
            if (firebaseUser != null)
//                firebaseUser.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        startHomeActivity();
//                    }
//                });
                startHomeActivity();
            else
                signInAnonymously();
        }
    };

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private void firebaseInit() {
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
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth != null && mAuthListener != null) {
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuth != null && mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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
                startHomeActivity();
            }
        });
    }

    private DatabaseReference mDatabase;

    private void writeNewUser() {
        if (mDatabase == null)
            mDatabase = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> userValues = new HashMap<>();
        userValues.put(DBConstants.userId, getUid());

        HashMap<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + DBConstants.user + "/" + getUid(), userValues);
        mDatabase.updateChildren(childUpdates);
    }

    private void startHomeActivity() {
        finish();
        Intent intent = new Intent(this, AdListActivity.class);
        startActivity(intent);
    }
}
