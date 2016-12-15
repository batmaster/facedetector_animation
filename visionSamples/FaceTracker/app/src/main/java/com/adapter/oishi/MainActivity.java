package com.adapter.oishi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private FrameLayout activity_main;

    private ImageView ro;
    private int angle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        activity_main = (FrameLayout) findViewById(R.id.activity_main);

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(R.drawable.light_pink);
        light.setScaleType(ImageView.ScaleType.FIT_XY);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1500, 1500);
//        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) 0;
        params.topMargin = (int) 200;


        // TODO do (check) inverse for BACK CAM


        light.setPivotX(100);
        light.setPivotY(0);
//        light.setRotation(15);

        light.setLayoutParams(params);


        activity_main.addView(light);

//        light.post(new Runnable() {
//            @Override
//            public void run() {
//
//                final Handler h = new Handler();
//                h.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        angle += 15;
//
//                        light.setRotation(angle);
//
//                        h.postDelayed(this, 50);
//                    }
//                }, 50);
//            }
//        });

//        ro = (ImageView) findViewById(R.id.ro);
//        ro.post(new Runnable() {
//            @Override
//            public void run() {
//
//                ro.setPivotY(0);
//
//                final Handler h = new Handler();
//                h.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        angle += 1;
//
//                        ro.setRotation(angle);
//
//                        h.postDelayed(this, 50);
//                    }
//                }, 50);
//
//            }
//        });



    }
}
