package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.media.MediaPlayer;

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

    public int count;

    public static int MAX_COUNT = 5;

    private MediaPlayer mp;

    public Face() {

    }

    public void initSound(final Context context) {
        mp = MediaPlayer.create(context, randomSound());
        mp.seekTo(1000);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.reset();
                mp = MediaPlayer.create(context, randomSound());
                mp.seekTo(1000);
                mp.start();
            }
        });
    }

    private int randomSound() {
        Random r = new Random();
        return r.nextDouble() > 0.7 ? R.raw.sound1 : R.raw.sound2;
    }

    public void playSound() {
        mp.start();
    }

    public void pauseSound() {
        if (mp.isPlaying()) {
            mp.pause();
        }
    }

    public void stopSound() {
        mp.stop();
        mp.reset();
        mp.release();
    }

    public boolean isPlayingSound() {
        return mp.isPlaying();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Face) || obj == null) {
            return false;
        }

        return this.id == ((Face) obj).id;
    }
}
