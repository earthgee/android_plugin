package com.earthgee.nuwa;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.earthgee.library.Nuwa;

/**
 * Created by zhaoruixuan on 2017/7/22.
 */
public class NuwaApplication extends Application{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Nuwa.init(this);
        Nuwa.loadPatch(this, Environment.getExternalStorageDirectory().
                getAbsolutePath().concat("/classes.dex"));
    }
}













