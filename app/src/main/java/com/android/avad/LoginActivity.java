package com.android.avad;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import me.arulnadhan.AchievementUnlockedLib.AchievementUnlocked;

/**
 * Created by sagar_000 on 7/27/2016.
 */
public class LoginActivity extends BaseActivity  {

    private static final String TAG = "FacebookLogin";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private CallbackManager mCallbackManager;

    @TargetApi(Build.VERSION_CODES.M)
    public void androidM() {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 123);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        final ImageView imgLogo = (ImageView) findViewById(R.id.icon);
        final LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setVisibility(View.GONE);


        Animation animTranslate = AnimationUtils.loadAnimation(this, R.anim.translate);
        animTranslate.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                loginButton.setVisibility(View.VISIBLE);
                Animation animFade = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.fade);
                loginButton.startAnimation(animFade);

            }
        });
        imgLogo.startAnimation(animTranslate);

        if (Build.VERSION.SDK_INT >= 23) {

            if (!Settings.canDrawOverlays(getApplicationContext()))
                new AlertDialog.Builder(this)

                        .setMessage("Starting from Android 6, " + getResources().getString(R.string.app_name) + " needs permission to display notifications. Click enable to proceed")
                        .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                androidM();
                            }
                        })

                        .show();

        }
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };

        mCallbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions("email", "public_profile", "user_birthday");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    loginToast(profile.getName());
                }

                final AccessToken accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                        String gender = user.optString("gender");
                        String age = user.optString("birthday");
                        Log.i(TAG, "Gender:- " + gender);
                        Log.i(TAG, "Age:- " + age);

                        Log.i("MyLog", user.toString());
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
    }

    public void loginToast(String user) {
        AchievementUnlocked toast = new AchievementUnlocked(this).setTitle("Welcome " + user).setBackgroundColor(Color.parseColor("#333333")).setTitleColor(0xffffffff).setIcon(getDrawable(R.drawable.com_facebook_button_icon)).setDuration(1000).alignTop(false).isLarge(false).build();
        toast.show();
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Couldn't get the permission, terminating process", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            findViewById(R.id.button_facebook_login).setVisibility(View.VISIBLE);
        }
    }
}
