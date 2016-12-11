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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);

        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = Singleton.activity.PREVIEW_CAM_X;
        double imageHeight = Singleton.activity.PREVIEW_CAM_Y;
        float scale = (float) Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        List<Landmark> landmarks = face.getLandmarks();
        float x1 = -1;
        float x2 = -1;
        float y1 = -1;
        float y2 = -1;

        if (landmarks.size() == 0) {
            for (int i = 0; i < Singleton.activity.getFaces().size(); i++) {
                if (Singleton.activity.getFaces().get(i).id == face.getId()) {
                    Singleton.activity.removeFace(i);
                    return;
                }
            }

        }




        int existIndex = -1;
        com.google.android.gms.samples.vision.face.facetracker.Face existFace = null;
        for (int i = 0; i < Singleton.activity.getFaces().size(); i++) {
            if (Singleton.activity.getFaces().get(i).id == face.getId()) {
                existFace = Singleton.activity.getFaces().get(i);
                existIndex = i;
                break;
            }
        }

        if (existIndex == -1) {
            existFace = new com.google.android.gms.samples.vision.face.facetracker.Face();
            existFace.id = face.getId();
        }



        Log.d("landmarkkk", "======");

        for (int i = 0; i < landmarks.size(); i++) {

            Log.d("landmarkkk", getLandmarkType(landmarks.get(i).getType()));

            float whereX = landmarks.get(i).getPosition().x * scale;
            if (Singleton.activity.CAMERA_FACING == CameraSource.CAMERA_FACING_FRONT) {
                whereX = Singleton.activity.PREVIEW_CAM_X - whereX;
            }
            canvas.drawCircle(whereX, landmarks.get(i).getPosition().y * scale, FACE_POSITION_RADIUS, mFacePositionPaint);


            if (landmarks.get(i).getType() == Landmark.LEFT_MOUTH) {
                x1 = landmarks.get(i).getPosition().x  * scale;
                y1 = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.RIGHT_MOUTH) {
                x2 = landmarks.get(i).getPosition().x  * scale;
                y2 = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.LEFT_EAR_TIP) {
                existFace.leftEarX = landmarks.get(i).getPosition().x  * scale;
                existFace.leftEarY = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.RIGHT_EAR_TIP) {
                existFace.rightEarX = landmarks.get(i).getPosition().x  * scale;
                existFace.rightEarY = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.LEFT_EYE) {
                existFace.leftEyeX = landmarks.get(i).getPosition().x  * scale;
                existFace.leftEyeY = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.RIGHT_EYE) {
                existFace.rightEyeX = landmarks.get(i).getPosition().x  * scale;
                existFace.rightEyeY = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.BOTTOM_MOUTH) {
                existFace.bottomMouthX = landmarks.get(i).getPosition().x  * scale;
                existFace.bottomMouthY = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.LEFT_CHEEK) {
                existFace.leftCheekX = landmarks.get(i).getPosition().x  * scale;
                existFace.leftCheekY = landmarks.get(i).getPosition().y  * scale;
            }
            else if (landmarks.get(i).getType() == Landmark.RIGHT_CHEEK) {
                existFace.rightCheekX = landmarks.get(i).getPosition().x  * scale;
                existFace.rightCheekY = landmarks.get(i).getPosition().y  * scale;
            }

        }

        Log.d("Euler", face.getEulerY() + " " + face.getEulerZ());

        if (x1 != -1 && x2 != -1 && y1 != -1 && y2 != -1) {
            existFace.mouthX = Math.abs((x2 + x1) / 2);
            existFace.mouthY = Math.abs((y2 + y1) / 2);
        }

        existFace.eulerY = face.getEulerY();
        existFace.eulerZ = face.getEulerZ();



        if (existIndex == -1) {
            Singleton.activity.addFace(existFace);
        }
    }




    private String getLandmarkType(int which) {
        switch (which) {
            case Landmark.BOTTOM_MOUTH:
                return "Bottom mouth";

            case Landmark.LEFT_CHEEK:
                return "Left cheek";

            case Landmark.LEFT_EAR:
                return "Left ear";

            case Landmark.LEFT_EAR_TIP:
                return "Left ear tip";

            case Landmark.LEFT_EYE:
                return "Left eye";

            case Landmark.LEFT_MOUTH:
                return "Left mouth";

            case Landmark.NOSE_BASE:
                return "nose base";

            case Landmark.RIGHT_CHEEK:
                return "Right cheek";

            case Landmark.RIGHT_EAR:
                return "Right ear";

            case Landmark.RIGHT_EAR_TIP:
                return "Right ear tip";

            case Landmark.RIGHT_EYE:
                return "Right eye";

            case Landmark.RIGHT_MOUTH:
                return "Right mouth";

            default:
                return "Unidentified";

        }
    }

}
