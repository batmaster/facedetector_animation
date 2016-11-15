package com.google.android.gms.samples.vision.face.facetracker.ui.camera;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.samples.vision.face.facetracker.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class TestPureAnimationActivity extends AppCompatActivity {

    private RelativeLayout parent;
    private ClonableRelativeLayout layoutSakura;

    private TextView tVolume1;
    private SeekBar sVolume1;

    private TextView tSpeed1;
    private SeekBar sSpeed1;

    private TextView tFSpeed1;
    private SeekBar sFSpeed1;

    private TextView tSpeedX1;
    private SeekBar sSpeedX1;

    private TextView tFSpeedX1;
    private SeekBar sFSpeedX1;

    private TextView tSpeedY1;
    private SeekBar sSpeedY1;

    private TextView tFSpeedY1;
    private SeekBar sFSpeedY1;

    private TextView tStartSize1;
    private SeekBar sStartSize1;

    private TextView tFinalSize1;
    private SeekBar sFinalSize1;


    private TextView tVolume2;
    private SeekBar sVolume2;

    private TextView tSpeed2;
    private SeekBar sSpeed2;

    private TextView tFSpeed2;
    private SeekBar sFSpeed2;

    private TextView tSpeedX2;
    private SeekBar sSpeedX2;

    private TextView tFSpeedX2;
    private SeekBar sFSpeedX2;

    private TextView tSpeedY2;
    private SeekBar sSpeedY2;

    private TextView tFSpeedY2;
    private SeekBar sFSpeedY2;

    private TextView tStartSize2;
    private SeekBar sStartSize2;

    private TextView tFinalSize2;
    private SeekBar sFinalSize2;

    private ToggleButton togBeem;
    private ToggleButton togFlora;

    private boolean beem = true;
    private boolean beemEars = true;
    private boolean beemEyes = true;
    private boolean flora = true;

    private int volume1 = 10;
    private int speed1 = 10;
    private int fspeed1 = 14;
    private int speedx1 = 1;
    private int fspeedx1 = 3;
    private int speedy1 = 5;
    private int fspeedy1 = 10;
    private int startSize1 = 0;
    private int finalSize1 = 15;

    private int volume2 = 2;
    private int speed2 = 19;
    private int fspeed2 = 19;
    private int speedx2 = 2;
    private int fspeedx2 = 3;
    private int speedy2 = 2;
    private int fspeedy2 = 3;
    private int startSize2 = 1;
    private int finalSize2 = 17;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pure_animation);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        MAX_X = size.x;
        MAX_Y = size.y;
        SIZE = size.x/10;

        parent = (RelativeLayout) findViewById(R.id.parent);
        layoutSakura = (ClonableRelativeLayout) findViewById(R.id.layoutSakura);

        tVolume1 = (TextView) findViewById(R.id.tVolume1);
        sVolume1 = (SeekBar) findViewById(R.id.sVolume1);
        sVolume1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    volume1 = 1;
                }
                volume1 = progress;
                tVolume1.setText("ปริมาณการพุ่ง " + volume1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tSpeed1 = (TextView) findViewById(R.id.tSpeed1);
        sSpeed1 = (SeekBar) findViewById(R.id.sSpeed1);
        sSpeed1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    speed1 = 1;
                }
                speed1 = progress;
                tSpeed1.setText("ความแรงการพุ่ง " + speed1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFSpeed1 = (TextView) findViewById(R.id.tFSpeed1);
        sFSpeed1 = (SeekBar) findViewById(R.id.sFSpeed1);
        sFSpeed1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    fspeed1 = 1;
                }
                fspeed1 = progress;
                tFSpeed1.setText("factor ความแรงการพุ่ง " + fspeed1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tSpeedX1 = (TextView) findViewById(R.id.tSpeedX1);
        sSpeedX1 = (SeekBar) findViewById(R.id.sSpeedX1);
        sSpeedX1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    speedx1 = 1;
                }
                speedx1 = progress;
                tSpeedX1.setText("ความแรงการหมุนแกน X " + speedx1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFSpeedX1 = (TextView) findViewById(R.id.tFSpeedX1);
        sFSpeedX1 = (SeekBar) findViewById(R.id.sFSpeedX1);
        sFSpeedX1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    fspeedx1 = 1;
                }
                fspeedx1 = progress;
                tFSpeedX1.setText("factor ความแรงการหมุนแกน X " + fspeedx1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tSpeedY1 = (TextView) findViewById(R.id.tSpeedY1);
        sSpeedY1 = (SeekBar) findViewById(R.id.sSpeedY1);
        sSpeedY1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    speedy1 = 1;
                }
                speedy1 = progress;
                tSpeedY1.setText("ความแรงการหมุนแกน Y " + speedy1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFSpeedY1 = (TextView) findViewById(R.id.tFSpeedY1);
        sFSpeedY1 = (SeekBar) findViewById(R.id.sFSpeedY1);
        sFSpeedY1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    fspeedy1 = 1;
                }
                fspeedy1 = progress;
                tFSpeedY1.setText("factor ความแรงการหมุนแกน Y " + fspeedy1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tStartSize1 = (TextView) findViewById(R.id.tStartSize1);
        sStartSize1 = (SeekBar) findViewById(R.id.sStartSize1);
        sStartSize1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    startSize1 = 1;
                }
                startSize1 = progress;
                tStartSize1.setText("ขนาดเล็กสุดตอนเริ่ม " + startSize1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFinalSize1 = (TextView) findViewById(R.id.tFinalSize1);
        sFinalSize1 = (SeekBar) findViewById(R.id.sFinalSize1);
        sFinalSize1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    finalSize1 = 1;
                }
                finalSize1 = progress;
                tFinalSize1.setText("ขนาดใหญ่สุดก่อนหาย " + finalSize1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        tVolume2 = (TextView) findViewById(R.id.tVolume2);
        sVolume2 = (SeekBar) findViewById(R.id.sVolume2);
        sVolume2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    volume2 = 1;
                }
                volume2 = progress;
                tVolume2.setText("ปริมาณการพุ่ง " + volume2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tSpeed2 = (TextView) findViewById(R.id.tSpeed2);
        sSpeed2 = (SeekBar) findViewById(R.id.sSpeed2);
        sSpeed2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    speed2 = 1;
                }
                speed2 = progress;
                tSpeed2.setText("ความแรงการพุ่ง " + speed2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFSpeed2 = (TextView) findViewById(R.id.tFSpeed2);
        sFSpeed2 = (SeekBar) findViewById(R.id.sFSpeed2);
        sFSpeed2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    fspeed2 = 1;
                }
                fspeed2 = progress;
                tFSpeed2.setText("factor ความแรงการพุ่ง " + fspeed2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tSpeedX2 = (TextView) findViewById(R.id.tSpeedX2);
        sSpeedX2 = (SeekBar) findViewById(R.id.sSpeedX2);
        sSpeedX2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    speedx2 = 1;
                }
                speedx2 = progress;
                tSpeedX2.setText("ความแรงการหมุนแกน X " + speedx2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFSpeedX2 = (TextView) findViewById(R.id.tFSpeedX2);
        sFSpeedX2 = (SeekBar) findViewById(R.id.sFSpeedX2);
        sFSpeedX2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    fspeedx2 = 1;
                }
                fspeedx2 = progress;
                tFSpeedX2.setText("factor ความแรงการหมุนแกน X " + fspeedx2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tSpeedY2 = (TextView) findViewById(R.id.tSpeedY2);
        sSpeedY2 = (SeekBar) findViewById(R.id.sSpeedY2);
        sSpeedY2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    speedy2 = 1;
                }
                speedy2 = progress;
                tSpeedY2.setText("ความแรงการหมุนแกน Y " + speedy2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFSpeedY2 = (TextView) findViewById(R.id.tFSpeedY2);
        sFSpeedY2 = (SeekBar) findViewById(R.id.sFSpeedY2);
        sFSpeedY2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    fspeedy2 = 1;
                }
                fspeedy2 = progress;
                tFSpeedY2.setText("factor ความแรงการหมุนแกน Y " + fspeedy2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tStartSize2 = (TextView) findViewById(R.id.tStartSize2);
        sStartSize2 = (SeekBar) findViewById(R.id.sStartSize2);
        sStartSize2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    startSize2 = 1;
                }
                startSize2 = progress;
                tStartSize2.setText("ขนาดเล็กสุดตอนเริ่ม " + startSize2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tFinalSize2 = (TextView) findViewById(R.id.tFinalSize2);
        sFinalSize2 = (SeekBar) findViewById(R.id.sFinalSize2);
        sFinalSize2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    finalSize2 = 1;
                }
                finalSize2 = progress;
                tFinalSize2.setText("ขนาดใหญ่สุดก่อนหาย " + finalSize2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

        togBeem = (ToggleButton) findViewById(R.id.togBeem);
        togBeem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                beem = isChecked;
            }
        });

        togFlora = (ToggleButton) findViewById(R.id.togFlora);
        togFlora.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                flora = isChecked;
            }
        });

        final Handler mHandler01 = new Handler();
        mHandler01.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (beem) {
                    createSakura(MAX_X / 2, MAX_X / 2);
                }

                mHandler01.postDelayed(this, (500 / volume1));
            }
        });

        final Handler mHandler0 = new Handler();
        mHandler0.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (beem) {
                    createSakura(MAX_X / 2, MAX_X / 2);
                }

                mHandler0.postDelayed(this, (500 / volume1));
            }
        });


        final Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (beem) {
                    createSakura(MAX_X / 2, MAX_X / 2);
                }

                mHandler.postDelayed(this, (500 / volume1));
            }
        });

        final Handler mHandler2 = new Handler();
        mHandler2.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (flora) {
                    createSakura2(MAX_X / 2, MAX_X / 2);
                }

                mHandler.postDelayed(this, (500 / volume2));
            }
        });


//        final Handler mHandler3 = new Handler();
//        mHandler3.post(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
//            @Override
//            public void run() {
//
//                if (beemEars) {
//                    createSakuraEars1((MAX_X / 2) + 150, (MAX_X / 2) - 50);
//                    createSakuraEars2((MAX_X / 2) - 200, (MAX_X / 2) - 50);
//
//                }
//
//                mHandler3.postDelayed(this, (long) (1.5 * 500 / volume1));
//            }
//        });
//
//        final Handler mHandler4 = new Handler();
//        mHandler4.post(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
//            @Override
//            public void run() {
//
//                if (beemEyes) {
//                    createSakuraEyes1((MAX_X / 2) + 100, (MAX_X / 2) - 120);
//                    createSakuraEyes2((MAX_X / 2) - 150, (MAX_X / 2) - 120);
//
//                }
//
//                mHandler4.postDelayed(this, (long) (1.5 * 500 / volume1));
//            }
//        });


        final ArrayList<View> views = new ArrayList<View>();

//        final Handler mHandler3 = new Handler();
//        parent.post(new Runnable() {
//            @Override
//            public void run() {
//                mHandler3.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        views.add((View) layoutSakura.clone());
//
////                        getBitmapFromView(layoutSakura);
//
//                        Log.d("sizeee", views.size() + "");
//                        if (views.size() > 250) {
//                            for (int i = 0; i < views.size(); i++) {
//                                getBitmapFromView(views.get(i));
//                            }
//                        }
//                        else {
//                            mHandler.postDelayed(this, 1000 / 25);
//                        }
//                    }
//                });
//            }
//        });



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

    int SIZE = 0;
    int i = 0;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void createSakura(float x, float y) {

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        params.topMargin = (int) y - (SIZE / 2);

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

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * fspeedx1) + 8000) / (speedx1/10.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * fspeedy1) + 8000) / (speedy1/10.0));

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * fspeed1) + 2000) / (speed1/10.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        float posX = (float) (r.nextDouble() * (MAX_X/2.0) * (sign ? 1 : -1));

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, posX);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, MAX_Y - y + (SIZE / 2));
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);
        translationY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sakura.setLayerType(View.LAYER_TYPE_NONE, null);

                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
                animatorSet.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


//        ObjectAnimator alpha = ObjectAnimator.ofFloat(sakura, View.ALPHA, 1f, 0f);
//        alpha.setInterpolator(new AccelerateInterpolator());
//        alpha.setDuration(duration);
//        alpha.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                sakura.clearAnimation();
//                sakura.setVisibility(View.GONE);
//                layoutSakura.removeView(sakura);
//                animatorSet.cancel();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });

        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY/*, alpha*/);
        animatorSet.start();

        i++;
    }

    private static int MAX_X = 1080;
    private static int MAX_Y = 1920;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void createSakura2(float x, float y) {

        Random r = new Random();

        if (r.nextDouble() < 0.3) {
            return;
        }

        int k = r.nextInt(120);

        int im = (k < 20 ? 0 :
                (k < 60 ? 1 :
                        (k < 80 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        params.topMargin = (int) y - (SIZE / 2);

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

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (r.nextDouble() * 1000 * fspeedx2) + 15000 + (1000 * (20 - speedx2));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 1000 * fspeedy2) + 15000 + (1000 * (20 - speedy2));

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * fspeed2) + 4000) / (speed2/10.0));

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.25 * finalSize2);
        duration = (long) (((r.nextDouble() * 400.0 * fspeed2) + 2500) / (speed2/10.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.02f * startSize2)), sakura.getScaleX() * (1f + (0.02f * startSize2)) + scale);
        scaleX.setInterpolator(new AccelerateInterpolator());
        scaleX.setDuration(duration);

        Log.d("scaleee", sakura.getScaleX() * (1f + (0.02f * startSize2)) + " " + scale);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.02f * startSize2)), sakura.getScaleX() * (1f + (0.02f * startSize2)) + scale);
        scaleY.setInterpolator(new AccelerateInterpolator());
        scaleY.setDuration(duration);

        duration = (long) (((r.nextDouble() * 500.0 * fspeed2) + 2500) / (speed2/10.0));
        sign = r.nextDouble() > 0.5 ? true : false;
        int x2 = r.nextInt(MAX_X) + 250;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, x2 * (sign ? 1 : -1));
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        sign = r.nextDouble() > 0.5 ? true : false;
        int y2 = r.nextInt((int) (MAX_Y / 1.5)) + 250;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, y2 * (sign ? 1 : -1));
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);

        duration = (long) (r.nextDouble() * 3000) + 1500;
        ObjectAnimator alpha = ObjectAnimator.ofFloat(sakura, View.ALPHA, 1f, 0f);
        alpha.setInterpolator(new AccelerateInterpolator());
        alpha.setDuration(duration);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sakura.setLayerType(View.LAYER_TYPE_NONE, null);

                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
                animatorSet.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY, alpha);
        animatorSet.start();

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
            f = new File(file.getAbsolutePath() + "/frameX" + fr + ".png");
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


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void createSakuraEars1(float x, float y) {

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        params.topMargin = (int) y - (SIZE / 2);

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

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * fspeedx1) + 8000) / (speedx1/5.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * fspeedy1) + 8000) / (speedy1/5.0));

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * fspeed1) + 2000) / (speed1/7.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, MAX_X - x + SIZE);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);
        translationX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sakura.setLayerType(View.LAYER_TYPE_NONE, null);

                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
                animatorSet.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        sign = r.nextDouble() > 0.5 ? true : false;
        float posY = (float) (r.nextDouble() * (MAX_X/10.0) * (sign ? 1 : -1));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, posY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);



        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();

        i++;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void createSakuraEars2(float x, float y) {

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        params.topMargin = (int) y - (SIZE / 2);

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

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * fspeedx1) + 8000) / (speedx1/5.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * fspeedy1) + 8000) / (speedy1/5.0));

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * fspeed1) + 2000) / (speed1/7.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, 0 - x - SIZE);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);
        translationX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sakura.setLayerType(View.LAYER_TYPE_NONE, null);

                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
                animatorSet.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        sign = r.nextDouble() > 0.5 ? true : false;
        float posY = (float) (r.nextDouble() * (MAX_X/10.0) * (sign ? 1 : -1));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, posY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);



        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();

        i++;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void createSakuraEyes1(float x, float y) {

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        params.topMargin = (int) y - (SIZE / 2);

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

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * fspeedx1) + 8000) / (speedx1/5.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * fspeedy1) + 8000) / (speedy1/5.0));

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * fspeed1) + 2000) / (speed1/5.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, MAX_X - x + SIZE);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);
        translationX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sakura.setLayerType(View.LAYER_TYPE_NONE, null);

                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
                animatorSet.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        sign = r.nextDouble() > 0.5 ? true : false;
        float posY = (float) (r.nextDouble() * (MAX_X/5.0)) + 200f;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, -posY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);


        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();

        i++;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void createSakuraEyes2(float x, float y) {

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        params.topMargin = (int) y - (SIZE / 2);

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

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * fspeedx1) + 8000) / (speedx1/5.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * fspeedy1) + 8000) / (speedy1/5.0));

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * fspeed1) + 2000) / (speed1/5.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, 0 - x - SIZE);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);
        translationX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                sakura.setLayerType(View.LAYER_TYPE_NONE, null);

                sakura.clearAnimation();
                sakura.setVisibility(View.GONE);
                layoutSakura.removeView(sakura);
                animatorSet.cancel();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        sign = r.nextDouble() > 0.5 ? true : false;
        float posY = (float) (r.nextDouble() * (MAX_X/5.0)) + 200f;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, -posY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);


        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();

        i++;
    }


}
