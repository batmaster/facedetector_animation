package com.adapter.oishi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.plus.PlusShare;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class FinishRecordActivity extends AppCompatActivity {

    private OishiApplication app;

    private ImageView imageViewHome;
    private ImageView imageViewShareCorner;
    private ImageView imageViewPlay;
    private ImageView imageViewDelete;
    private ImageView imageViewSave;
    private ImageView imageViewShare;

    private VideoView videoView;

    private static String VIDEO_FILE_PATH_TEMP;
    private static String VIDEO_FILE_PATH;
    private String videoFileName;

    private Boolean hasSaved = false;

    private Dialog dialogNoInternet;
    private Dialog dialogConfirmNoSave;

    private CallbackManager callbackManager;

    private String gid;
    private String where;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_record);

        app = (OishiApplication) getApplicationContext();
        app.sendPageStat("finish");

        gid = getIntent().getStringExtra("gid");
        where = getIntent().getStringExtra("where");
        app.getHttpService().goToPreview(gid, where, new HTTPService.OnResponseCallback<JSONObject>() {
            @Override
            public void onResponse(boolean success, Throwable error, JSONObject data) {

            }
        });

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FBLogin", "onSuccess" + loginResult.toString());

                sharing = false;
                share();
            }

            @Override
            public void onCancel() {
                sharing = false;
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("FBLogin", "onError" + error.getMessage());
                Crashlytics.logException(error);
                sharing = false;
            }
        });

        VIDEO_FILE_PATH = Environment.getExternalStorageDirectory() + "/oishi";
        VIDEO_FILE_PATH_TEMP = getFilesDir().getAbsolutePath() + "/tmp";


        videoFileName = getIntent().getStringExtra("videoFileName");

        dialogConfirmNoSave = new Dialog(FinishRecordActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialogConfirmNoSave.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogConfirmNoSave.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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

        dialogNoInternet = new Dialog(FinishRecordActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialogNoInternet.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogNoInternet.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogNoInternet.setContentView(R.layout.dialog_no_internet);
        dialogNoInternet.setCancelable(false);

        ((ImageView) dialogNoInternet.findViewById(R.id.imageViewClose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogNoInternet.dismiss();
            }
        });

        imageViewHome = (ImageView) findViewById(R.id.imageViewHome);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                startActivity(intent);

                finish();
            }
        });

        imageViewShareCorner = (ImageView) findViewById(R.id.imageViewShareCorner);
        imageViewShareCorner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog dialog = new BottomSheetDialog(FinishRecordActivity.this);

                View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.share_dialog, null);
//                final BottomSheetBehavior behavior = BottomSheetBehavior.from(v);
//                behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//                    @Override
//                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                        }
//                    }
//
//                    @Override
//                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//
//                    }
//                });

                ((LinearLayout) v.findViewById(R.id.linearFacebook)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + Config.getString(getApplicationContext(), Config.share_url);
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
//                        startActivity(intent);

                        ShareLinkContent content = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse(Config.getString(getApplicationContext(), Config.share_url)))
                                .build();

                        ShareDialog dialog = new ShareDialog(FinishRecordActivity.this);
                        dialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                            @Override
                            public void onSuccess(Sharer.Result result) {
                                sharing = false;
                                Log.d("FBShare", "onSuccess " + result);

                            }

                            @Override
                            public void onCancel() {
                                sharing = false;
                                Log.d("FBShare", "onCancel()");

                            }

                            @Override
                            public void onError(FacebookException error) {
                                error.printStackTrace();

                                sharing = false;
                                Log.d("FBShare", "onError" + error.getMessage());
                                Crashlytics.logException(error);

                            }
                        });

                    }
                });

                ((LinearLayout) v.findViewById(R.id.linearTwitter)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String url = Config.getString(getApplicationContext(), Config.share_twitter_url);
                        String desc = Config.getString(getApplicationContext(), Config.share_twitter_description);

                        String uri = "http://www.twitter.com/intent/tweet?url=" + url + "&text=" + desc;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(uri));
                        startActivity(i);
                    }
                });

                ((LinearLayout) v.findViewById(R.id.linearGoogle)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        intent.setType("text/plain");
//                        intent.putExtra(Intent.EXTRA_TEXT, Config.getString(getApplicationContext(), Config.share_gplus_url));
//                        intent.setPackage(filterByPackageName(getApplicationContext(), intent, "com.google.android.apps.plus"));
//                        startActivity(intent);

                        Intent shareIntent = new PlusShare.Builder(FinishRecordActivity.this)
                                .setType("text/plain")
//                                .setText(Config.getString(getApplicationContext(), Config.share_gplus_url))
                                .setContentUrl(Uri.parse(Config.getString(getApplicationContext(), Config.share_gplus_url)))
                                .getIntent();
                        startActivity(shareIntent);


                    }
                });

                ((LinearLayout) v.findViewById(R.id.linearUrl)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setClipboard(Config.getString(getApplicationContext(), Config.copy_url));
                        Toast.makeText(getApplicationContext(), "คัดลอกลิ้งค์เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.setContentView(v);
                dialog.show();
            }
        });

        imageViewPlay = (ImageView) findViewById(R.id.imageViewPlay);
        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageViewPlay.setVisibility(View.INVISIBLE);
                videoView.setZ(10f);

                videoView.start();
                app.sendPageStat("preview");
            }
        });

        imageViewDelete = (ImageView) findViewById(R.id.imageViewDelete);
        imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTempVideo();

//                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
//                intent.putExtra("FaceTrackerActivity", true);
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
                share();
            }
        });

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoPath(VIDEO_FILE_PATH_TEMP + "/" + videoFileName);
//        videoView.setMediaController(new MediaController(this));
//        videoView.requestFocus();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                imageViewPlay.setVisibility(View.VISIBLE);

                videoView.seekTo(1000);

                videoView.setZ(0f);
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.seekTo(1000);

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
//
//            // delete the original file
//            new File(VIDEO_FILE_PATH_TEMP + "/" + videoFileName).delete();

            hasSaved = true;
            Toast.makeText(getApplicationContext(), "บันทึกไฟล์ " + videoFileName + " เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
            app.getHttpService().sendStat(HTTPService.SAVERESULT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File("file://" + Environment.getExternalStorageDirectory());
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }
            else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            }

        }
        catch (FileNotFoundException e) {
            Log.e("tag", e.getMessage());
            Toast.makeText(getApplicationContext(), "บันทึกไฟล์ " + videoFileName + " ไม่สำเร็จ", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
            Toast.makeText(getApplicationContext(), "บันทึกไฟล์ " + videoFileName + " ไม่สำเร็จ", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteTempVideo() {
        File tmp = new File(VIDEO_FILE_PATH_TEMP + "/" + videoFileName);
        tmp.delete();
    }

    @Override
    public void onBackPressed() {
        if (!hasSaved) {
            dialogConfirmNoSave.show();
            app.sendPageStat("unrecorded");
        }
        else {
            deleteTempVideo();

            Intent intent = new Intent(getApplicationContext(), FaceTrackerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

            finish();
        }
    }

    private void setClipboard(String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    private static String filterByPackageName(Context context, Intent intent, String prefix) {
        List<ResolveInfo> matches = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith(prefix)) {
                return info.activityInfo.packageName;
            }
        }
        return null;
    }

    private boolean sharing = false;
    private void share() {
        if (app.isNetworkConnected()) {
            if (!sharing) {
                sharing = true;

                if (AccessToken.getCurrentAccessToken() == null) {
                    String[] permissions = {"public_profile", "email"};
                    LoginManager.getInstance().logInWithReadPermissions(FinishRecordActivity.this, Arrays.asList(permissions));
                } else {
                    File f = new File(VIDEO_FILE_PATH_TEMP + "/" + videoFileName);
                    Uri videoFileUri = Uri.fromFile(f);
                    ShareVideo video = new ShareVideo.Builder()
                            .setLocalUrl(videoFileUri)
                            .build();
                    ShareVideoContent content = new ShareVideoContent.Builder()
                            .setVideo(video)
                            .build();

                    ShareDialog dialog = new ShareDialog(FinishRecordActivity.this);
                    dialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                        @Override
                        public void onSuccess(Sharer.Result result) {
                            sharing = false;
                            Log.d("FBShare", "onSuccess " + result);

                            app.getHttpService().sendStat(HTTPService.SHARERESULT);
                            app.getHttpService().saveShare(gid, result.getPostId(), new HTTPService.OnResponseCallback<JSONObject>() {
                                @Override
                                public void onResponse(boolean success, Throwable error, JSONObject data) {

                                }
                            });

                            final PreferenceService pref = new PreferenceService(getApplicationContext());
                            if (AccessToken.getCurrentAccessToken() != null && (pref.getBoolean(PreferenceService.KEY_BOOLEAN_HAS_UPDATE_FB_INFO) == false)) {

                                app.getHttpService().updateToken(new HTTPService.OnResponseCallback<JSONObject>() {
                                    @Override
                                    public void onResponse(boolean success, Throwable error, JSONObject data) {
                                        pref.putBoolean(PreferenceService.KEY_BOOLEAN_HAS_UPDATE_FB_INFO, true);

                                        Log.d("httpapi", "API 5 Save FB Info OK!!");
                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancel() {
                            sharing = false;
                            Log.d("FBShare", "onCancel()");

                        }

                        @Override
                        public void onError(FacebookException error) {
                            error.printStackTrace();

                            sharing = false;
                            Log.d("FBShare", "onError" + error.getMessage());
                            Crashlytics.logException(error);

                        }
                    });

                    if (dialog.canShow(ShareVideoContent.class)) {
                        dialog.show(content, ShareDialog.Mode.AUTOMATIC);
                    } else {
                        Log.d("FBShare", "you cannot share :(");
                    }
                }
            }
        }
        else {
            Log.d("FBShare", "no net");
            dialogNoInternet.show();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
