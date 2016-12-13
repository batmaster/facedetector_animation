package com.google.android.gms.samples.vision.face.facetracker;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FinishRecordActivity extends AppCompatActivity {

    private ImageView imageViewHome;
    private ImageView imageViewPlay;
    private ImageView imageViewDelete;
    private ImageView imageViewSave;
    private ImageView imageViewShare;

    private VideoView videoView;

    private static File VIDEO_FILE_PATH_TEMP;
    private static String VIDEO_FILE_PATH;
    private String videoFileName;
    private Bitmap thumbnail;

    private Boolean hasSaved = false;

    private Dialog dialogNoInternet;
    private Dialog dialogConfirmNoSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_record);

        VIDEO_FILE_PATH = Environment.getExternalStorageDirectory() + "/oishi";
        VIDEO_FILE_PATH_TEMP = getFilesDir();

        videoFileName = getIntent().getStringExtra("videoFileName");

        dialogConfirmNoSave = new Dialog(FinishRecordActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialogConfirmNoSave.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogConfirmNoSave.setContentView(R.layout.dialog_confirm_unsave);
        dialogConfirmNoSave.setCancelable(false);

        ((ImageView) dialogConfirmNoSave.findViewById(R.id.imageViewClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogConfirmNoSave.dismiss();
            }
        });

        ((ImageView) dialogConfirmNoSave.findViewById(R.id.imageViewOk)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveVideoToPublic();

                dialogConfirmNoSave.dismiss();
            }
        });








        thumbnail = ThumbnailUtils.createVideoThumbnail(videoFileName, MediaStore.Images.Thumbnails.MINI_KIND);

        imageViewHome = (ImageView) findViewById(R.id.imageViewHome);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                startActivity(intent);

                finish();
            }
        });

        imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                imageViewPlay.setVisibility(View.INVISIBLE);
                videoView.setZ(10f);

                videoView.seekTo(1000);
                videoView.start();
            }
        });

        imageViewDelete = (ImageView) findViewById(R.id.imageViewDelete);
        imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTempVideo();

                Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
                startActivity(intent);

                finish();
            }
        });

        imageViewSave = (ImageView) findViewById(R.id.imageViewSave);
        imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveVideoToPublic();
            }
        });

        imageViewShare = (ImageView) findViewById(R.id.imageViewShare);
        imageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoPath(VIDEO_FILE_PATH_TEMP + "/" + videoFileName);
//        videoView.setMediaController(new MediaController(this));
//        videoView.requestFocus();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                imageViewPlay.setVisibility(View.VISIBLE);

                videoView.start();
                videoView.seekTo(1000);
                videoView.pause();

                videoView.setZ(0f);
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
                videoView.seekTo(1000);
                videoView.pause();

                videoView.setZ(0f);
            }
        });


    }


    private void saveVideoToPublic() {
        InputStream in = null;
        OutputStream out = null;

        try {
            File dir = new File(VIDEO_FILE_PATH);
            if(!dir.exists() || !dir.isDirectory()) {
                dir.mkdir();
            }

            in = new FileInputStream(VIDEO_FILE_PATH_TEMP + "/" + videoFileName);
            out = new FileOutputStream(VIDEO_FILE_PATH + "/" + videoFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(VIDEO_FILE_PATH_TEMP + "/" + videoFileName).delete();


        }
        catch (FileNotFoundException e) {
            Log.e("tag", e.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

        hasSaved = true;
        Toast.makeText(getApplicationContext(), "บันทึกไฟล์ " + videoFileName + " เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
    }

    private void deleteTempVideo() {
        File tmp = new File(VIDEO_FILE_PATH_TEMP + "/" + videoFileName);
        tmp.delete();
    }

    @Override
    public void onBackPressed() {
        if (!hasSaved) {
            dialogConfirmNoSave.show();
        }
        else {
            deleteTempVideo();

            Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
            startActivity(intent);

            finish();
        }
    }
}
