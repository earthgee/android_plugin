package com.earthgee.libaray;

import android.app.Application;
import android.content.Context;

import com.earthgee.libaray.core.PluginDirHelper;
import com.earthgee.libaray.pm.PluginManager;

/**
 * Created by zhaoruixuan on 2017/5/25.
 */
public class PluginApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        PluginHelper.getInstance().applicationOnCreate(getBaseContext());
        //todo
        //先放在这里
        PluginManager.getInstance().init(getBaseContext());
    }

    @Override
    protected void attachBaseContext(Context base) {
        PluginHelper.getInstance().applicationAttachBaseContext(base);
        super.attachBaseContext(base);
    }

}





















