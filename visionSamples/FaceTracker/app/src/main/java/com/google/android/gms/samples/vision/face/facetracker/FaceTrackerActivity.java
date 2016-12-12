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
package com.google.android.gms.samples.vision.face.facetracker;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {

    private ClonableRelativeLayout topLayout;
    private RelativeLayout layoutSakura;
    private FrameLayout layoutUnderSakura;
    private CanvasView canvasView;

    private ToggleButton toggleButtonRecord;

    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        faces = new ArrayList<com.google.android.gms.samples.vision.face.facetracker.Face>();

        Singleton.activity = this;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        MAX_X = size.x;
        MAX_Y = size.y;
        int SIZE = size.x/10;
        TRIANGLE = (int) Math.sqrt((MAX_X * MAX_X + MAX_Y * MAX_Y)) + SIZE / 2;

        PREVIEW_CAM_X = (int)(MAX_X);
        PREVIEW_CAM_Y = (int)(MAX_Y);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;


        topLayout = (ClonableRelativeLayout) findViewById(R.id.topLayout);
        layoutSakura = (RelativeLayout) findViewById(R.id.layoutSakura);
        layoutUnderSakura = (FrameLayout) findViewById(R.id.layoutUnderSakura);
        canvasView = (CanvasView) findViewById(R.id.canvasView);

        toggleButtonRecord = (ToggleButton) findViewById(R.id.toggleButtonRecord);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mPreview.post(new Runnable() {
            @Override
            public void run() {
                Log.d("mPreview", mPreview.getWidth() + " " + mPreview.getHeight());
            }
        });
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }



        makeThreadSakura();



        // mediaProjection

        mMediaRecorder = new MediaRecorder();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private static ArrayList<com.google.android.gms.samples.vision.face.facetracker.Face> faces;

    public ArrayList<com.google.android.gms.samples.vision.face.facetracker.Face> getFaces() {
        return faces;
    }

    public void addFace(com.google.android.gms.samples.vision.face.facetracker.Face face) {
        face.initSound(getApplicationContext());
        faces.add(face);
    }

    public void removeFace(int index) {
        faces.get(index).stopSound();
        faces.remove(index);
        canvasView.clear();
    }


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
    private static int MAX_X = 1080;
    private static int MAX_Y = 1920;
    public static int PREVIEW_CAM_X = 0;
    public static int PREVIEW_CAM_Y = 0;
    private static int TRIANGLE = 0;

    private ToggleButton toggleButtonEars;
    private ToggleButton toggleButtonEyes;
    private ToggleButton toggleButtonMouth;

    public static int CAMERA_FACING = CameraSource.CAMERA_FACING_FRONT;

    private ArrayList<ImageView> cheeks = new ArrayList<ImageView>();
    private ArrayList<ImageView> lights = new ArrayList<ImageView>();
    private ArrayList<ImageView> sparks = new ArrayList<ImageView>();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void makeThreadSakura() {


        toggleButtonEars = (ToggleButton) findViewById(R.id.toggleButtonEars);
        toggleButtonEyes = (ToggleButton) findViewById(R.id.toggleButtonEyes);
        toggleButtonMouth = (ToggleButton) findViewById(R.id.toggleButtonMouth);

//        final Handler mHandler = new Handler();
//        mHandler.post(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
//            @Override
//            public void run() {
//
//                for (int i = 0; i < faces.size(); i++) {
//                    if (faces.get(i).mouthX != -1 && faces.get(i).mouthY != -1) {
//                        createSakuraMouth(faces.get(i));
//                        createSakuraMouth(faces.get(i));
//
//                        createSakura2(faces.get(i));
//                    }
//
//                    if (faces.get(i).leftEarX != -1 && faces.get(i).leftEarY != -1) {
//                        createSakuraEarsLeft(faces.get(i));
//                    }
//
//                    if (faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) {
//                        createSakuraEarsRight(faces.get(i));
//                    }
//
//                    if (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) {
//                        createSakuraEyesLeft(faces.get(i));
//                    }
//
//                    if (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1) {
//                        createSakuraEyesRight(faces.get(i));
//                    }
//
//                    Log.d("facesss", faces.get(i).toString());
//
//
//
//
//                    faces.get(i).count++;
//                    if (faces.get(i).count == 2)
//                        faces.remove(i);
//                }
//
//                Log.d("facesss", faces.size() + "");
//
//
//                mHandler.postDelayed(this, (500 / volume1));
//            }
//        });

        toggleButtonRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (ContextCompat.checkSelfPermission(FaceTrackerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                        ContextCompat.checkSelfPermission(FaceTrackerActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(FaceTrackerActivity.this, Manifest.permission.RECORD_AUDIO)) {
                        toggleButtonRecord.setChecked(false);

                        Snackbar.make(findViewById(android.R.id.content), label_permissions,
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ActivityCompat.requestPermissions(FaceTrackerActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSIONS);
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(FaceTrackerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSIONS);
                    }
                } else {
                    onToggleScreenShare(compoundButton);
                }


//                if (b) {
//                    cloning = true;
//
//                    final Handler mHandler0 = new Handler();
//                    mHandler0.post(new Runnable() {
//                        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
//                        @Override
//                        public void run() {
//                            if (cloning) {
//                                views.add(FaceTrackerActivity.this.clone());
//                                Log.d("viewss", views.size() + " " + new Date());
//
//                                mHandler0.postDelayed(this, 1000 / 25);
//                            }
//                        }
//                    });
//                }
//                else {
//                    cloning = false;
//
//                    for (int i = 0; i < views.size(); i++) {
//
//                        final int finalI = i;
//                        new Handler().post(new Runnable() {
//                            @Override
//                            public void run() {
//                                FaceTrackerActivity activity = (FaceTrackerActivity) views.get(finalI);
//                                getBitmapFromView(activity.topLayout);
//                            }
//                        });
//                    }
//                }
            }
        });




        final Handler handlerCheek = new Handler();
        handlerCheek.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                for (int i = 0; i < cheeks.size(); i++) {
                    layoutSakura.removeView(cheeks.get(i));
                    cheeks.get(i).setVisibility(View.GONE);
                    cheeks.remove(i);
                }

                for (int i = 0; i < faces.size(); i++) {
                    if ((faces.get(i).leftCheekX != -1 && faces.get(i).leftCheekY != -1) &&
                            (faces.get(i).rightCheekX != -1 && faces.get(i).rightCheekY != -1)) {

                        createCheek(faces.get(i));
                    }
                }

                handlerCheek.postDelayed(this, 16);
            }
        });

        final Handler handlerLight = new Handler();
        handlerLight.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                for (int i = 0; i < lights.size(); i++) {
                    layoutUnderSakura.removeView(lights.get(i));
                    lights.get(i).setVisibility(View.GONE);
                    lights.remove(i);

                    sparks.get(i).setVisibility(View.GONE);
                    sparks.remove(i);
                }


                if (toggleButtonMouth.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        if ((faces.get(i).bottomMouthX != -1 && faces.get(i).bottomMouthY != -1) ||
                                (faces.get(i).mouthX != -1 && faces.get(i).mouthY != -1)) {

                            createLightMouth(faces.get(i));
                        }
                    }
                }
                else if (toggleButtonEyes.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        if (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) {
                            createLightEyesLeft(faces.get(i));
                        }

                        if (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1) {
                            createLightEyesRight(faces.get(i));
                        }
                    }
                }
                else if (toggleButtonEars.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {

                        // left ear
                        if (faces.get(i).leftEarX != -1 && faces.get(i).leftEarY != -1) {
//                            createSakuraEarsLeft(faces.get(i), 0);
                        }
                        else if ((faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) &&
                                (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1)) {
//                            createSakuraEarsLeft(faces.get(i), 1);
                        }
                        else if ((faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) &&
                                (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1)) {
//                            createSakuraEarsLeft(faces.get(i), 2);
                        }

                        // right ear
                        if (faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) {
//                            createSakuraEarsRight(faces.get(i), 0);
                        }
                        else if ((faces.get(i).leftEarX != -1 && faces.get(i).leftEarY != -1) &&
                                (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1)) {
//                            createSakuraEarsRight(faces.get(i), 1);
                        }
                        else if ((faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1) &&
                                (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1)) {
//                            createSakuraEarsRight(faces.get(i), 2);
                        }
                    }
                }

                handlerLight.postDelayed(this, 16);
            }
        });


        final Handler handlerSakuraMouth = new Handler();
        handlerSakuraMouth.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (toggleButtonMouth.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        if ((faces.get(i).bottomMouthX != -1 && faces.get(i).bottomMouthY != -1) ||
                                (faces.get(i).mouthX != -1 && faces.get(i).mouthY != -1)) {
                            createSakuraMouth(faces.get(i));
                            createSakuraMouth(faces.get(i));

                            if (!faces.get(i).isPlayingSound()) {
                                faces.get(i).playSound();
                            }
                        }
                        else {
                            if (faces.get(i).isPlayingSound()) {
                                faces.get(i).pauseSound();
                            }
                        }
                    }
                }

                handlerSakuraMouth.postDelayed(this, (500 / volume1));
            }
        });

        final Handler handlerSakuraEyes = new Handler();
        handlerSakuraEyes.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (toggleButtonEyes.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        int sakuring = 0;

                        if (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) {
                            createSakuraEyesLeft(faces.get(i));
                            createSakuraEyesLeft(faces.get(i));
                            sakuring++;
                        }

                        if (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1) {
                            createSakuraEyesRight(faces.get(i));
                            createSakuraEyesRight(faces.get(i));
                            sakuring++;
                        }

                        if (sakuring >= 0) {
                            if (!faces.get(i).isPlayingSound()) {
                                faces.get(i).playSound();
                            }
                        }
                        else {
                            if (faces.get(i).isPlayingSound()) {
                                faces.get(i).pauseSound();
                            }
                        }

                    }
                }

                handlerSakuraEyes.postDelayed(this, (long) (1.5 * 500 / volume1));
            }
        });

        final Handler handlerSakuraEars = new Handler();
        handlerSakuraEars.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (toggleButtonEars.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        int sakuring = 0;

                        // left ear
                        if (faces.get(i).leftEarX != -1 && faces.get(i).leftEarY != -1) {
                            createSakuraEarsLeft(faces.get(i), 0);
                            sakuring++;
                        }
                        else if ((faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) &&
                                (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1)) {
                            createSakuraEarsLeft(faces.get(i), 1);
                            sakuring++;
                        }
                        else if ((faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) &&
                                (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1)) {
                            createSakuraEarsLeft(faces.get(i), 2);
                            sakuring++;
                        }

                        // right ear
                        if (faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) {
                            createSakuraEarsRight(faces.get(i), 0);
                            sakuring++;
                        }
                        else if ((faces.get(i).leftEarX != -1 && faces.get(i).leftEarY != -1) &&
                                (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1)) {
                            createSakuraEarsRight(faces.get(i), 1);
                            sakuring++;
                        }
                        else if ((faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1) &&
                                (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1)) {
                            createSakuraEarsRight(faces.get(i), 2);
                            sakuring++;
                        }

                        if (sakuring >= 0) {
                            if (!faces.get(i).isPlayingSound()) {
                                faces.get(i).playSound();
                            }
                        }
                        else {
                            if (faces.get(i).isPlayingSound()) {
                                faces.get(i).pauseSound();
                            }
                        }

                    }
                }

                handlerSakuraEars.postDelayed(this, (long) (1.5 * 500 / volume1));
            }
        });

    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setTrackingEnabled(true)
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

        mCameraSource = new CameraSource.Builder(context, detector)
//                .setRequestedPreviewSize(640, 480)
                .setRequestedPreviewSize(PREVIEW_CAM_Y, PREVIEW_CAM_X)
                .setFacing(CAMERA_FACING)
                .setAutoFocusEnabled(true)
                .setRequestedFps(15f)
                .build();





    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
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
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onNewItem(int faceId, Face item) {
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
            mOverlay.remove(mFaceGraphic);
        }
    }



    private static final int[][] cheek = {
            {R.drawable.cheek1_left, R.drawable.cheek1_right},
            {R.drawable.cheek2_left, R.drawable.cheek2_right},
            {R.drawable.cheek3_left, R.drawable.cheek3_right},
    };

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void createCheek(com.google.android.gms.samples.vision.face.facetracker.Face face) {

        int size = (int) Math.abs(face.leftCheekX - face.rightCheekX) / 2;

        FrameLayout.LayoutParams paramsLeft = new FrameLayout.LayoutParams(size, size);
        paramsLeft.leftMargin = (int) face.leftCheekX;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            paramsLeft.leftMargin = MAX_X - paramsLeft.leftMargin;
        }
        paramsLeft.leftMargin -= (size / 2);
        paramsLeft.topMargin = (int) face.leftCheekY - (size / 2);


        ImageView cheekLeft = new ImageView(getApplicationContext());
        cheekLeft.setImageResource(cheek[face.randomCheek][0]);
//        cheekLeft.setRotation(face.eulerY * 2 + face.eulerZ * 2);

        // TODO do (check) inverse for BACK CAM
        cheekLeft.setLayoutParams(paramsLeft);
        layoutUnderSakura.addView(cheekLeft);
        cheeks.add(cheekLeft);


        FrameLayout.LayoutParams paramsRight = new FrameLayout.LayoutParams(size, size);
        paramsRight.leftMargin = (int) face.rightCheekX;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            paramsRight.leftMargin = MAX_X - paramsRight.leftMargin;
        }
        paramsRight.leftMargin -= (size / 2);
        paramsRight.topMargin = (int) face.rightCheekY - (size / 2);

        ImageView cheekRight = new ImageView(getApplicationContext());
        cheekRight.setImageResource(cheek[face.randomCheek][1]);
//        cheekRight.setRotation(face.eulerY * 2 + face.eulerZ * 2);

        // TODO do (check) inverse for BACK CAM
        cheekRight.setLayoutParams(paramsRight);
        layoutUnderSakura.addView(cheekRight);
        cheeks.add(cheekRight);
    }


    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void createLightMouth(com.google.android.gms.samples.vision.face.facetracker.Face face) {

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

        final int sizeSpark = (int) com.google.android.gms.samples.vision.face.facetracker.Face.getDistance(face.rightEyeX, face.rightEyeY, face.leftEyeX, face.leftEyeY);

        final ImageView spark = new ImageView(getApplicationContext());
        spark.setImageResource(R.drawable.img_light_spark);
        spark.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams paramsSpark = new FrameLayout.LayoutParams(sizeSpark, sizeSpark);
        paramsSpark.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            paramsSpark.leftMargin = MAX_X - paramsSpark.leftMargin;
        }
        paramsSpark.leftMargin -= (sizeSpark / 2);
        paramsSpark.topMargin = (int) y - (sizeSpark / 2);

        spark.setLayoutParams(paramsSpark);
        layoutUnderSakura.addView(spark);
        sparks.add(spark);



        final int sizeX = (int) (MAX_X / 1.5);
        final int sizeY = (int) (MAX_Y);

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(R.drawable.light_pink);
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
        light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));

        light.setAlpha(0.8f);

        light.setLayoutParams(params);
        layoutUnderSakura.addView(light);
        lights.add(light);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void createLightEyesLeft(com.google.android.gms.samples.vision.face.facetracker.Face face) {

        float x = face.leftEyeX;
        float y = face.leftEyeY;

        final int sizeSpark = (int) com.google.android.gms.samples.vision.face.facetracker.Face.getDistance(face.rightEyeX, face.rightEyeY, face.leftEyeX, face.leftEyeY);

        final ImageView spark = new ImageView(getApplicationContext());
        spark.setImageResource(R.drawable.img_light_spark);
        spark.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams paramsSpark = new FrameLayout.LayoutParams(sizeSpark, sizeSpark);
        paramsSpark.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            paramsSpark.leftMargin = MAX_X - paramsSpark.leftMargin;
        }
        paramsSpark.leftMargin -= (sizeSpark / 2);
        paramsSpark.topMargin = (int) y - (sizeSpark / 2);

        spark.setLayoutParams(paramsSpark);
        layoutUnderSakura.addView(spark);
        sparks.add(spark);



        final int sizeX = (int) (MAX_X / 1.5);
        final int sizeY = (int) (MAX_Y);

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(R.drawable.light_pink);
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
        light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));

        light.setAlpha(0.8f);

        light.setLayoutParams(params);
        layoutUnderSakura.addView(light);
        lights.add(light);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void createLightEyesRight(com.google.android.gms.samples.vision.face.facetracker.Face face) {

        float x = face.rightEyeX;
        float y = face.rightEyeY;

        final int sizeSpark = (int) com.google.android.gms.samples.vision.face.facetracker.Face.getDistance(face.rightEyeX, face.rightEyeY, face.leftEyeX, face.leftEyeY);

        final ImageView spark = new ImageView(getApplicationContext());
        spark.setImageResource(R.drawable.img_light_spark);
        spark.setScaleType(ImageView.ScaleType.FIT_XY);

        FrameLayout.LayoutParams paramsSpark = new FrameLayout.LayoutParams(sizeSpark, sizeSpark);
        paramsSpark.leftMargin = (int) x;
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            paramsSpark.leftMargin = MAX_X - paramsSpark.leftMargin;
        }
        paramsSpark.leftMargin -= (sizeSpark / 2);
        paramsSpark.topMargin = (int) y - (sizeSpark / 2);

        spark.setLayoutParams(paramsSpark);
        layoutUnderSakura.addView(spark);
        sparks.add(spark);



        final int sizeX = (int) (MAX_X / 1.5);
        final int sizeY = (int) (MAX_Y);

        final ImageView light = new ImageView(getApplicationContext());
        light.setImageResource(R.drawable.light_pink);
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
        light.setRotation(-1 * ((float) (Math.toDegrees(Math.atan2(realY, realX))) - 90));

        light.setAlpha(0.8f);

        light.setLayoutParams(params);
        layoutUnderSakura.addView(light);
        lights.add(light);
    }


    int[] sa = {
            R.drawable.sakura_pink1,
            R.drawable.sakura_pink2,
            R.drawable.sakura_white1,
            R.drawable.sakura_white2,
            R.drawable.sakura3,
            R.drawable.sakura4,
            R.drawable.sakura5,
    };

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraMouth(com.google.android.gms.samples.vision.face.facetracker.Face face) {

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
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        int size = (int) Math.sqrt(Math.pow(face.leftEyeX - face.rightEyeX, 2) + Math.pow(face.leftEyeY - face.rightEyeY, 2)) / 2;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? size : size / 3, im < 4 ? size : size / 3);
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
        long duration = (long) (r.nextDouble() * 5000) + 2000;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/10.0));
        duration *= 2;

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
        duration /= 1.5;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX - (size / 2));
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, realY + (size / 2));
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

        animatorSet.playTogether(rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();

    }

    int fr = 0;
    private void getBitmapFromView(View view) {
        long l = SystemClock.currentThreadTimeMillis();
        Log.d("bitmaps", "Frame: " + fr + " start at " + new Date());
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
//        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
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
    private void createSakuraEyesLeft(com.google.android.gms.samples.vision.face.facetracker.Face face) {

        float x = face.leftEyeX;
        float y = face.leftEyeY;

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        int size = (int) Math.sqrt(Math.pow(face.leftEyeX - face.rightEyeX, 2) + Math.pow(face.leftEyeY - face.rightEyeY, 2)) / 2;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? size : size / 3, im < 4 ? size : size / 3);
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
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * facespeedx1) + 8000) / (speedx1/5.0));
        duration *= 5;

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeedy1) + 8000) / (speedy1/5.0));
        duration *= 5;

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;
        duration *= 5;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/5.0));
        duration *= 5;

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
        duration /= 1.5;

//        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX - (size / 2));
//        translationX.setInterpolator(new DecelerateInterpolator());
//        translationX.setDuration(duration);
//
//        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, realY + (size / 2));








//        float triangle = (float) Math.sqrt(Math.pow(MAX_X - x, 2) + Math.pow(MAX_Y - y, 2));
//        float realX = (float) Math.sin(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * triangle;
//        float realY = (float) Math.cos(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * triangle;
//
//        float posX = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));
//
//        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX + posX) : realX + posX;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX - (size / 2));
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, realY + (size / 2));
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


        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEyesRight(com.google.android.gms.samples.vision.face.facetracker.Face face) {

        float x = face.rightEyeX;
        float y = face.rightEyeY;

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        int size = (int) Math.sqrt(Math.pow(face.leftEyeX - face.rightEyeX, 2) + Math.pow(face.leftEyeY - face.rightEyeY, 2)) / 2;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? size : size / 3, im < 4 ? size : size / 3);
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
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * facespeedx1) + 8000) / (speedx1/5.0));
        duration *= 5;

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeedy1) + 8000) / (speedy1/5.0));
        duration *= 5;

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;
        duration *= 5;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/5.0));
        duration *= 5;

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
        duration /= 1.5;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX - (size / 2));
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, realY + (size / 2));
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


        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();


    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEarsLeft(com.google.android.gms.samples.vision.face.facetracker.Face face, int type) {

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
            float eeX = (face.rightEyeX - face.leftEyeX);
            float eeY = (face.rightEyeY - face.leftEyeY);
            x = face.leftEyeX - eeX;
            y = face.leftEyeY - eeY;
        }

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        int size = (int) Math.sqrt(Math.pow(face.leftEyeX - face.rightEyeX, 2) + Math.pow(face.leftEyeY - face.rightEyeY, 2)) / 2;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? size : size / 3, im < 4 ? size : size / 3);
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
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * facespeedx1) + 8000) / (speedx1/5.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeedy1) + 8000) / (speedy1/5.0));

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
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/7.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);












        float posX = (float) (0 - size / 2);
        float realY = (float) Math.sin(Math.toRadians(face.eulerZ * -1)) * MAX_Y;

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (MAX_X - x - posX) : (MAX_X - x - posX);

        duration *= 2;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration((int) (duration * (x - posX) / TRIANGLE));
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
        translationY.setDuration(duration);



        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEarsRight(com.google.android.gms.samples.vision.face.facetracker.Face face, int type) {

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
            float eeX = (face.rightEyeX - face.leftEyeX);
            float eeY = (face.rightEyeY - face.leftEyeY);
            x = face.rightEyeX + eeX;
            y = face.rightEyeY + eeY;
        }

        Random r = new Random();
        int k = r.nextInt(120);

        int im = (k < 40 ? 0 :
                (k < 60 ? 1 :
                        (k < 70 ? 2 :
                                (k < 100 ? 3 :
                                        (k < 110 ? 4 :
                                                (k < 115 ? 5 : 6))))));

        int size = (int) Math.sqrt(Math.pow(face.leftEyeX - face.rightEyeX, 2) + Math.pow(face.leftEyeY - face.rightEyeY, 2)) / 2;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? size : size / 3, im < 4 ? size : size / 3);
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
        int startD = sign ? r.nextInt(60) : r.nextInt(60) + 300;
        int endD = sign ? r.nextInt(60) + 300 : r.nextInt(60);
        long duration = (long) (((r.nextDouble() * 500.0 * facespeedx1) + 8000) / (speedx1/5.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeedy1) + 8000) / (speedy1/5.0));

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
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/7.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        float posX = (float) (0 - size / 2);
        float realY = (float) Math.sin(Math.toRadians(face.eulerZ)) * MAX_Y;

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? (MAX_X - x - posX) : -1 * (MAX_X - x - posX);

        duration *= 2;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
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
        float posY = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));

        float toY = realY + posY;

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, toY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);



        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
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
        if (requestCode != REQUEST_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
            toggleButtonRecord.setChecked(false);
            return;
        }
        mMediaProjectionCallback = new MediaProjectionCallback();
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    public void onToggleScreenShare(View view) {
        if (((ToggleButton) view).isChecked()) {
            initRecorder();
            shareScreen();
        } else {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            Log.v(TAG, "Stopping Recording");
            stopScreenSharing();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("MainActivity",
                MAX_X, MAX_Y, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null
                /*Handler*/);
    }

    private void initRecorder() {
        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mMediaRecorder.setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/video.mp4");
            mMediaRecorder.setVideoSize(MAX_X, MAX_Y);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setVideoEncodingBitRate(3000000);
            mMediaRecorder.setVideoFrameRate(30);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = ORIENTATIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientation);
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
                Log.v(TAG, "Recording Stopped");
            }
            mMediaProjection = null;
            stopScreenSharing();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
        // be reused again
        destroyMediaProjection();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "MediaProjection Stopped");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] +
                        grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                    onToggleScreenShare(toggleButtonRecord);
                } else {
                    toggleButtonRecord.setChecked(false);
                    Snackbar.make(findViewById(android.R.id.content), label_permissions,
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);
                                }
                            }).show();
                }
                return;
            }
        }


        // old
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

}
