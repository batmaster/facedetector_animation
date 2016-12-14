package com.google.android.gms.samples.vision.face.facetracker;

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

    public int randomCheek = 0;

    public boolean waitForStop;
    public int count;

    public static int MAX_COUNT = 5;

    private MediaPlayer mp;

    public Face() {
        Random r = new Random();
        randomCheek = r.nextInt(4);
    }

    public void initSound(final Context context) {
        Log.d("sounddd", "initSound " + id);
        int sid = randomSound();
        mp = MediaPlayer.create(context, sid);
        mp.seekTo(1000);
        mp.setLooping(false);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                int sid = randomSound();
                mp.reset();
                mp = MediaPlayer.create(context, sid);
                mp.seekTo(1000);
                mp.setLooping(false);
                mp.start();
            }
        });
    }

    private int randomSound() {
        Random r = new Random();
        return r.nextDouble() > 0.7 ? R.raw.sound1 : R.raw.sound2;
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

}
