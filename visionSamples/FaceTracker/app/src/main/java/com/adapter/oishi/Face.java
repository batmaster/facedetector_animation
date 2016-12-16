package com.adapter.oishi;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Random;

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

    public float bottomMouthX = -1;
    public float bottomMouthY = -1;

    public float leftCheekX = -1;
    public float leftCheekY = -1;
    public float rightCheekX = -1;
    public float rightCheekY = -1;

    public float faceHeight = 0;
    public float scale = 0;

    public boolean waitForStop;
    public int count;

    public static int MAX_COUNT = 5;

    private MediaPlayer mp;

    public Face() {
        Random r = new Random();
    }

    public void initSound(final Context context) {
        Log.d("sounddd", "initSound " + id);
        mp = MediaPlayer.create(context, id % 2 == 0 ? R.raw.sfx : R.raw.beam);
        mp.setLooping(true);
    }

    public void playSound() {
        Log.d("sounddd", "playSound " + id);
        mp.start();
    }

    public void pauseSound() {
        Log.d("sounddd", "pauseSound " + id);
        if (mp.isPlaying()) {
            mp.pause();
        }
    }

    public void stopSound() {
        Log.d("sounddd", "stopSound " + id);
        mp.stop();
        mp.reset();
        mp.release();
    }

    public boolean isPlayingSound() {
        Log.d("sounddd", "isPlayingSound " + id);
        return mp.isPlaying();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Face) || obj == null) {
            return false;
        }

        return this.id == ((Face) obj).id;
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public int getSakuraSize() {
        return (int) (faceHeight / 6 * scale);
    }

//    @Override
//    public String toString() {
//        return "Face{" +
//                "id=" + id +
//                ", mouthX=" + mouthX +
//                ", mouthY=" + mouthY +
//                ", leftEarX=" + leftEarX +
//                ", leftEarY=" + leftEarY +
//                ", rightEarX=" + rightEarX +
//                ", rightEarY=" + rightEarY +
//                ", leftEyeX=" + leftEyeX +
//                ", leftEyeY=" + leftEyeY +
//                ", rightEyeX=" + rightEyeX +
//                ", rightEyeY=" + rightEyeY +
//                ", eulerY=" + eulerY +
//                ", eulerZ=" + eulerZ +
//                ", bottomMouthX=" + bottomMouthX +
//                ", bottomMouthY=" + bottomMouthY +
//                ", leftCheekX=" + leftCheekX +
//                ", leftCheekY=" + leftCheekY +
//                ", rightCheekX=" + rightCheekX +
//                ", rightCheekY=" + rightCheekY +
//                ", randomCheek=" + randomCheek +
//                ", waitForStop=" + waitForStop +
//                ", count=" + count +
//                ", mp=" + mp +
//                '}';
//    }


    @Override
    public String toString() {
        return "Face{" +
                "id=" + id +
                ", count=" + count +
                '}';
    }
}
