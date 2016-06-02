package com.example.sociallogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import io.fabric.sdk.android.Fabric;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    //Twitter
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "GzE4GATBFLl7S9SxI2sWcNMA6";
    private static final String TWITTER_SECRET = "2J5xJywEk8p9Vl3Rst5DgpXedDpsGKUOVzD83zSYP0J2HhHH9x";
    //Tags to send the username and image url to next activity using intent
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PROFILE_IMAGE_URL = "image_url";

    //Twitter Login Button
    TwitterLoginButton twitterLoginButton;


    private CallbackManager callbackManager;
    private LoginButton fbLoginButton;
    static String Name, Email, ProfilePic, DOB, ID;
    Button bt_login;
    String ImgURL;

    //Signin button
    private SignInButton signInButton;
    //Signing Options
    private GoogleSignInOptions gso;
    //google api client
    private GoogleApiClient mGoogleApiClient;
    //Signin constant to check the activity result
    private int RC_SIGN_IN = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        //You need this method to be used only once to configure
        //your key hash in your App Console at
        // developers.facebook.com/apps

        showHashKey(MainActivity.this);
        // creating connection detector class instance

        setContentView(R.layout.activity_main);

        //Initializing google signin option
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //Initializing twitter login button
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitterLogin);
        //Adding callback to the button
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                //If login succeeds passing the Calling the login method and passing Result object
                login(result);
            }

            @Override
            public void failure(TwitterException exception) {
                //If failure occurs while login handle it here
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

        //Initializing signinbutton
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        bt_login = (Button) findViewById(R.id.bt_login);
        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        fbLoginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));

        //Initializing google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(MainActivity.this /* FragmentActivity */,MainActivity.this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        FaceBookLogin();

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends"));
            }
        });

        //Setting onclick listener to signing button
        signInButton.setOnClickListener(this);

    }

    //The twitter login function accepting the result object
    public void login(Result<TwitterSession> result) {

        //Creating a twitter session with result's data
        TwitterSession session = result.data;

        //Getting the username from session
        final String username = session.getUserName();
        Long  userid = session.getUserId();

        //This code will fetch the profile image URL
        //Getting the account service of the user logged in
        Twitter.getApiClient(session).getAccountService()
                .verifyCredentials(true, false, new Callback<User>() {
                    @Override
                    public void failure(TwitterException e) {
                        //If any error occurs handle it here
                    }

                    @Override
                    public void success(Result<User> userResult) {
                        //If it succeeds creating a User object from userResult.data
                        User user = userResult.data;

                        //Getting the profile image url
                        String profileImage = user.profileImageUrl.replace("_normal", "");
//                        String Email = user.email.replace("email", "");

                        //Creating an Intent
                        Intent intent = new Intent(MainActivity.this, AfterLogin.class);

                        //Adding the values to intent
                        intent.putExtra("Name",username);
                        intent.putExtra("ImgURL", profileImage);
//                        intent.putExtra("Email", Email);

                        Log.i("Twitter", "success: "+profileImage );
                        Log.i("Twitter", "success: "+username);
                        //Starting intent
                        startActivity(intent);
                    }
                });
    }

    //This function will option signing intent
    private void signIn() {
        //Creating an intent
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        //Starting intent for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void FaceBookLogin() {

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                // App code
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                Log.v("Profile ---------   ", response.toString());

                                try {
                                    ID = object.getString("id");
                                    Name = object.getString("name");
                                    DOB = object.optString("birthday");
                                    Email = object.optString("email");
                                    ImgURL = "https://graph.facebook.com/" + ID + "/picture?type=large";
                                    Log.v("Email = ", " " + Email);
                                    //                                    Toast.makeText(getApplicationContext(), "Name " + Name, Toast.LENGTH_LONG).show();

                                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(MainActivity.this, AfterLogin.class);
                                    intent.putExtra("Name", Name);
                                    intent.putExtra("Email", Email);
                                    intent.putExtra("DOB", DOB);
                                    intent.putExtra("ID", ID);
                                    intent.putExtra("ImgURL", ImgURL);

                                    startActivity(intent);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

                System.out.println("Facebook Login Successful!");
                System.out.println("Logged in user Details : ");
                System.out.println("--------------------------");
                System.out.println("User ID  : " + loginResult.getAccessToken().getUserId());
                System.out.println("Authentication Token : " + loginResult.getAccessToken().getToken());

            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Login cancelled by user!", Toast.LENGTH_LONG).show();
                System.out.println("Facebook Login failed!!");

            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(MainActivity.this, "Login unsuccessful!", Toast.LENGTH_LONG).show();
                System.out.println("Facebook Login failed!! because of " + e.getCause().toString());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    public static void showHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.example.arpit.facebooklogin", PackageManager.GET_SIGNATURES); //Your            package name here
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent i) {
        callbackManager.onActivityResult(reqCode, resCode, i);
        twitterLoginButton.onActivityResult(reqCode, resCode, i);
        //If signin
        if (reqCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(i);
            //Calling a new function to handle signin
            handleSignInResult(result);
        }
    }


    //After the signing we are calling this function
    private void handleSignInResult(GoogleSignInResult result) {
        //If the login succeed
        if (result.isSuccess()) {
            //Getting google account
            GoogleSignInAccount acct = result.getSignInAccount();

            ID = acct.getId();
            Name = acct.getDisplayName();
//            DOB = acct.optString("birthday");
            Email = acct.getEmail();
            ProfilePic = acct.getPhotoUrl().toString();
            Log.v("Email = ", " " + Email);

            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();
            ImgURL=acct.getPhotoUrl().toString();
            Intent intent = new Intent(MainActivity.this, AfterLogin.class);
            intent.putExtra("Name", Name);
            intent.putExtra("Email", Email);
            intent.putExtra("ImgURL", ImgURL);
            intent.putExtra("ID", ID);
            Log.i("", "image url: "+ImgURL);
            startActivity(intent);

        } else {
            //If login fails
            Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("", "onConnectionFailed: "+connectionResult);
    }

    @Override
    public void onClick(View v) {
        if (v == signInButton) {
            //Calling signin
            signIn();
        }
    }
}
