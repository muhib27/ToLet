package com.to.let.bd.common;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.to.let.bd.R;
import com.to.let.bd.activities.AdDetailsActivity;
import com.to.let.bd.activities.NewAdActivity2;
import com.to.let.bd.model.AdInfo;
import com.to.let.bd.utils.AppConstants;
import com.to.let.bd.utils.DBConstants;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected void updateTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
        }

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        if (toolbarTitle != null) {
            toolbarTitle.setSelected(true);
            toolbarTitle.setText(title);
        }
    }

    private ProgressDialog progressDialog;

    public void showProgressDialog() {
        showProgressDialog("", getString(R.string.loading));
    }

    public void showProgressDialog(String message) {
        showProgressDialog("", message);
    }

    public void showProgressDialog(String title, String message) {
        showProgressDialog(title, message, false);
    }

    public void showProgressDialog(String title, String message, boolean cancelable) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, title, message);
            progressDialog.setCancelable(cancelable);
        } else {
            progressDialog.show();
        }
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showLog(String message) {
        if (message == null)
            message = "null";
        Log.d(TAG, message);
    }

    public void showLog() {
        showLog("Test log");
    }

    public void showToast() {
        showToast(R.string.app_name);
    }

    public void showToast(int resourceId) {
        showToast(getString(resourceId));
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean isEmailValid(String emailAddress) {
        return Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches();
    }

    public boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
//        LoginManager.getInstance().logOut();
    }

//    public void firebaseAppInvites() {
//        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.app_name))
//                .setMessage(getString(R.string.invitation_message))
//                .setDeepLink(Uri.parse("https://z55f5.app.goo.gl/eaFH"))
//                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
//                .setCallToActionText("CallToActionText")
//                .build();
//        startActivityForResult(intent, KgbhsConstants.firebaseInviteType);
//    }
//
//    public void sendGraphRequest() {
//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/{app-request-id}",
//                null,
//                HttpMethod.GET,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//            /* handle the result */
//                    }
//                }
//        ).executeAsync();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//
//        MenuItem loginLogoutItem = menu.findItem(R.id.action_login_logout);
//        if (!KgbhsConstants.isValidUserForAction()) {
//            loginLogoutItem.setIcon(R.drawable.login_icon);
//            loginLogoutItem.setTitle(R.string.login);
//        } else {
//            loginLogoutItem.setIcon(R.drawable.logout_icon);
//            loginLogoutItem.setTitle(R.string.log_out);
//        }
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
//        if (id == R.id.action_login_logout) {
//            if (!KgbhsConstants.isValidUserForAction()) {
//                startLogin();
//            } else {
//                logout();
//                finish();
//            }
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

//    public void startLogin() {
//        Intent authenticationIntent = new Intent(this, LoginActivity.class);
//        authenticationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivityForResult(authenticationIntent, KgbhsConstants.LOGIN_RESULT);
//    }

    public FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        return user.getUid();
    }

    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

//    public void gotoSpecificActivity(Intent intent, int type) {
//        FirebaseUser firebaseUser = getCurrentUser();
//        if (firebaseUser == null || firebaseUser.isAnonymous()) {
//            Intent loginIntent = new Intent(this, LoginActivity.class);
//            startActivityForResult(loginIntent, type);
//            return;
//        }
//        startActivity(intent);
//    }

    protected void startNewAdActivity() {
        Intent newAdIntent = new Intent(this, NewAdActivity2.class);
        newAdIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(newAdIntent, AppConstants.newAdType);
    }

    public void startAdDetailsActivity(AdInfo adInfo) {
        Intent adDetailsIntent = new Intent(this, AdDetailsActivity.class);
//        adDetailsIntent.putExtra(DBConstants.adId, adInfo.getAdId());
//        adDetailsIntent.putExtra(DBConstants.flatRent, adInfo.getFlatRent());
//        adDetailsIntent.putExtra(DBConstants.othersFee, adInfo.getOthersFee());

//        adDetailsIntent.putExtra(DBConstants.bedRoom, adInfo.getBedRoom());
//        adDetailsIntent.putExtra(DBConstants.bathroom, adInfo.getBathroom());
//        adDetailsIntent.putExtra(DBConstants.balcony, adInfo.getBalcony());

//        adDetailsIntent.putExtra(DBConstants.startingDate, adInfo.getStartingDate());
//        adDetailsIntent.putExtra(DBConstants.startingMonth, adInfo.getStartingMonth());
//        adDetailsIntent.putExtra(DBConstants.startingYear, adInfo.getStartingYear());
//
//        adDetailsIntent.putExtra(DBConstants.latitude, adInfo.getLatitude());
//        adDetailsIntent.putExtra(DBConstants.longitude, adInfo.getLongitude());
//        adDetailsIntent.putExtra(DBConstants.flatSpace, adInfo.getFlatSpace());
//
//        adDetailsIntent.putExtra(DBConstants.fullAddress, adInfo.getFullAddress());
//
//        if (!(adInfo.getImages() == null || adInfo.getImages().isEmpty())) {
//            String[] images = new String[adInfo.getImages().size()];
//            for (int i = 0; i < adInfo.getImages().size(); i++) {
//                images[i] = adInfo.getImages().get(i).getDownloadUrl();
//            }
//            adDetailsIntent.putExtra(DBConstants.images, images);
//        }
//
//        if (adInfo.getMap() != null) {
//            adDetailsIntent.putExtra(DBConstants.map, adInfo.getMap().getDownloadUrl());
//        }
        adDetailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        adDetailsIntent.putExtra(AppConstants.keyAdInfo, adInfo);
        startActivity(adDetailsIntent);
    }

    protected void shareAction() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey check out the latest To-Let app:\nhttps://play.google.com/store/apps/details?id=" +
                getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void showSimpleDialog(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setIcon(R.mipmap.ic_launcher_round);
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void showSimpleDialog(int messageResourceId) {
        showSimpleDialog(getString(messageResourceId));
    }
}
