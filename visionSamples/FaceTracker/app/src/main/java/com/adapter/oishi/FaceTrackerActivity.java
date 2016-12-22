/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adapter.oishi;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.adapter.oishi.ui.camera.CameraSourcePreview;
import com.adapter.oishi.ui.camera.GraphicOverlay;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {

    private OishiApplication app;

    private ClonableRelativeLayout topLayout;
    private RelativeLayout layoutSakura;
    private FrameLayout layoutCheek;
    private FrameLayout layoutLight;

    private ToggleButton toggleButtonRecord;

    private Dialog dialogOs;

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        app = (OishiApplication) getApplicationContext();
        app.getHttpService().sendStat(HTTPService.SAVERESULT);
        app.sendPageStat("home");
        app.getHttpService().sendStat(HTTPService.OPENAPP);

        faces = new SparseArray<com.adapter.oishi.Face>();

        Singleton.activity = this;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        MAX_X = size.x;
        MAX_Y = size.y;
        int SIZE = size.x/10;
        TRIANGLE = (int) Math.sqrt((MAX_X * MAX_X + MAX_Y * MAX_Y)) + SIZE / 2;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        dialogOs = new Dialog(FaceTrackerActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialogOs.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogOs.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogOs.setContentView(R.layout.dialog);
        dialogOs.setCancelable(true);
        ((ImageView) dialogOs.findViewById(R.id.imageView)).setImageResource(R.drawable.bg_popup_os_warning);
        ((ImageView) dialogOs.findViewById(R.id.imageView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogOs.dismiss();
            }
        });


        topLayout = (ClonableRelativeLayout) findViewById(R.id.topLayout);
        layoutSakura = (RelativeLayout) findViewById(R.id.layoutSakura);
        layoutCheek = (FrameLayout) findViewById(R.id.layoutCheek);
        layoutLight = (FrameLayout) findViewById(R.id.layoutLight);

        toggleButtonRecord = (ToggleButton) findViewById(R.id.toggleButtonRecord);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);


        selector = (LinearLayout) findViewById(R.id.selector);

        imageViewSwapCamera = (ImageView) findViewById(R.id.imageViewSwapCamera);
        imageViewSwapCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (hasPermissions) {
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        faces.get(key).waitForStop = true;
                    }

                    toggleButtonEars.setChecked(false);
                    toggleButtonEyes.setChecked(false);
                    toggleButtonMouth.setChecked(false);

                    Random r = new Random();
                    double d = r.nextDouble();
                    if (d < 0.25) {
                        toggleButtonEyes.setChecked(true);
                    } else if (d < 0.50) {
                        toggleButtonMouth.setChecked(true);
                    } else if (d < 0.75) {
                        toggleButtonEars.setChecked(true);
                    } else {
                        toggleButtonFace .setChecked(true);
                    }

                    if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
                        CAMERA_FACING = CameraSource.CAMERA_FACING_BACK;
                    } else {
                        CAMERA_FACING = CameraSource.CAMERA_FACING_FRONT;
                    }

                    mPreview.stop();
                    if (mCameraSource != null) {
                        mCameraSource.release();
                    }

                    createCameraSource();
                    startCameraSource();
                    mPreview.requestLayout();
                }
            }
        });



        toggleButtonEars = (ToggleButton) findViewById(R.id.toggleButtonEars);
        toggleButtonEars.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (toggleButtonEars.isChecked() && !toggleButtonEyes.isChecked() && !toggleButtonMouth.isChecked() && !toggleButtonFace.isChecked()) {
                    return true;
                }
                return false;
            }
        });
        toggleButtonEars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    where = "ear";

                    toggleButtonEyes.setChecked(false);
                    toggleButtonMouth.setChecked(false);
                    toggleButtonFace.setChecked(false);
                }
            }
        });

        toggleButtonEyes = (ToggleButton) findViewById(R.id.toggleButtonEyes);
        toggleButtonEyes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!toggleButtonEars.isChecked() && toggleButtonEyes.isChecked() && !toggleButtonMouth.isChecked() && !toggleButtonFace.isChecked()) {
                    return true;
                }
                return false;
            }
        });
        toggleButtonEyes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    where = "eye";

                    toggleButtonEars.setChecked(false);
                    toggleButtonMouth.setChecked(false);
                    toggleButtonFace.setChecked(false);
                }
            }
        });

        toggleButtonMouth = (ToggleButton) findViewById(R.id.toggleButtonMouth);
        toggleButtonMouth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!toggleButtonEars.isChecked() && !toggleButtonEyes.isChecked() && toggleButtonMouth.isChecked() && !toggleButtonFace.isChecked()) {
                    return true;
                }
                return false;
            }
        });
        toggleButtonMouth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    where = "mouth";

                    toggleButtonEars.setChecked(false);
                    toggleButtonEyes.setChecked(false);
                    toggleButtonFace.setChecked(false);
                }
            }
        });

        toggleButtonFace = (ToggleButton) findViewById(R.id.toggleButtonFace);
        toggleButtonFace.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!toggleButtonEars.isChecked() && !toggleButtonEyes.isChecked() && !toggleButtonMouth.isChecked() && toggleButtonFace.isChecked()) {
                    return true;
                }
                return false;
            }
        });
        toggleButtonFace.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    where = "face";
                    toggleButtonEars.setChecked(false);
                    toggleButtonEyes.setChecked(false);
                    toggleButtonMouth.setChecked(false);
                }
            }
        });

        final Dialog dialogNoInternet = new Dialog(FaceTrackerActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialogNoInternet.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNoInternet.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogNoInternet.setContentView(R.layout.dialog_no_internet);
        dialogNoInternet.setCancelable(false);

        ((ImageView) dialogNoInternet.findViewById(R.id.imageViewClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNoInternet.dismiss();
            }
        });


        toggleButtonRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    dialogOs.show();
                    return true;
                }
                else {
                    if (!app.isNetworkConnected()) {

                        if (!dialogNoInternet.isShowing()) {
                            dialogNoInternet.show();
                        }

                        return true;
                    } else {
                        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

                        if (ContextCompat.checkSelfPermission(FaceTrackerActivity.this, Manifest.permission.CAMERA) +
                                ContextCompat.checkSelfPermission(FaceTrackerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                                ContextCompat.checkSelfPermission(FaceTrackerActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                            if (ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, Manifest.permission.CAMERA) ||
                                    ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                                    ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, Manifest.permission.RECORD_AUDIO)) {
//                        toggleButtonRecord.setChecked(false);

                                waitingForResult = true;
                                ActivityCompat.requestPermissions(FaceTrackerActivity.this, permissions, REQUEST_PERMISSIONS);
                                return true;
                            } else {
                                waitingForResult = true;
                                ActivityCompat.requestPermissions(FaceTrackerActivity.this, permissions, REQUEST_PERMISSIONS);
                                return true;
                            }
                        }

                        return false;
                    }
                }
            }
        });
        toggleButtonRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                onToggleScreenShare(compoundButton);

                if (b) {
                    selector.setVisibility(View.INVISIBLE);
                }
                else {
                    selector.setVisibility(View.VISIBLE);
                }


            }
        });

        // mediaProjection

        mMediaRecorder = new MediaRecorder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }



        // permission
        if (getIntent().getBooleanExtra("pass", true)) {
            checkCameraSupportSize();
            hasPermissions = true;

            createCameraSource();
            initRecorder();

            makeThreadSakura();
        }



        finishDialog = new Dialog(FaceTrackerActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        finishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        finishDialog.setContentView(R.layout.dialog_finish_record);
        finishDialog.setCancelable(false);

    }

    private boolean recording = false;
    private boolean cool = false;

    private static SparseArray<com.adapter.oishi.Face> faces;

    public SparseArray<com.adapter.oishi.Face> getFaces() {
        return faces;
    }

    public void addFace(com.adapter.oishi.Face face) {
        face.initSound(getApplicationContext());
        faces.append(face.id, face);


        Log.d("remover", "add " + face.id + " " + SystemClock.currentThreadTimeMillis());

        if (!recording) {
            Random r = new Random();
            if (r.nextBoolean()) {
                cool = !cool;

                if (cool) {
                    ObjectAnimator.ofFloat((ImageView) findViewById(R.id.imageFrameIce1), View.ALPHA, 0f, 1f).setDuration(300).start();
                } else {
                    ObjectAnimator.ofFloat((ImageView) findViewById(R.id.imageFrameIce1), View.ALPHA, 1f, 0f).setDuration(300).start();
                }
            }
        }
    }

//    public void removeFace(int id) {
//        Log.d("sound", "remove face " + id);
//        if (faces.get(id) != null && faces.get(id).waitForStop) {
//            faces.get(id).waitForStop = true;
//        }
//    }


    private int volume1 = 10;
    private int speed1 = 10;
    private int facespeed1 = 14;
    private int speedx1 = 1;
    private int facespeedx1 = 3;
    private int speedy1 = 5;
    private int facespeedy1 = 10;
    private int startSize1 = 0;
    private int finalSize1 = 15;

    private int volume2 = 2;
    private int speed2 = 19;
    private int facespeed2 = 19;
    private int speedx2 = 2;
    private int facespeedx2 = 3;
    private int speedy2 = 2;
    private int facespeedy2 = 3;
    private int startSize2 = 1;
    private int finalSize2 = 17;

    //private int SIZE = 0;
    public static int MAX_X = 1080;
    public static int MAX_Y = 1920;
    public static int PREVIEW_CAM_X = 0;
    public static int PREVIEW_CAM_Y = 0;

    public static int IMAGE_WIDTH = 0;
    public static int IMAGE_HEIGHT = 0;

    private static int TRIANGLE = 0;

    private ImageView imageViewSwapCamera;
    private ToggleButton toggleButtonEars;
    private ToggleButton toggleButtonEyes;
    private ToggleButton toggleButtonMouth;
    private ToggleButton toggleButtonFace;

    private LinearLayout selector;

    public static int CAMERA_FACING = CameraSource.CAMERA_FACING_FRONT;

    private float minusX;

    private ArrayList<ImageView> cheeks = new ArrayList<ImageView>();
    private ArrayList<ImageView> lights = new ArrayList<ImageView>();
    private ArrayList<ImageView> imageViewFaces = new ArrayList<ImageView>();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void makeThreadSakura() {

        Random r = new Random();
        double d = r.nextDouble();
        if (d < 0.4) {
            toggleButtonEyes.setChecked(true);
        }
        else if (d < 0.8) {
            toggleButtonMouth.setChecked(true);
        }
        else {
            toggleButtonEars.setChecked(true);
        }

        final Handler handlerCheek = new Handler();
        handlerCheek.post(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < cheeks.size(); i++) {
                    layoutCheek.removeView(cheeks.get(i));
                    cheeks.get(i).setVisibility(View.GONE);
                    cheeks.remove(i);
                }

                for (int i = 0; i < faces.size(); i++) {
                    int key = faces.keyAt(i);
                    com.adapter.oishi.Face f = faces.get(key);

//                    if ((f.leftCheekX != -1 && f.leftCheekY != -1) &&
//                            (f.rightCheekX != -1 && f.rightCheekY != -1)) {

                        createCheek(f);
//                    }
                }

                if (appRuning) {
                    handlerCheek.postDelayed(this, 16);
                }
            }
        });

        final Handler handlerLight = new Handler();
        handlerLight.post(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < lights.size(); i++) {
                    layoutLight.removeView(lights.get(i));
                    lights.get(i).setVisibility(View.GONE);
                    lights.remove(i);
                }


                if (toggleButtonMouth.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        com.adapter.oishi.Face f = faces.get(key);

                        if ((f.bottomMouthX != -1 && f.bottomMouthY != -1) ||
                                (f.mouthX != -1 && f.mouthY != -1)) {

                            createLightMouth(f);
                        }
                    }
                }
                else if (toggleButtonEyes.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        com.adapter.oishi.Face f = faces.get(key);

                        if (f.leftEyeX != -1 && f.leftEyeY != -1) {
                            createLightEyesLeft(f);
                        }

                        if (f.rightEyeX != -1 && f.rightEyeY != -1) {
                            createLightEyesRight(f);
                        }
                    }
                }
                else if (toggleButtonEars.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        com.adapter.oishi.Face f = faces.get(key);

                        // left ear
                        if (f.leftEarX != -1 && f.leftEarY != -1) {
                            createLightEarsLeft(f, 2);
                        }
                        else if ((f.rightEarX != -1 && f.rightEarY != -1) &&
                                (f.rightEyeX != -1 && f.rightEyeY != -1)) {
                            createLightEarsLeft(f, 2);
                        }
                        else if ((f.leftEyeX != -1 && f.leftEyeY != -1) &&
                                (f.rightEyeX != -1 && f.rightEyeY != -1)) {
                            createLightEarsLeft(f, 2);
                        }

                        // right ear
                        if (f.rightEarX != -1 && f.rightEarY != -1) {
                            createLightEarsRight(f, 2); //0
                        }
                        else if ((f.leftEarX != -1 && f.leftEarY != -1) &&
                                (f.leftEyeX != -1 && f.leftEyeY != -1)) {
                            createLightEarsRight(f, 2); //1
                        }
                        else if ((f.rightEyeX != -1 && f.rightEyeY != -1) &&
                                (f.leftEyeX != -1 && f.leftEyeY != -1)) {
                            createLightEarsRight(f, 2); //2
                        }
                    }
                }

                if (appRuning) {
                    handlerLight.postDelayed(this, 16);
                }
            }
        });


        final Handler handlerRemover = new Handler();
        handlerRemover.post(new Runnable() {
            @Override
            public void run() {

                Log.d("remover", faces.size() + "");

                // prevent handler not accurate
                if (SystemClock.currentThreadTimeMillis() - lastChecking < CHECKING_DELAY) {

                }
                else {
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        com.adapter.oishi.Face f = faces.get(key);

                        f.count++;
                        Log.d("remover", f.id + " f++ " + f.count + " " + lastChecking);
                        if (f.count > 4) {
                            faces.get(f.id).waitForStop = true;
                        }

                        if (f.waitForStop) {
                            Log.d("remover", f.id + " " + faces.size());
                            if (f.isPlayingSound()) {
                                f.stopSound();
                            }

                            faces.remove(f.id);
                            Log.d("remover", f.id + " " + faces.size());

                            if (!recording && faces.size() == 0) {
                                cool = false;
                                ((ImageView) findViewById(R.id.imageFrameIce1)).setAlpha(0f);
                            }
                        }

                        if (!toggleButtonEyes.isChecked() && !toggleButtonEars.isChecked() && !toggleButtonMouth.isChecked() && !toggleButtonFace.isChecked()) {
                            try {
                                if (f.isPlayingSound()) {
                                    f.pauseSound();
                                }
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    lastChecking = SystemClock.currentThreadTimeMillis();
                }

                if (appRuning) {
                    handlerRemover.postDelayed(this, CHECKING_DELAY);
                }
            }
        });

        final Handler handlerFace = new Handler();
        handlerFace.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < imageViewFaces.size(); i++) {
                    layoutCheek.removeView(imageViewFaces.get(i));
                    imageViewFaces.get(i).setVisibility(View.GONE);
                    imageViewFaces.remove(i);

                }

                if (toggleButtonFace.isChecked()) {

                    ArrayList<com.adapter.oishi.Face> reorderFaces = new ArrayList<com.adapter.oishi.Face>();
                    for (int i = 0; i < faces.size(); i++) {
                        reorderFaces.add(faces.get(faces.keyAt(i)));
                    }
                    Collections.sort(reorderFaces);

                    for (int i = 0; i < reorderFaces.size(); i++) {
                        com.adapter.oishi.Face f = reorderFaces.get(i);

                        if (f.left != -1 && f.right != -1 && f.top != -1 && f.bottom != -1) {
                            createFace(f);

                            if (!f.waitForStop && !f.isPlayingSound()) {
                                f.playSound();
                            }

                        }
                    }
                }
                if (appRuning) {
                    handlerFace.postDelayed(this, 16);
                }
            }
        });

        final Handler handlerSakuraMouth = new Handler();
        handlerSakuraMouth.post(new Runnable() {
            @Override
            public void run() {

                if (toggleButtonMouth.isChecked()) {
                    Log.d("handlerSakuraMouth", faces.toString());
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        com.adapter.oishi.Face f = faces.get(key);

                        if ((f.bottomMouthX != -1 && f.bottomMouthY != -1) ||
                                (f.mouthX != -1 && f.mouthY != -1)) {
                            createSakuraMouth(f);
                            createSakuraMouth(f);

                            if (!f.waitForStop && !f.isPlayingSound()) {
                                f.playSound();
                            }

                        }
                    }
                }
                if (appRuning) {
                    handlerSakuraMouth.postDelayed(this, 16);
                }
            }
        });

        final Handler handlerSakuraEyes = new Handler();
        handlerSakuraEyes.post(new Runnable() {
            @Override
            public void run() {

                if (toggleButtonEyes.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        com.adapter.oishi.Face f = faces.get(key);
                        int sakuring = 0;

                        if (f.leftEyeX != -1 && f.leftEyeY != -1) {
                            createSakuraEyesLeft(f);
                            createSakuraEyesLeft(f);
                            createSakuraEyesLeft(f);
                            sakuring++;
                        }

                        if (f.rightEyeX != -1 && f.rightEyeY != -1) {
                            createSakuraEyesRight(f);
                            createSakuraEyesRight(f);
                            createSakuraEyesRight(f);
                            sakuring++;
                        }

                        if (sakuring >= 0 && !f.waitForStop && !f.isPlayingSound()) {
                            f.playSound();
                        }

                    }
                }
                if (appRuning) {
                    handlerSakuraEyes.postDelayed(this, 16);
                }
            }
        });

        final Handler handlerSakuraEars = new Handler();
        handlerSakuraEars.post(new Runnable() {
            @Override
            public void run() {


                if (toggleButtonEars.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        int key = faces.keyAt(i);
                        com.adapter.oishi.Face f = faces.get(key);
                        int sakuring = 0;

                        // left ear
                        if (f.leftEarX != -1 && f.leftEarY != -1) {
                            createSakuraEarsLeft(f, 2);
                            createSakuraEarsLeft(f, 2);
                            sakuring++;
                        }
                        else if ((f.rightEarX != -1 && f.rightEarY != -1) &&
                                (f.rightEyeX != -1 && f.rightEyeY != -1)) {
                            createSakuraEarsLeft(f, 2);
                            createSakuraEarsLeft(f, 2);
                            sakuring++;
                        }
                        else if ((f.leftEyeX != -1 && f.leftEyeY != -1) &&
                                (f.rightEyeX != -1 && f.rightEyeY != -1)) {
                            createSakuraEarsLeft(f, 2);
                            createSakuraEarsLeft(f, 2);
                            sakuring++;
                        }

                        // right ear
                        if (f.rightEarX != -1 && f.rightEarY != -1) {
                            createSakuraEarsRight(f, 2);
                            createSakuraEarsRight(f, 2);
                            sakuring++;
                        }
                        else if ((f.leftEarX != -1 && f.leftEarY != -1) &&
                                (f.leftEyeX != -1 && f.leftEyeY != -1)) {
                            createSakuraEarsRight(f, 2);
                            createSakuraEarsRight(f, 2);
                            sakuring++;
                        }
                        else if ((f.rightEyeX != -1 && f.rightEyeY != -1) &&
                                (f.leftEyeX != -1 && f.leftEyeY != -1)) {
                            createSakuraEarsRight(f, 2);
                            createSakuraEarsRight(f, 2);
                            sakuring++;
                        }

                        if (sakuring >= 0 && !f.waitForStop && !f.isPlayingSound()) {
                            f.playSound();
                        }

                    }
                }

                if (appRuning) {
                    handlerSakuraEars.postDelayed(this, 16);
                }
            }
        });

    }
    private static long lastChecking = 0;
    private static final int CHECKING_DELAY = 30;


    private void createCameraSource() {
        Log.d("c511", "createCameraSource");

        float minFaceSize = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? 0.4f : 0.4f;

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setTrackingEnabled(true)
                .setMinFaceSize(minFaceSize)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());


        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

//        int pW = MAX_X;
//        int pH = MAX_Y;
//        if (MAX_Y / MAX_X * 1.0 == 4 / 3.0) {
//            pW = 480;
//            pH = 640;
//        }
        int w = 0;
        int h = 0;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            w = Config.getInt(getApplicationContext(), Config.wFront);
            h = Config.getInt(getApplicationContext(), Config.hFront);
        }
        else {
            w = Config.getInt(getApplicationContext(), Config.wBack);
            h = Config.getInt(getApplicationContext(), Config.hBack);
        }
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(h, w)
                .setFacing(CAMERA_FACING)
                .setAutoFocusEnabled(true)
                .setRequestedFps(60f)
                .build();


    }


    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();

        appRuning = true;
    }

    boolean appRuning = true;
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();


        for (int i = 0; i < faces.size(); i++) {
            int key = faces.keyAt(i);
            faces.get(key).waitForStop = true;
            faces.get(key).stopSound();
        }

        if (!waitingForResult) {
            appRuning = false;
            recording = false;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }

        destroyMediaProjection();
    }


    private void startCameraSource() {
        Log.d("surfacee", "startCameraSource");

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
//                Log.d("ppw", mCameraSource.getPreviewSize().toString());
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {

        private int faceId;
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onNewItem(int faceId, Face item) {
            Log.d("fffa", "new  " + faceId);

            this.faceId = faceId;

            mFaceGraphic.setId(faceId);
        }

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        @Override
        public void onDone() {
            Log.d("sound", "done " + faceId);

            com.adapter.oishi.Face f = faces.get(faceId);
            if (f != null) {
                f.waitForStop = true;
            }

            mOverlay.remove(mFaceGraphic);
        }
    }


    private static final int[][] cheek = {
            {R.drawable.cheek1_left, R.drawable.cheek1_right},
            {R.drawable.cheek2_left, R.drawable.cheek2_right},
            {R.drawable.cheek3_left, R.drawable.cheek3_right},
    };

    int[] sa = {
            R.drawable.sakura_white1,
            R.drawable.sakura_white2,
            R.drawable.sakura_pink1,
            R.drawable.sakura_pink2,
            R.drawable.sakura3,
            R.drawable.sakura4,
            R.drawable.sakura5,
    };

    private void createFace(com.adapter.oishi.Face face) {

        float x = (face.left + face.right) / 2;
        float y = (face.top + face.bottom) / 2;

        Log.d("faceq", face.left + " " + face.right + " " + face.top + " " + face.bottom);

        int sizeX = (int) (Math.abs(face.left - face.right) * 1.8);
        int sizeY = (int) Math.abs(face.top - face.bottom) * 2;

        FrameLayout.LayoutParams paramsLeft = new FrameLayout.LayoutParams(sizeX, sizeY);
        paramsLeft.leftMargin = (int) (x - (sizeX / 2));
        paramsLeft.topMargin = (int) (y - (sizeY / 2));

        ImageView imageViewFace = new ImageView(getApplicationContext());
        imageViewFace.setImageResource(R.drawable.img_face_sakura);
        imageViewFace.setRotation(face.eulerZ);
        imageViewFace.setRotationY(-1 * face.eulerY);

        imageViewFace.setLayoutParams(paramsLeft);
        layoutCheek.addView(imageViewFace);
        imageViewFaces.add(imageViewFace);
    }

    private void createCheek(com.adapter.oishi.Face face) {

        float mountX = face.mouthX != -1 ? face.mouthX : face.bottomMouthX;
        float mountY = face.mouthY != -1 ? face.mouthY : face.bottomMouthY;

        int size = (int) (face.getSakuraSize() * 0.85);

        if (mountX != -1 && mountY != -1 && face.leftEyeX != -1 && face.leftEyeY != -1) {
            int x1 = (int) (face.leftEyeX + ((face.leftEyeX - mountX)) / 2);
            int y1 = (int) ((face.leftEyeY + mountY) / 2);

            FrameLayout.LayoutParams paramsLeft = new FrameLayout.LayoutParams(size, size);
            paramsLeft.leftMargin = x1;
            if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
                paramsLeft.leftMargin = MAX_X - paramsLeft.leftMargin;
            }
            paramsLeft.leftMargin -= (size / 2);
            paramsLeft.topMargin = (int) y1 - (size / 2);


            ImageView cheekLeft = new ImageView(getApplicationContext());
            cheekLeft.setImageResource(cheek[face.id % 3][0]);
            cheekLeft.setAlpha(0.4f);
//        cheekLeft.setRotation(face.eulerY * 2 + face.eulerZ * 2);

            // TODO do (check) inverse for BACK CAM
            cheekLeft.setLayoutParams(paramsLeft);
            layoutCheek.addView(cheekLeft);
            cheeks.add(cheekLeft);
        }


        if (mountX != -1 && mountY != -1 && face.rightEyeX != -1 && face.rightEyeY != -1) {
            int x2 = (int) (face.rightEyeX - ((mountX - face.rightEyeX)) / 2);
            int y2 = (int) ((face.rightEyeY + mountY) / 2);

            FrameLayout.LayoutParams paramsRight = new FrameLayout.LayoutParams(size, size);
            paramsRight.leftMargin = (int) x2;
            if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
                paramsRight.leftMargin = MAX_X - paramsRight.leftMargin;
            }
            paramsRight.leftMargin -= (size / 2);
            paramsRight.topMargin = (int) y2 - (size / 2);

            ImageView cheekRight = new ImageView(getApplicationContext());
            cheekRight.setImageResource(cheek[face.id % 3][1]);
            cheekRight.setAlpha(0.4f);
//        cheekRight.setRotation(face.eulerY * 2 + face.eulerZ * 2);

            // TODO do (check) inverse for BACK CAM
            cheekRight.setLayoutParams(paramsRight);
            layoutCheek.addView(cheekRight);
            cheeks.add(cheekRight);
        }
    }

    private void createLightMouth(com.adapter.oishi.Face face) {

        float x = 0;
        float y = 0;

        if (face.bottomMouthX != -1 && face.bottomMouthY != -1) {
            x = face.bottomMouthX;
            y = face.bottomMouthY;
        }
        else {
            x = face.mouthX;
            y = face.mouthY;
        }

        final int sizeX = MAX_X;
        final int sizeY = MAX_Y;

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(cool ? R.drawable.light_blue: R.drawable.light_pink);
        light.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeX, sizeY);
        params.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = MAX_X - params.leftMargin;
        }
        params.leftMargin -= (sizeX / 2);
        params.topMargin = (int) y;

        // TODO do (check) inverse for BACK CAM

        float ang = face.eulerY * 2 + face.eulerZ * 2;
        float leftX = ang > 0 ? x : MAX_X - x;
        float leftY = y;
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.sin(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.cos(Math.toRadians(ang)) * triangle;

        light.setPivotX(sizeX / 2);
        light.setPivotY(0);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }
        else {
            light.setRotation(((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }


        light.setAlpha(0.8f);

        light.setLayoutParams(params);
        layoutLight.addView(light);
        lights.add(light);
    }

    private void createLightEyesLeft(com.adapter.oishi.Face face) {

        float x = face.leftEyeX;
        float y = face.leftEyeY;

        final int sizeX = MAX_X;
        final int sizeY = (int) (MAX_Y);

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(cool ? R.drawable.light_blue: R.drawable.light_pink);
        light.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeX, sizeY);
        params.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = MAX_X - params.leftMargin;
        }
        params.leftMargin -= (sizeX / 2);
        params.topMargin = (int) y;

        // TODO do (check) inverse for BACK CAM

        float ang = face.eulerY * 2 + face.eulerZ * 2;
        float leftX = ang > 0 ? x : MAX_X - x;
        float leftY = y;
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.sin(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.cos(Math.toRadians(ang)) * triangle;

        light.setPivotX(sizeX / 2);
        light.setPivotY(0);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }
        else {
            light.setRotation(((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }

        light.setAlpha(0.8f);

        light.setLayoutParams(params);
        layoutLight.addView(light);
        lights.add(light);
    }

    private void createLightEyesRight(com.adapter.oishi.Face face) {

        float x = face.rightEyeX;
        float y = face.rightEyeY;

        final int sizeX = MAX_X;
        final int sizeY = (int) (MAX_Y);

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(cool ? R.drawable.light_blue: R.drawable.light_pink);
        light.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeX, sizeY);
        params.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = MAX_X - params.leftMargin;
        }
        params.leftMargin -= (sizeX / 2);
        params.topMargin = (int) y;

        // TODO do (check) inverse for BACK CAM

        float ang = face.eulerY * 2 + face.eulerZ * 2;
        float leftX = ang > 0 ? x : MAX_X - x;
        float leftY = y;
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.sin(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.cos(Math.toRadians(ang)) * triangle;

        light.setPivotX(sizeX / 2);
        light.setPivotY(0);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }
        else {
            light.setRotation(((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }

        light.setAlpha(0.8f);

        light.setLayoutParams(params);
        layoutLight.addView(light);
        lights.add(light);
    }

    private void createLightEarsLeft(com.adapter.oishi.Face face, int type) {

        float x = 0;
        float y = 0;

        if (type == 0) {
            x = face.leftEarX;
            y = face.leftEarY;
        }
        else if (type == 1) {
            float eeX = face.rightEarX - face.rightEyeX;
            float eeY = face.rightEarY - face.rightEyeY;
            // TODO do euler calculate
            x = face.leftEyeX - eeX;
            y = face.leftEyeY - eeY;
        }
        else if (type == 2) {
            float eeX = (face.rightEyeX - face.leftEyeX) / 1.5f;
            x = face.leftEyeX - eeX;
            y = face.leftEyeY + eeX / 1.5f;
        }

        final int sizeX = MAX_X * 2;
        final int sizeY = MAX_Y;

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(cool ? R.drawable.light_blue: R.drawable.light_pink);
        light.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeX, sizeY);
        params.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = MAX_X - params.leftMargin;
        }
        params.leftMargin -= (sizeX / 2);
        params.topMargin = (int) y + face.getSakuraSize();

        // TODO do (check) inverse for BACK CAM


        float ang = (float) (face.eulerY + face.eulerZ);
        float leftX = x;
        float leftY = MAX_Y - y;
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.cos(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.sin(Math.toRadians(-1 * ang)) * triangle;

        light.setPivotX(sizeX / 2);
        light.setPivotY(0);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }
        else {
            light.setRotation(((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }

        light.setAlpha(0.9f);

        light.setLayoutParams(params);
        layoutLight.addView(light);
        lights.add(light);


        Log.d("anglee", "lig " + (-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90)));
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void createLightEarsRight(com.adapter.oishi.Face face, int type) {

        float x = 0;
        float y = 0;

        if (type == 0) {
            x = face.rightEarX;
            y = face.rightEarY;
        }
        else if (type == 1) {
            float eeX = face.leftEyeX - face.leftEarX;
            float eeY = face.leftEyeY - face.leftEarY;
            // TODO do euler calculate
            x = face.rightEyeX + eeX;
            y = face.rightEyeY + eeY;
        }
        else if (type == 2) {
            float eeX = (face.rightEyeX - face.leftEyeX) / 1.5f;
            x = face.rightEyeX + eeX;
            y = face.rightEyeY + eeX / 1.5f;
        }

        final int sizeX = MAX_X * 2;
        final int sizeY = MAX_Y;

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(cool ? R.drawable.light_blue: R.drawable.light_pink);
        light.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeX, sizeY);
        params.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = MAX_X - params.leftMargin;
        }
        params.leftMargin -= (sizeX / 2);
        params.topMargin = (int) y + face.getSakuraSize();

        // TODO do (check) inverse for BACK CAM



        float ang = (float) (face.eulerY + face.eulerZ);
        float leftX = MAX_X - x;
        float leftY = y;
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.cos(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.sin(Math.toRadians(ang)) * triangle;

        light.setPivotX(sizeX / 2);
        light.setPivotY(0);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            light.setRotation(((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }
        else {
            light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));
        }

        light.setAlpha(0.9f);

        light.setLayoutParams(params);
        layoutLight.addView(light);
        lights.add(light);
    }

    private int randomSakura() {
        Random r = new Random();
        int k = r.nextInt(140);

        int im = (k < 40 ? 0 :
                (k < 80 ? 1 :
                        (k < 100 ? 4 :
                                (k < 120 ? 5 : 6))));
        if (cool && (im == 0 || im == 1)) {
            im += 2;
        }

        return im;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraMouth(com.adapter.oishi.Face face) {

        float x = 0;
        float y = 0;

        if (face.bottomMouthX != -1 && face.bottomMouthY != -1) {
            x = face.bottomMouthX;
            y = face.bottomMouthY;
        }
        else {
            x = face.mouthX;
            y = face.mouthY;
        }

        Random r = new Random();
        int im = randomSakura();

        int size = (im < 4 ? face.getSakuraSize() : face.getSakuraSize() / 2);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin);
        }
        params.leftMargin -= (size / 2);

        params.topMargin = (int) y - (size / 2);


        final ImageView sakura = new ImageView(getApplicationContext());
        sakura.setImageResource(sa[im]);
        sakura.setLayoutParams(params);

        layoutSakura.addView(sakura);

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? 0 : 359;
        int endD = sign ? 359 : 0;
        long duration = (long) (r.nextDouble() * 5000) + 1500;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/10.0));
        duration *= 1.3;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;

        float ang = face.eulerY * 2 + face.eulerZ * 2;
        float leftX = ang > 0 ? x : MAX_X - x;
        float leftY = MAX_Y - y + (size / 2);
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.sin(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.cos(Math.toRadians(ang)) * triangle;

        float posX = (float) (r.nextDouble() * (MAX_X/2.5) * (sign ? 1 : -1));

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX + posX) : realX + posX;
//        duration /= 1.2;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX - (size / 2));
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, realY + (size / 2));
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));
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

        animatorSet.playTogether(rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEyesLeft(com.adapter.oishi.Face face) {

        float x = face.leftEyeX;
        float y = face.leftEyeY;

        Random r = new Random();
        int im = randomSakura();

        int size = (int) (im < 4 ? face.getSakuraSize() / 1.2 : face.getSakuraSize() / 2.4);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (size / 2);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin) - size;
        }
        params.topMargin = (int) y - (size / 2);


        final ImageView sakura = new ImageView(getApplicationContext());
        sakura.setImageResource(sa[im]);
        sakura.setLayoutParams(params);

        layoutSakura.addView(sakura);

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? 0 : 359;
        int endD = sign ? 359 : 0;
        long duration = (long) (r.nextDouble() * 5000) + 2000;
        duration *= 2.7;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/5.0));
        duration *= 2.3;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        float ang = face.eulerY * 2 + face.eulerZ * 2;
        float leftX = ang > 0 ? x : MAX_X - x;
        float leftY = MAX_Y - y + (size / 2);
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.sin(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.cos(Math.toRadians(ang)) * triangle;

        float posX = (float) (r.nextDouble() * (MAX_X/2.5) * (sign ? 1 : -1));

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX + posX) : realX + posX;
//        duration /= 1.2;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX - (size / 2));
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, realY + (size / 2));
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));
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


        animatorSet.playTogether(rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEyesRight(com.adapter.oishi.Face face) {

        float x = face.rightEyeX;
        float y = face.rightEyeY;

        Random r = new Random();
        int im = randomSakura();

        int size = (int) (im < 4 ? face.getSakuraSize() / 1.2 : face.getSakuraSize() / 2.4);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (size / 2);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin) - size;
        }
        params.topMargin = (int) y - (size / 2);


        final ImageView sakura = new ImageView(getApplicationContext());
        sakura.setImageResource(sa[im]);
        sakura.setLayoutParams(params);

        layoutSakura.addView(sakura);

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? 0 : 359;
        int endD = sign ? 359 : 0;
        long duration = (long) (r.nextDouble() * 5000) + 2000;
        duration *= 2.7;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/5.0));
        duration *= 2.3;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        float triangle = (float) Math.sqrt(Math.pow(MAX_X - x, 2) + Math.pow(MAX_Y - y, 2));
        float realX = (float) Math.sin(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * triangle;
        float realY = (float) Math.cos(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * triangle;

        float posX = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX + posX) : realX + posX;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX - (size / 2));
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, realY + (size / 2));
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));
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


        animatorSet.playTogether(rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();


    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEarsLeft(com.adapter.oishi.Face face, int type) {

        float x = 0;
        float y = 0;

        if (type == 0) {
            x = face.leftEarX;
            y = face.leftEarY;
        }
        else if (type == 1) {
            float eeX = face.rightEarX - face.rightEyeX;
            float eeY = face.rightEarY - face.rightEyeY;
            // TODO do euler calculate
            x = face.leftEyeX - eeX;
            y = face.leftEyeY - eeY;
        }
        else if (type == 2) {
            float eeX = (face.rightEyeX - face.leftEyeX) / 1.5f;
            x = face.leftEyeX - eeX;
            y = face.leftEyeY + eeX / 1.5f;
        }

        Random r = new Random();
        int im = randomSakura();

        int size = (im < 4 ? face.getSakuraSize() : face.getSakuraSize() / 2);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin);
        }
        params.leftMargin -= (int) (size / 2);
        params.topMargin = (int) y - (size / 2) + size;


        final ImageView sakura = new ImageView(getApplicationContext());
        sakura.setImageResource(sa[im]);
        sakura.setLayoutParams(params);

        layoutSakura.addView(sakura);

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? 0 : 359;
        int endD = sign ? 359 : 0;
        long duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/7.0));
//        duration *= 1.2;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        float ang = (float) (face.eulerY + face.eulerZ);
        float leftX = x;
        float leftY = MAX_Y - y + (size / 2);
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.cos(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.sin(Math.toRadians(-1 * ang)) * triangle;

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX - (size / 2)) : (realX + (size / 2));

        duration *= 1.4;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
        translationX.setInterpolator(new DecelerateInterpolator());
        Log.d("sizesize" , size + "");
        translationX.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));
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
        float posY = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));

        float toY = realY + posY;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, toY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));

        animatorSet.playTogether(rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEarsRight(com.adapter.oishi.Face face, int type) {

        float x = 0;
        float y = 0;

        if (type == 0) {
            x = face.rightEarX;
            y = face.rightEarY;
        }
        else if (type == 1) {
            float eeX = face.leftEyeX - face.leftEarX;
            float eeY = face.leftEyeY - face.leftEarY;
            // TODO do euler calculate
            x = face.rightEyeX + eeX;
            y = face.rightEyeY + eeY;
        }
        else if (type == 2) {
            float eeX = (face.rightEyeX - face.leftEyeX) / 1.5f;
            x = face.rightEyeX + eeX;
            y = face.rightEyeY + eeX / 1.5f;
        }

        Random r = new Random();
        int im = randomSakura();

        int size = (im < 4 ? face.getSakuraSize() : face.getSakuraSize() / 2);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (size / 2);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin) - size;
        }
        params.topMargin = (int) y - (size / 2) + size;


        final ImageView sakura = new ImageView(getApplicationContext());
        sakura.setImageResource(sa[im]);
        sakura.setLayoutParams(params);

        layoutSakura.addView(sakura);

        sakura.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        final AnimatorSet animatorSet = new AnimatorSet();

        boolean sign = r.nextDouble() > 0.5 ? true : false;
        int startD = sign ? 0 : 359;
        int endD = sign ? 359 : 0;
        long duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/7.0));
//        duration *= 1.2;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        float ang = (float) (face.eulerY + face.eulerZ);
        float leftX = MAX_X - x + (size / 2);
        float leftY = y - (size / 2);
        float triangle = (float) Math.sqrt(Math.pow(leftX, 2) + Math.pow(leftY, 2));
        float realX = (float) Math.cos(Math.toRadians(ang)) * triangle;
        final float realY = (float) Math.sin(Math.toRadians(ang)) * triangle;

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? (realX - (size / 2)) : -1 * (realX + (size / 2));

        duration *= 1.4;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));
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
        float posY = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));

        float toY = realY + posY;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, toY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration((int) (duration * (triangle / TRIANGLE) * (100.0 / size)));



        animatorSet.playTogether(rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();
    }














    // mediaProjection http://www.truiton.com/2015/05/capture-record-android-screen-using-mediaprojection-apis/

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;

    private static final int REQUEST_CODE = 1000;
    private static final int REQUEST_PERMISSIONS = 10;

    private static final String label_permissions = "label_permissions";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int mScreenDensity;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        waitingForResult = false;

        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "ไม่สามารถบันทึกวิดีโอได้", Toast.LENGTH_SHORT).show();
            toggleButtonRecord.setChecked(false);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaProjectionCallback = new MediaProjectionCallback();
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
            mVirtualDisplay = createVirtualDisplay();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediaRecorder.start();
                }
            }, 1000);

        }

        startCountDownRecording();
    }

    private String gid;
    private String where;
    public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            app.getHttpService().saveGame(new HTTPService.OnResponseCallback<JSONObject>() {
                @Override
                public void onResponse(boolean success, Throwable error, JSONObject data) {
                    try {
                        gid = data.getString("gid");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        gid = "JSONException";
                    }

                    shareScreen();
                }
            });
        } else {
            if (recording) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                stopScreenSharing();

                Log.d("c511", "mMediaRecorder.stop mMediaRecorder.reset");

//                recording = false;
            }
        }
    }

    private Dialog finishDialog;

    private void startCountDownRecording() {
        recording = true;

        Log.d("counting", "1 " + new Date().toString());
        new Handler().postDelayed(new Runnable() {

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void run() {
                Log.d("counting", "2 " + new Date().toString());
                if (recording) {
                    // show filterLevel1
                    ObjectAnimator.ofFloat((ImageView) findViewById(R.id.imageFrameIce1), View.ALPHA , 0f, 1f).setDuration(300).start();
                    cool = true;

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            Log.d("counting", "3 " + new Date().toString());
                            if (recording) {
                                // show filterLevel2
                                ObjectAnimator.ofFloat((ImageView) findViewById(R.id.imageFrameIce2), View.ALPHA , 0f, 1f).setDuration(300).start();

                                new Handler().postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        Log.d("counting", "4 " + new Date().toString());
                                        if (recording) {
                                            // show filterLevel3
                                            ObjectAnimator.ofFloat((ImageView) findViewById(R.id.imageFrameIce3), View.ALPHA , 0f, 1f).setDuration(300).start();

                                            new Handler().postDelayed(new Runnable() {

                                                @Override
                                                public void run() {
                                                    Log.d("counting", "5 " + new Date().toString());
                                                    if (recording) {
                                                        // show endPopup
                                                        finishDialog.show();

                                                        for (int i = 0; i < faces.size(); i++) {
                                                            int key = faces.keyAt(i);
                                                            faces.get(key).pauseSound();
                                                        }

                                                        new Handler().postDelayed(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                Log.d("counting", "6 " + new Date().toString());
                                                                if (recording) {
                                                                    toggleButtonRecord.setChecked(false);

                                                                    for (int i = 0; i < faces.size(); i++) {
                                                                        int key = faces.keyAt(i);
                                                                        faces.get(key).waitForStop = true;
                                                                    }

                                                                    appRuning = false;

                                                                    new Handler().postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Log.d("counting", "7 " + new Date().toString());

                                                                            Intent intent = new Intent(getApplicationContext(), FinishRecordActivity.class);
                                                                            intent.putExtra("videoFileName", videoFileName);
                                                                            intent.putExtra("gid", gid);
                                                                            intent.putExtra("where", where);

                                                                            while (faces.size() > 0) {

                                                                            }

                                                                            startActivity(intent);

                                                                            new Handler().postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    Log.d("counting", "8 " + new Date().toString());

                                                                                    try {
                                                                                        if (finishDialog.isShowing()) {
                                                                                            finishDialog.dismiss();
                                                                                        }
                                                                                    }
                                                                                    catch (IllegalArgumentException e) {
                                                                                        e.printStackTrace();
                                                                                    }

                                                                                    finish();

                                                                                }
                                                                            }, 3000);

                                                                        }
                                                                    }, 2500);
                                                                }
                                                            }
                                                        }, 1000); // p l3
                                                    }
                                                }
                                            }, 1500);// p l3
                                        }
                                    }
                                }, 1000); // p l2
                            }
                        }
                    }, 1000);  // p l1
                }
            }
        }, 5000); // w
    }

    private boolean waitingForResult;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        app.getHttpService().sendStat(HTTPService.STARTGAME);
        app.sendPageStat("recording");

        cool = false;
        ((ImageView) findViewById(R.id.imageFrameIce1)).setAlpha(0f);

        if (mMediaProjection == null) {
            waitingForResult = true;
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);

            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();

        Log.d("c511", "shareScreen");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {

        return mMediaProjection.createVirtualDisplay("FaceTrackerActivity",
                MAX_X, MAX_Y, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private String videoFileName;

    private void initRecorder() {
        Log.d("c511", "initRecorder");
        videoFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(getFilesDir() + "/tmp/" + videoFileName);
            mMediaRecorder.setVideoSize(MAX_X, MAX_Y);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(6000000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (toggleButtonRecord.isChecked()) {
                toggleButtonRecord.setChecked(false);
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            mMediaProjection = null;
            stopScreenSharing();

            Log.d("c511", "MediaProjectionCallback onStop");
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
//        mMediaRecorder.release(); //If used: mMediaRecorder object cannot be reused again
//        mMediaRecorder = null;
        Log.d("c511", "MediaProjectionCallback stopScreenSharing");
//        destroyMediaProjection();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.d("c511", "MediaProjectionCallback stopScreenSharing");
    }

    private boolean hasPermissions = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        waitingForResult = false;
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] + grantResults[2]) == PackageManager.PERMISSION_GRANTED) {

                    checkCameraSupportSize();

                    hasPermissions = true;

                    createCameraSource();
                    initRecorder();

                    mPreview.requestLayout();

                    makeThreadSakura();

                    onToggleScreenShare(toggleButtonRecord);
                } else {
                    toggleButtonRecord.setChecked(false);

                }
                return;
            }

        }

    }

    private void checkCameraSupportSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float x = size.x;
        float y = size.y;

        float width = 0;
        float height = 0;

        float width640 = -1;
        float height640 = -1;

//        if (Config.getInt(getApplicationContext(), Config.wFront) == -1) {
            try {
                Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
                for (int i = 0; i < sizes.size(); i++) {
                    float w = sizes.get(i).width;
                    float h = sizes.get(i).height;
                    if (w > h) {
                        float t = h;
                        h = w;
                        w = t;
                    }
                    Log.d("Camera.Size", "front " + w + " " + h);
                    if (w <= x && h <= y && (h / w == y / x)) {
//                    if ((h / w == y / x)) {

                        if (height == 0) {
                            width = w;
                            height = h;
                        }

                        if (h < 640 && h >=480) {
                            width640 = w;
                            height640 = h;
                            break;
                        }
                    }
                }
                camera.release();
                camera = null;
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }

            if (width640 != -1) {
                Config.setInt(getApplicationContext(), Config.wFront, (int) width640);
                Config.setInt(getApplicationContext(), Config.hFront, (int) height640);
            } else {
                Config.setInt(getApplicationContext(), Config.wFront, (int) (width == 0 ? x : width));
                Config.setInt(getApplicationContext(), Config.hFront, (int) (height == 0 ? y : height));
            }

        width = 0;
        height = 0;

        width640 = -1;
        height640 = -1;

            try {
                Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
                for (int i = 0; i < sizes.size(); i++) {
                    float w = sizes.get(i).width;
                    float h = sizes.get(i).height;
                    if (w > h) {
                        float t = h;
                        h = w;
                        w = t;
                    }

                    Log.d("Camera.Size", "back " + w + " " + h);
                    if (w <= x && h <= y && (h / w == y / x)) {
//                    if ((h / w == y / x)) {

                        if (height == 0) {
                            width = w;
                            height = h;
                        }

                        if (h < 640 && h >=480) {
                            width640 = w;
                            height640 = h;
                            break;
                        }
                    }
                }
                camera.release();
                camera = null;
            }
            catch (RuntimeException e) {
                e.printStackTrace();
            }

            if (width640 != -1) {
                Config.setInt(getApplicationContext(), Config.wBack, (int) width640);
                Config.setInt(getApplicationContext(), Config.hBack, (int) height640);
            } else {
                Config.setInt(getApplicationContext(), Config.wBack, (int) (width == 0 ? x : width));
                Config.setInt(getApplicationContext(), Config.hBack, (int) (height == 0 ? y : height));
            }

            String log = x + " " + y + " " + Config.getInt(getApplicationContext(), Config.wFront) + " " +
                    Config.getInt(getApplicationContext(), Config.hFront) + " " +
                    Config.getInt(getApplicationContext(), Config.wBack) + " " +
                    Config.getInt(getApplicationContext(), Config.hBack);

            Log.d("Camera.Size", log);

            Answers.getInstance().logCustom(new CustomEvent("Camera Support Size")
                    .putCustomAttribute("Device", HTTPService._UA + " " + log)
            );

//        }

        File dir = new File(getFilesDir().getAbsolutePath() + "/tmp/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (recording) {

        }
        else {
            super.onBackPressed();
        }
    }
}
