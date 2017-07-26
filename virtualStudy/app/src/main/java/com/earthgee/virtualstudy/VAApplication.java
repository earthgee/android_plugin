package com.earthgee.virtualstudy;

import android.app.Application;
import android.content.Context;

import com.earthgee.corelibrary.PluginManager;

/**
 * Created by zhaoruixuan on 2017/7/26.
 */
public class VAApplication extends Application{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PluginManager.getInstance(base).init();
    }

}
