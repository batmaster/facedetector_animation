package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by batmaster on 6/17/16 AD.
 */
public class SharePref {

    public static void setStringRid(Context context, String value) {
        SharedPreferences sp = context.getSharedPreferences("oishi_sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("rid", value);
        editor.commit();
    }

    public static String getStringRid(Context context) {
        SharedPreferences sp = context.getSharedPreferences("oishi_sakura", Context.MODE_PRIVATE);
        return sp.getString("rid", null);
    }
}
