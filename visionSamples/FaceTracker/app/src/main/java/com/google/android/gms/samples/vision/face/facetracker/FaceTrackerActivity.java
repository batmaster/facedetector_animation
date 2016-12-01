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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {

    private RelativeLayout layoutSakura;

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
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        faces = new ArrayList<com.google.android.gms.samples.vision.face.facetracker.Face>();

        Singleton.activity = this;
        layoutSakura = (RelativeLayout) findViewById(R.id.layoutSakura);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
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
    }

    public static ArrayList<com.google.android.gms.samples.vision.face.facetracker.Face> faces;


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

    private int SIZE = 0;
    public static int MAX_X = 1080;
    private static int MAX_Y = 1920;
    private static int TRIANGLE = 0;

    private ToggleButton toggleButtonEars;
    private ToggleButton toggleButtonEyes;
    private ToggleButton toggleButtonMouth;

    public static int CAMERA_FACING = CameraSource.CAMERA_FACING_FRONT;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void makeThreadSakura() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        MAX_X = size.x;
        MAX_Y = size.y;
        SIZE = size.x/10;
        TRIANGLE = (int) Math.sqrt((MAX_X * MAX_X + MAX_Y * MAX_Y)) + SIZE / 2;


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
//                        createSakura(faces.get(i));
//                        createSakura(faces.get(i));
//
//                        createSakura2(faces.get(i));
//                    }
//
//                    if (faces.get(i).leftEarX != -1 && faces.get(i).leftEarY != -1) {
//                        createSakuraEars1(faces.get(i));
//                    }
//
//                    if (faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) {
//                        createSakuraEars2(faces.get(i));
//                    }
//
//                    if (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) {
//                        createSakuraEyes1(faces.get(i));
//                    }
//
//                    if (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1) {
//                        createSakuraEyes2(faces.get(i));
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

        final Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (toggleButtonMouth.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        if ((faces.get(i).bottomMouthX != -1 && faces.get(i).bottomMouthY != -1) ||
                                (faces.get(i).mouthX != -1 && faces.get(i).mouthY != -1)) {
                            createSakura(faces.get(i));
                            createSakura(faces.get(i));
                        }
                    }
                }

                Log.d("facesss", faces.size() + "");


                mHandler.postDelayed(this, (500 / volume1));
            }
        });

        final Handler mHandler2 = new Handler();
        mHandler2.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (toggleButtonMouth.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        if (faces.get(i).mouthX != -1 && faces.get(i).mouthY != -1) {
                            createSakura2(faces.get(i));
                        }
                    }
                }

                mHandler.postDelayed(this, (500 / volume2));
            }
        });


        final Handler mHandler3 = new Handler();
        mHandler3.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (toggleButtonEars.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        if (faces.get(i).leftEarX != -1 && faces.get(i).leftEarY != -1) {
                            createSakuraEars1(faces.get(i), 0);
                            Log.d("ears", 0 + "");
                        }
                        else if ((faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) &&
                                (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1)) {
                            createSakuraEars1(faces.get(i), 1);
                            Log.d("ears", 1 + "");
                        }
                        else if ((faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) &&
                                (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1)) {
                            createSakuraEars1(faces.get(i), 2);
                            Log.d("ears", 2 + "");
                        }

//                        if (faces.get(i).rightEarX != -1 && faces.get(i).rightEarY != -1) {
//                            createSakuraEars2(faces.get(i));
//                        }
                    }
                }

                mHandler3.postDelayed(this, (long) (1.5 * 500 / volume1));
            }
        });

        final Handler mHandler4 = new Handler();
        mHandler4.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {

                if (toggleButtonEyes.isChecked()) {
                    for (int i = 0; i < faces.size(); i++) {
                        if (faces.get(i).leftEyeX != -1 && faces.get(i).leftEyeY != -1) {
                            createSakuraEyes1(faces.get(i));
                        }

                        if (faces.get(i).rightEyeX != -1 && faces.get(i).rightEyeY != -1) {
                            createSakuraEyes2(faces.get(i));
                        }
                    }
                }

                mHandler4.postDelayed(this, (long) (1.5 * 500 / volume1));
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
                .setRequestedPreviewSize(1920, 1080)
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

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
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

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakura(com.google.android.gms.samples.vision.face.facetracker.Face face) {

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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin) - SIZE;
        }
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
        long duration = (long) (((r.nextDouble() * 500.0 * facespeedx1) + 8000) / (speedx1/10.0));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeedy1) + 8000) / (speedy1/10.0));

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
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/10.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;

        float realX = (float) Math.sin(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * TRIANGLE;
        float realY = (float) Math.cos(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * TRIANGLE;

        float posX = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX + posX) : realX + posX;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        Log.d("transs", (realX + posX) + " " + (y + realY));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, y + realY);
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

        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY/*, alpha*/);
        animatorSet.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void createSakura2(com.google.android.gms.samples.vision.face.facetracker.Face face) {

        float x = face.mouthX;
        float y = face.mouthY;

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
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = MAX_X - params.leftMargin;
        }
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
        long duration = (long) (r.nextDouble() * 1000 * facespeedx2) + 15000 + (1000 * (20 - speedx2));

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 1000 * facespeedy2) + 15000 + (1000 * (20 - speedy2));

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeed2) + 4000) / (speed2/10.0));

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.25 * finalSize2);
        duration = (long) (((r.nextDouble() * 400.0 * facespeed2) + 2500) / (speed2/10.0));

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.02f * startSize2)), sakura.getScaleX() * (1f + (0.02f * startSize2)) + scale);
        scaleX.setInterpolator(new AccelerateInterpolator());
        scaleX.setDuration(duration);

        Log.d("scaleee", sakura.getScaleX() * (1f + (0.02f * startSize2)) + " " + scale);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.02f * startSize2)), sakura.getScaleX() * (1f + (0.02f * startSize2)) + scale);
        scaleY.setInterpolator(new AccelerateInterpolator());
        scaleY.setDuration(duration);

        duration = (long) (((r.nextDouble() * 500.0 * facespeed2) + 2500) / (speed2/10.0));
        sign = r.nextDouble() > 0.5 ? true : false;
        int x2 = r.nextInt(MAX_Y) + 250;

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * x2 * (sign ? 1 : -1) : x2 * (sign ? 1 : -1);

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
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
    }

    int fr = 0;

    private void getBitmapFromView(View view) {
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
    private void createSakuraEars1(com.google.android.gms.samples.vision.face.facetracker.Face face, int type) {

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
            float eeX = (face.rightEyeX - face.leftEyeX) / 2;
            float eeY = (face.rightEyeY - face.leftEyeY) / 2;
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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin) - SIZE;
        }
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

        float realX = (float) Math.cos(Math.toRadians(face.eulerZ * 2)) * TRIANGLE * -1;
        float realY = (float) Math.sin(Math.toRadians(face.eulerZ * 2)) * MAX_Y/10;

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (x - realX) : (x - realX);

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

        Log.d("toyy", toY + " ");

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, toY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);



        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEars2(com.google.android.gms.samples.vision.face.facetracker.Face face) {

        float x = face.rightEarX;
        float y = face.rightEarY;

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
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = MAX_X - params.leftMargin;
        }
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

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (0 - x - SIZE) : (0 - x - SIZE);

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
        float posY = (float) (r.nextDouble() * (MAX_Y/10.0) * (sign ? 1 : -1));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, posY);
        translationY.setInterpolator(new DecelerateInterpolator());
        translationY.setDuration(duration);



        animatorSet.playTogether(rotationX, rotationY, rotationZ, scaleX, scaleY, translationX, translationY);
        animatorSet.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void createSakuraEyes1(com.google.android.gms.samples.vision.face.facetracker.Face face) {

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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin) - SIZE;
        }
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
        long duration = (long) (((r.nextDouble() * 500.0 * facespeedx1) + 8000) / (speedx1/5.0));
        duration *= 4;

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeedy1) + 8000) / (speedy1/5.0));
        duration *= 4;

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;
        duration *= 4;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/5.0));
        duration *= 8;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);

        float realX = (float) Math.sin(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * TRIANGLE;
        float realY = (float) Math.cos(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * TRIANGLE;

        float posX = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX + posX) : realX + posX;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        Log.d("transs", (realX + posX) + " " + (y + realY));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, y + realY);
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
    private void createSakuraEyes2(com.google.android.gms.samples.vision.face.facetracker.Face face) {

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

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(im < 4 ? SIZE : SIZE / 3, im < 4 ? SIZE : SIZE / 3);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.leftMargin = (int) x - (SIZE / 2);
        if (CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
            params.leftMargin = (MAX_X - params.leftMargin) - SIZE;
        }
        params.topMargin = (int) y - (SIZE / 2);

        Log.d("eyes", x + " " + params.leftMargin);

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
        long duration = (long) (((r.nextDouble() * 500.0 * facespeedx1) + 8000) / (speedx1/5.0));
        duration *= 4;

        ObjectAnimator rotationX = ObjectAnimator.ofFloat(sakura, View.ROTATION_X, startD, endD);
        rotationX.setRepeatCount(ValueAnimator.INFINITE);
        rotationX.setRepeatMode(ValueAnimator.RESTART);
        rotationX.setInterpolator(new LinearInterpolator());
        rotationX.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (((r.nextDouble() * 500.0 * facespeedy1) + 8000) / (speedy1/5.0));
        duration *= 4;

        ObjectAnimator rotationY = ObjectAnimator.ofFloat(sakura, View.ROTATION_Y, startD, endD);
        rotationY.setRepeatCount(ValueAnimator.INFINITE);
        rotationY.setRepeatMode(ValueAnimator.RESTART);
        rotationY.setInterpolator(new LinearInterpolator());
        rotationY.setDuration(duration);


        sign = r.nextDouble() > 0.5 ? true : false;
        startD = sign ? 0 : 359;
        endD = sign ? 359 : 0;
        duration = (long) (r.nextDouble() * 5000) + 2000;
        duration *= 4;

        ObjectAnimator rotationZ = ObjectAnimator.ofFloat(sakura, View.ROTATION, startD, endD);
        rotationZ.setRepeatCount(ValueAnimator.INFINITE);
        rotationZ.setRepeatMode(ValueAnimator.RESTART);
        rotationZ.setInterpolator(new LinearInterpolator());
        rotationZ.setDuration(duration);


        float scale = (float) (r.nextDouble() * 0.1 * finalSize1) + 1.2f;
        duration = (long) (((r.nextDouble() * 200.0 * facespeed1) + 2000) / (speed1/5.0));
        duration *= 8;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(sakura, View.SCALE_X, sakura.getScaleX() * (1f + (0.05f * startSize1)), scale);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(duration);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(sakura, View.SCALE_Y, sakura.getScaleY() * (1f + (0.05f * startSize1)), scale);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(duration);



        float realX = (float) Math.sin(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * TRIANGLE;
        float realY = (float) Math.cos(Math.toRadians(face.eulerY * 2 + face.eulerZ * 2)) * TRIANGLE;

        float posX = (float) (r.nextDouble() * (MAX_Y/4.0) * (sign ? 1 : -1));

        float toX = CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT ? -1 * (realX + posX) : realX + posX;

        ObjectAnimator translationX = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_X, 0, toX);
        translationX.setInterpolator(new DecelerateInterpolator());
        translationX.setDuration(duration);

        Log.d("transs", (realX + posX) + " " + (y + realY));

        ObjectAnimator translationY = ObjectAnimator.ofFloat(sakura, View.TRANSLATION_Y, 0, y + realY);
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
}
