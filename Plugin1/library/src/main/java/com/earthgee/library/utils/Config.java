package com.earthgee.library.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhaoruixuan on 2017/3/17.
 */
public class Config {

    public static final boolean LOG=true;

    public static ClassLoader sPluginClassLoader=Constants.class.getClassLoader();

    public static void setSoLastModifiedTime(Context context,String soName,long time){
        SharedPreferences prefs=context.getSharedPreferences(Constants.PREFERENCE_NAME,
                Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
        prefs.edit().putLong(soName,time).apply();
    }

    public static long getSoLastModifiedTime(Context context,String soName){
        SharedPreferences prefs=context.getSharedPreferences(Constants.PREFERENCE_NAME,
                Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
        return prefs.getLong(soName,0);
    }

}
