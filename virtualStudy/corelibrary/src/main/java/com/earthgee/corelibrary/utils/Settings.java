package com.earthgee.corelibrary.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

/**
 * Created by zhaoruixuan on 2017/7/25.
 */
public class Settings {

    private static final String FILE_NAME="VirtualApk_Settings";

    public static void setSoVersion(Context context,String name,int version){
        SharedPreferences preferences=context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt(name,version);
        editor.commit();
    }

    public static int getSoVersion(Context context,String name){
        SharedPreferences preferences=context.getSharedPreferences(FILE_NAME,Context.MODE_PRIVATE);
        return preferences.getInt(name,0);
    }

}




















