package com.example.gupsup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gupsup.Login.LoginActivity;

public class SplashActivity extends AppCompatActivity {


    private  ImageView imgSplash;
    private  TextView tvSplash;
    private Animation animation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgSplash=findViewById(R.id.imSplash);
        tvSplash=findViewById(R.id.tvSplash);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        //ActionBar actionBar = getActionBar();
        //actionBar.hide();


        animation= AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        imgSplash.startAnimation(animation);
        tvSplash.startAnimation(animation);
    }
}