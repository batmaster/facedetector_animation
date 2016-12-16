package com.adapter.oishi;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import io.fabric.sdk.android.Fabric;

/**
 * Created by batmaster on 12/14/2016 AD.
 */

public class OishiApplication extends Application {

    private Tracker mTracker;

    private HTTPService httpService;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        AppsFlyerLib.getInstance().startTracking(this, "HGETasn6yh8FhMC2LQgEWP");
        AppsFlyerLib.getInstance().setAppId("com.adapter.oishi");

        httpService = new HTTPService(getApplicationContext());
    }

    public HTTPService getHttpService() {
        return httpService;
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    public void sendPageStat(String label) {

        Log.d("sendPageStat", label);

        getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Page")
                .setAction("opened")
                .setLabel(label)
                .build());

    }

    public void sendButtonStat(String label) {

        Log.d("sendButtonStat", label);

        getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setCategory("Button")
                .setAction("clicked")
                .setLabel(label)
                .build());

    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
