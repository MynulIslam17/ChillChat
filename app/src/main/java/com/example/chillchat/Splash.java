package com.example.chillchat;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash extends AppCompatActivity {
    FirebaseAuth fAuth;
    Animation anim;
    ImageView appLogo;
    TextView tvDeveloperName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
         // find id's
        appLogo=findViewById(R.id.appLogo) ;
        tvDeveloperName=findViewById(R.id.tvDeveloperName);


        // create instance of firebase authentication
        fAuth=FirebaseAuth.getInstance();
        FirebaseUser  user=fAuth.getCurrentUser();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (user == null) {
                    Intent myInent=new Intent(Splash.this,Login.class);
                    startActivity(myInent);
                    finish();

                }else {

                    Intent myInent=new Intent(Splash.this,MainActivity.class);
                    startActivity(myInent);
                    finish();

                }



            }
        },4000);


        // set animation to the logo & devloper name
        anim= AnimationUtils.loadAnimation(Splash.this,R.anim.alpha);
         appLogo.startAnimation(anim);


       // create another alpha anim programetically
         anim=new AlphaAnimation(0.0f,1.0f);
         anim.setFillAfter(true);
         anim.setDuration(4000);

         tvDeveloperName.startAnimation(anim);




    }
}