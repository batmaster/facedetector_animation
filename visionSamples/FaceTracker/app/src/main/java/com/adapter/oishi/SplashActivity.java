package com.adapter.oishi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class SplashActivity extends AppCompatActivity {

    private OishiApplication app;
    private static final int REQUEST_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("FaceTrackerActivity", false)) {
            Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
            startActivity(intent);

            finish();
        }
        setContentView(R.layout.activity_splash);

        app = (OishiApplication) getApplicationContext();
        app.sendPageStat("splash_page");

        if (AccessToken.getCurrentAccessToken() == null && SharePref.getStringRid(getApplicationContext()) == null) {
            SharePref.setStringRid(getApplicationContext(), "fbid_" + UUID.randomUUID().toString());
        }

        app.getHttpService().getDataInfo(new HTTPService.OnResponseCallback<JSONObject>() {
            @Override
            public void onResponse(boolean success, Throwable error, JSONObject data) {
                if (!(data == null)) {
                    try {

                        JSONArray appdata = data.getJSONArray("appdata");

                        String share_url = appdata.getJSONObject(0).getString("share_url");
                        String share_title = appdata.getJSONObject(0).getString("share_title");
                        String share_description = appdata.getJSONObject(0).getString("share_description");
                        String share_image = appdata.getJSONObject(0).getString("share_image");
                        String share_twitter_url = appdata.getJSONObject(0).getString("share_twitter_url");
                        String share_twitter_description = appdata.getJSONObject(0).getString("share_twitter_description");
                        String share_gplus_url = appdata.getJSONObject(0).getString("share_gplus_url");
                        String copy_url = appdata.getJSONObject(0).getString("copy_url");

                        Config.setString(getApplicationContext(), Config.share_url, share_url);
                        Config.setString(getApplicationContext(), Config.share_title, share_title);
                        Config.setString(getApplicationContext(), Config.share_description, share_description);
                        Config.setString(getApplicationContext(), Config.share_image, share_image);
                        Config.setString(getApplicationContext(), Config.share_twitter_url, share_twitter_url);
                        Config.setString(getApplicationContext(), Config.share_twitter_description, share_twitter_description);
                        Config.setString(getApplicationContext(), Config.share_gplus_url, share_gplus_url);
                        Config.setString(getApplicationContext(), Config.copy_url, copy_url);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                checkAndRequestPermissions();

                            }
                        }, 3000);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "เชื่อมต่ออินเตอร์เน็ตไม่ได้", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            checkAndRequestPermissions();

                        }
                    }, 3000);
                }
            }
        });


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
