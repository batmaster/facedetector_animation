package com.google.android.gms.samples.vision.face.facetracker;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.appsflyer.AppsFlyerLib;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by batmaster on 12/14/2016 AD.
 */

public class OishiApplication extends Application {


    private HTTPService httpService;

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        AppsFlyerLib.getInstance().startTracking(this, "vdVe9UxXnHjhUoKFT2HAnK");
        AppsFlyerLib.getInstance().setAppId("com.adapter.oishi");

        httpService = new HTTPService(getApplicationContext());
    }

    public HTTPService getHttpService() {
        return httpService;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
