package com.google.android.gms.samples.vision.face.facetracker;

import android.app.Application;

import com.appsflyer.AppsFlyerLib;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by batmaster on 12/14/2016 AD.
 */

public class OishiApplication extends Application {



    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        AppsFlyerLib.getInstance().startTracking(this, "vdVe9UxXnHjhUoKFT2HAnK");
        AppsFlyerLib.getInstance().setAppId("com.adapter.oishi");
    }
}
