package com.google.android.gms.samples.vision.face.facetracker;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                checkAndRequestPermissions();

            }
        }, 4000);
    }



    private void checkAndRequestPermissions() {
        final String[] permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CAMERA) +
                ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, Manifest.permission.RECORD_AUDIO)) {

                // not pass
                ActivityCompat.requestPermissions(SplashActivity.this, permissions, REQUEST_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(SplashActivity.this, permissions, REQUEST_PERMISSIONS);
            }


        } else {
            // pass
            startNextActivity(true);
        }
    }

    private void startNextActivity(boolean pass) {
        Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
        if (!pass) {
            intent.putExtra("pass", false);
        }
        startActivity(intent);

        finish();
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] + grantResults[2]) == PackageManager.PERMISSION_GRANTED) {
                    // pass
                    startNextActivity(true);
                } else {
                    // not pass
                    startNextActivity(false);
                }
                return;
            }
        }
    }
}
