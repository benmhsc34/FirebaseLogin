package com.openclassrooms.firebaseoc;

import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.openclassrooms.firebaseoc.api.UserHelper;
import com.openclassrooms.firebaseoc.auth.ProfileActivity;
import com.openclassrooms.firebaseoc.base.BaseActivity;
import com.openclassrooms.firebaseoc.mentor_chat.MentorChatActivity;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_activity_button_login)
    Button buttonLogin;

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_main;
    }

    //FOR DATA
    // 1 - Identifier for Sign-In Activity
    private static final int RC_SIGN_IN = 123;

    // --------------------
    // ACTIONS
    // --------------------

    @Override
    protected void onResume() {
        super.onResume();
        // 5 - Update UI when activity is resuming
        this.updateUIWhenResuming();
    }

    @OnClick(R.id.main_activity_button_login)
    public void onClickLoginButton() {
        // 4 - Start appropriate activity
        if (this.isCurrentUserLogged()){
            this.startProfileActivity();
        } else {
            this.startSignInActivity();
        }
    }

    @OnClick(R.id.main_activity_button_chat)
    public void onClickChatButton() {
        // 2 - Check if user is connected before launching MentorActivity
        if (this.isCurrentUserLogged()){
            this.startMentorChatActivity();
        } else {
            this.showSnackBar(this.coordinatorLayout, getString(R.string.error_not_connected));
        }
    }


    // --------------------
    // NAVIGATION
    // --------------------

    // 2 - Launch Sign-In Activity
    private void startSignInActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), //EMAIL
                                         new AuthUI.IdpConfig.GoogleBuilder().build(), // SUPPORT GOOGLE
                                         new AuthUI.IdpConfig.FacebookBuilder().build())) // FACEBOOK
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_logo_auth)
                        .build(),
                RC_SIGN_IN);

    }

    private void startMentorChatActivity(){
        Intent intent = new Intent(this, MentorChatActivity.class);
        startActivity(intent);
    }

    // 3 - Launching Profile Activity
    private void startProfileActivity(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    //FOR DESIGN
    // 1 - Get Coordinator Layout
    @BindView(R.id.main_activity_coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 4 - Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }


    // --------------------
    // UI
    // --------------------

    //Show Snack Bar with a message
    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    //Update UI when activity is resuming
    private void updateUIWhenResuming(){
        this.buttonLogin.setText(this.isCurrentUserLogged() ? getString(R.string.button_login_text_logged) : getString(R.string.button_login_text_not_logged));
    }

    // --------------------
    // UTILS
    // --------------------

    // 3 - Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
                showSnackBar(this.coordinatorLayout, getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    // --------------------
    // REST REQUEST
    // --------------------

    // 1 - Http request that create user in firestore
    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }



}

