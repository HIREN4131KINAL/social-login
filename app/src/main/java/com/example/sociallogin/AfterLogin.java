package com.example.sociallogin;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.picasso.Picasso;

public class AfterLogin extends AppCompatActivity {

    TextView NameTv, EmailTv, DOBTv;
    ImageView ProfilePicImg;
    Button Logout;
    String ImgURL;
    static String Name, Email, ProfilePic, DOB, ID;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInAccount acct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        NameTv = (TextView) findViewById(R.id.Name);
        EmailTv = (TextView) findViewById(R.id.Email);
        DOBTv = (TextView) findViewById(R.id.Dob);
        Logout = (Button) findViewById(R.id.btn_Logout);
        ProfilePicImg = (ImageView) findViewById(R.id.imageView);

//        Log.i("", "image url : "+acct.getPhotoUrl().toString());
        String Name = getIntent().getStringExtra("Name");
        String Email = getIntent().getStringExtra("Email");
        String DOB = getIntent().getStringExtra("DOB");
        String ID = getIntent().getStringExtra("ID");
        String ProfilePic = getIntent().getStringExtra("ImgURL");

        //For the profile picture
        try {

//            ImgURL = "https://graph.facebook.com/" + ID + "/picture?type=large";
            Picasso.with(AfterLogin.this)
                    .load("" + ProfilePic)
                    .into(ProfilePicImg);
        } catch (Exception e) {

            e.printStackTrace();
        }

        NameTv.setText("Name :" + Name);
        EmailTv.setText("Email : " + Email);
        DOBTv.setText("DOB : " + DOB);

        Log.i("", "Name : " + Name);
        Log.i("", "Email : " + Email);
        Log.i("", "DOB : " + DOB);
        Log.i("", "ID : " + ID);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(AfterLogin.this, "Logout clicked...", Toast.LENGTH_SHORT).show();
                //For Logout Facebook SDK
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(AfterLogin.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }

}
