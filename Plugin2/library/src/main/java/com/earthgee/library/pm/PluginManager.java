package com.earthgee.library.pm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;

import com.earthgee.library.BuildConfig;

/**
 * Created by zhaoruixuan on 2017/4/12.
 */
public class PluginManager {

    public static final String AUTHORITY_NAME= BuildConfig.AUTHORITY_NAME;

    private static PluginManager instance=null;

    public static PluginManager getInstance(){
        if(instance==null){
            instance=new PluginManager();
        }
        return instance;
    }

    public ActivityInfo getActivityInfo(ComponentName className,int flags) throws Exception{
            if(className==null){
                return null;
            }

            return null;
    }

}
