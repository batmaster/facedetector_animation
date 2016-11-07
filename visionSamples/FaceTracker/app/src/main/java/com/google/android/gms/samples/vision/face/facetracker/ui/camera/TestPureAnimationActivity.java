package com.google.android.gms.samples.vision.face.facetracker.ui.camera;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.samples.vision.face.facetracker.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

public class TestPureAnimationActivity extends AppCompatActivity {

    private RelativeLayout parent;
    private RelativeLayout layoutSakura;

    private Button buttonSakura;

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pure_animation);

        parent = (RelativeLayout) findViewById(R.id.parent);
        layoutSakura = (RelativeLayout) findViewById(R.id.layoutSakura);

        buttonSakura = (Button) findViewById(R.id.buttonSakura);
        buttonSakura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

//        while (i < 1000) {
//            createSakura(500, 500);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }


        final Handler mHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                createSakura(500, 500);
//                createSakura(520, 500);
//                createSakura(480, 500);

//                createSakura2(500, 500);
//                createSakura2(500, 500);
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.post(runnable);

//        final Handler mHandler2 = new Handler();
//        final Runnable runnable2 = new Runnable() {
//            @Override
//            public void run() {
//                getBitmapFromView(layoutSakura);
//                mHandler2.postDelayed(this, 1000);
//            }
//        };
//        parent.post(new Runnable() {
//            @Override
//            public void run() {
//                mHandler2.post(runnable2);
//            }
//        });

    }

    int SIZE = 100;
    int i = 0;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void createSakura(float x, float y) {

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 20 ? 0 :
                (k < 40 ? 1 :
                        (k < 60 ? 2 :
                                (k < 80 ? 3 :
                                        (k < 100 ? 4 :
                                                (k < 110 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x;
        params.topMargin = (int) y;

        int[] sa = {
                R.drawable.sakura,
                R.drawable.sakura2,
                R.drawable.sakura3,
                R.drawable.sakura4,
                R.drawable.sakura5,
                R.drawable.sakura6,
                R.drawable.sakura7
        };

        final ImageView sakura = new ImageView(getApplicationContext());
        sakura.setImageResource(sa[im]);
        sakura.setLayoutParams(params);

        layoutSakura.addView(sakura);


        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? 0 : 359;
        int endD = sign ? 359 : 0;


        RotateAnimation rotateAnimation = new RotateAnimation(startD, endD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration((long) (r.nextDouble() * 1000) + 400);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        long l = (long) (r.nextDouble() * 4500) + 1500;

        ScaleAnimation scaleAnimation = new ScaleAnimation(sakura.getScaleX(), 2f, sakura.getScaleY(), 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(l);
        scaleAnimation.setInterpolator(new DecelerateInterpolator());

        sign = r.nextDouble() > 0.5 ? true : false;
        float xScale = (float) ((r.nextInt(100) / 100.0) * 0.99 + 0.25) * (sign ? 1 : -1);
        ScaleAnimation scaleXAnimation = new ScaleAnimation(sakura.getScaleX(), xScale, sakura.getScaleY(), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleXAnimation.setDuration(l);
        scaleXAnimation.setInterpolator(new DecelerateInterpolator());


        TranslateAnimation translateAnimation = new TranslateAnimation(0, (long) (r.nextDouble() * 300) * (sign ? 1 : -1), 0, 1820 - y + (SIZE / 2));
        translateAnimation.setDuration(l);
        translateAnimation.setInterpolator(new DecelerateInterpolator());
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        AnimationSet animSet = new AnimationSet(false);
        animSet.addAnimation(rotateAnimation);
        animSet.addAnimation(scaleXAnimation);
        animSet.addAnimation(scaleAnimation);
        animSet.addAnimation(translateAnimation);


        sakura.startAnimation(animSet);

        Log.d("sakura", "create " + (i + 1));
        i++;
    }

    private static final int MAX_X = 1080;
    private static final int MAX_Y = 1920;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void createSakura2(float x, float y) {

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 20 ? 0 :
                (k < 40 ? 1 :
                        (k < 60 ? 2 :
                                (k < 80 ? 3 :
                                        (k < 100 ? 4 :
                                                (k < 110 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x;
        params.topMargin = (int) y;

        int[] sa = {
                R.drawable.sakura,
                R.drawable.sakura2,
                R.drawable.sakura3,
                R.drawable.sakura4,
                R.drawable.sakura5,
                R.drawable.sakura6,
                R.drawable.sakura7
        };

        final ImageView sakura = new ImageView(getApplicationContext());
        sakura.setImageResource(sa[im]);
        sakura.setLayoutParams(params);

        layoutSakura.addView(sakura);

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        boolean sign2 = r.nextDouble() > 0.5 ? true : false;

        int startD = sign ? 0 : 359;
        int endD = sign ? 359 : 0;

        RotateAnimation rotateAnimation = new RotateAnimation(startD, endD, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration((long) (r.nextDouble() * 1000) + 400);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        long l = (long) (r.nextDouble() * 4500) + 1500;

        ScaleAnimation scaleAnimation = new ScaleAnimation(sakura.getScaleX(), 2f, sakura.getScaleY(), 2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(l);
        scaleAnimation.setInterpolator(new DecelerateInterpolator());

        sign = r.nextDouble() > 0.5 ? true : false;
        float xScale = (float) ((r.nextInt(100) / 100.0) * 0.99 + 0.25) * (sign ? 1 : -1);
        ScaleAnimation scaleXAnimation = new ScaleAnimation(sakura.getScaleX(), 1f, sakura.getScaleY(), xScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleXAnimation.setDuration(l);
        scaleXAnimation.setInterpolator(new DecelerateInterpolator());

        l = (long) (r.nextDouble() * 4500) + 1500;

        sign = r.nextDouble() > 0.5 ? true : false;
        float yScale = (float) ((r.nextInt(100) / 100.0) * 0.99 + 0.25) * (sign ? 1 : -1);
        ScaleAnimation scaleYAnimation = new ScaleAnimation(sakura.getScaleX(), yScale, sakura.getScaleY(), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleYAnimation.setDuration(l);
        scaleYAnimation.setInterpolator(new DecelerateInterpolator());

        int x2 = r.nextInt(MAX_X);
        int y2 = r.nextInt(MAX_Y);

        TranslateAnimation translateAnimation = new TranslateAnimation(0, x2 * (sign ? 1 : -1), 0, y2 * (sign2 ? 1 : -1));
        translateAnimation.setDuration(l);
        translateAnimation.setInterpolator(new DecelerateInterpolator());
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        alphaAnimation.setDuration(l / 2);


        AnimationSet animSet = new AnimationSet(false);
        animSet.addAnimation(rotateAnimation);
        animSet.addAnimation(scaleAnimation);
        animSet.addAnimation(scaleXAnimation);
        animSet.addAnimation(scaleYAnimation);
        animSet.addAnimation(translateAnimation);
        animSet.addAnimation(alphaAnimation);


        sakura.startAnimation(animSet);

        Log.d("sakura", "create " + (i + 1));
        i++;
    }

    int fr = 0;

    public void getBitmapFromView(View view) {
        long l = SystemClock.currentThreadTimeMillis();
        Log.d("bitmaps", "Frame: " + fr + " start at " + new Date());
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);

        File file;
        File f = null;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            file = new File(android.os.Environment.getExternalStorageDirectory(), "frame");
            if (!file.exists()) {
                file.mkdirs();

            }
            f = new File(file.getAbsolutePath() + "/frame" + fr + ".png");
        }

        try {
            FileOutputStream ostream = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
            ostream.close();
            Log.d("bitmaps", "Frame: " + fr + " take" + (SystemClock.currentThreadTimeMillis() - l) + " ms");
            fr++;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
