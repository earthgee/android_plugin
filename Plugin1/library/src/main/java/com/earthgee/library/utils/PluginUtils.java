package com.earthgee.library.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by zhaoruixuan on 2017/3/20.
 */
public class PluginUtils {

    public static PackageInfo getPackageInfo(Context context,String apkFilePath){
        PackageManager pm=context.getPackageManager();
        PackageInfo packageInfo=null;
        packageInfo=pm.getPackageArchiveInfo(apkFilePath,PackageManager.GET_ACTIVITIES|PackageManager.GET_SERVICES);
        return packageInfo;
    }

}
