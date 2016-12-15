package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by batmaster on 2/26/16 AD.
 */
public class PreferenceService {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public static final String KEY_BOOLEAN_HAS_UPDATE_FB_INFO = "KEY_BOOLEAN_HAS_UPDATE_FB_INFO";

    public PreferenceService(Context context) {
        sp = context.getSharedPreferences("oishi_sakura", Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public boolean getBoolean(String key) {
        return sp.getBoolean(key, false);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }


}
