package com.google.android.gms.samples.vision.face.facetracker;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import java.util.Random;

import static com.google.android.gms.samples.vision.face.facetracker.R.drawable.sakura;

public class RotateActivity extends AppCompatActivity {

    /*
    http://www.akexorcist.com/2014/07/android-code-object-animator.html
        • View.ALPHA = "alpha"
        • View.ROTATION = "rotation"
        • View.ROTATION_X = "rotationX"
        • View.ROTATION_Y = "rotationY"
        • View.SCALE_X = "scaleX"
        • View.SCALE_Y = "scaleY"
        • View.TRANSLATION_X = "translationX"
        • View.TRANSLATION_Y = "translationY"
        • View.X = "x"
        • View.Y = "y"
     */

    private ImageView sakura;

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);

        sakura = (ImageView) findViewById(R.id.sakura);

        Random r = new Random();
        boolean sign = r.nextDouble() > 0.5 ? true : false;
        long l = (long) (r.nextDouble() * 4500) + 1500;

        sign = r.nextDouble() > 0.5 ? true : false;
        float xScale = (float) ((r.nextInt(100)/100.0) * 0.99 + 0.25) * (sign ? 1 : -1);
        ScaleAnimation scaleXAnimation = new ScaleAnimation(sakura.getScaleX(), xScale, sakura.getScaleY(), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleXAnimation.setDuration(5000);
        scaleXAnimation.setInterpolator(new DecelerateInterpolator());

        l = (long) (r.nextDouble() * 4500) + 1500;

        sign = r.nextDouble() > 0.5 ? true : false;
        float yScale = (float) ((r.nextInt(100)/100.0) * 0.99 + 0.25) * (sign ? 1 : -1);
        ScaleAnimation scaleYAnimation = new ScaleAnimation(sakura.getScaleX(), 1f, sakura.getScaleY(), yScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleYAnimation.setDuration(5000);
        scaleYAnimation.setInterpolator(new DecelerateInterpolator());

        AnimationSet animSet = new AnimationSet(false);
        animSet.addAnimation(scaleXAnimation);
        animSet.addAnimation(scaleYAnimation);
//        sakura.startAnimation(animSet);

        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator spinX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, 0f, 359f);
        spinX.setDuration(5000);
        spinX.setInterpolator(new LinearInterpolator());
        spinX.setRepeatMode(ValueAnimator.RESTART);
        spinX.setRepeatCount(ValueAnimator.INFINITE);


        ObjectAnimator spinY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, 0f, 359f);
        spinY.setDuration(3000);
        spinY.setInterpolator(new LinearInterpolator());
        spinY.setRepeatMode(ValueAnimator.RESTART);
        spinY.setRepeatCount(ValueAnimator.INFINITE);

        animatorSet.play(spinX).with(spinY);
        animatorSet.start();

        Animator.AnimatorListener ll = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        spinX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


    }
}
