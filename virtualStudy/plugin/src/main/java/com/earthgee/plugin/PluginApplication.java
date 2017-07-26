package com.earthgee.plugin;

import android.app.Application;
import android.util.Log;

/**
 * Created by zhaoruixuan on 2017/7/26.
 */
public class PluginApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("earthgee2","plugin application onCreate");
    }

}
