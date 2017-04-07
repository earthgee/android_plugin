package com.earthgee.library;

import android.app.Application;
import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/4/6.
 * 侵入式的application，不使用这个application也可以使用提供的api
 */
public class PluginApplication extends Application{

    @Override
    protected void attachBaseContext(Context base) {
        PluginHelper.getInstance().applicationAttachBaseContext(base);
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PluginHelper.getInstance().applicationOnCreate(getBaseContext());
    }

}
