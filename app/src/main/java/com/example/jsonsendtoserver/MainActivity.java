package com.example.jsonsendtoserver;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

    public class MainActivity extends AppCompatActivity {


        private static final String TAG = MainActivity.class.getSimpleName();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.splash_screen);

            final TextView textView = findViewById(R.id.loading);
            final ImageView logo = findViewById(R.id.logo);

            final Animation loadingAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            final Animation theRest = AnimationUtils.loadAnimation(this, R.anim.fade_in);



            loadingAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    textView.startAnimation(theRest);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG,"splash screen run ");
                            Intent intent = new Intent(MainActivity.this, UserPrefActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        }
                    }, 4000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            logo.startAnimation(loadingAnim);
        }
    }
