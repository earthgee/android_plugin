package com.earthgee.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by zhaoruixuan on 2017/8/7.
 */
public class PluginService2 extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        Log.d("earthgee","remote service onCreate");
        super.onCreate();
    }

}
