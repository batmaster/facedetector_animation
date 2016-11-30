package com.google.android.gms.samples.vision.face.facetracker;

/**
 * Created by batmaster on 11/21/2016 AD.
 */

public class Face {
    
    public int id;
    public float mouthX = -1;
    public float mouthY = -1;
    public float leftEarX = -1;
    public float leftEarY = -1;
    public float rightEarX = -1;
    public float rightEarY = -1;
    public float leftEyeX = -1;
    public float leftEyeY = -1;
    public float rightEyeX = -1;
    public float rightEyeY = -1;
    public float eulerY = -1;
    public float eulerZ = -1;

    public int count;

    public static int MAX_COUNT = 5;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Face) || obj == null) {
            return false;
        }

        return this.id == ((Face) obj).id;
    }

    @Override
    public String toString() {
        return "Face{" +
                "id=" + id +
                ", mouthX=" + mouthX +
                ", mouthY=" + mouthY +
                ", leftEarX=" + leftEarX +
                ", leftEarY=" + leftEarY +
                ", rightEarX=" + rightEarX +
                ", rightEarY=" + rightEarY +
                ", leftEyeX=" + leftEyeX +
                ", leftEyeY=" + leftEyeY +
                ", rightEyeX=" + rightEyeX +
                ", rightEyeY=" + rightEyeY +
                ", eulerY=" + eulerY +
                ", eulerZ=" + eulerZ +
                ", count=" + count +
                '}';
    }
}
