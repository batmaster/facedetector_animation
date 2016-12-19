package com.adapter.oishi;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by batmaster on 9/11/2016 AD.
 */

public class Config {

    public static final String share_url = "share_url";
    public static final String share_title = "share_title";
    public static final String share_description = "share_description";
    public static final String share_image = "share_image";
    public static final String share_twitter_url = "share_twitter_url";
    public static final String share_twitter_description = "share_twitter_description";
    public static final String share_gplus_url = "share_gplus_url";
    public static final String copy_url = "copy_url";

    private static final String[] pages = {
            "http://www.oishidrink.com/sakura/app_install.php",
            "โหลดเลย! แอป Oishi \\\"ระเบิดความฮา ซากุระมาเต็ม\\\"",
            "มาปลดปล่อยความสดชื่นกับโออิชิ ซากุระ สตรอเบอร์รี่กันดีกว่า ทั้งสนุกทั้งฟินไปกับฟังก์ชั่นที่ทำให้เพื่อนๆ ได้ปลดปล่อยความสดชื่นของดอกซากุระออกมาพร้อมกับสีหน้าสุดมันส์ของคุณ รีบเล่น  รีบแชร์ให้หมด ถ้าสดชื่นนนนนน ไม่อยากตกเทรนด์ โหลดเลย!",
            "http://www.oishidrink.com/sakura/app/image/shareApp.jpg",
            "http://www.oishidrink.com/sakura/app_install.php",
            "xxxxxxxxxxxxxxxxx",
            "http://www.oishidrink.com/sakura/app_install.php",
            "http://www.oishidrink.com/sakura/app_install.php",
    };



    public static int getInt(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        return sp.getInt(key, 1);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        int index = -1;
        if (key.equals(share_url)) {
            index = 0;
        }
        else if (key.equals(share_title)) {
            index = 1;
        }
        else if (key.equals(share_description)) {
            index = 2;
        }
        else if (key.equals(share_image)) {
            index = 3;
        }
        else if (key.equals(share_twitter_url)) {
            index = 4;
        }
        else if (key.equals(share_twitter_description)) {
            index = 5;
        }
        else if (key.equals(share_gplus_url)) {
            index = 6;
        }
        else if (key.equals(copy_url)) {
            index = 7;
        }

        return sp.getString(key, pages[index]);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("sakura", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }


}
