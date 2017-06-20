package com.earthgee.multidex;

import android.app.Application;
import android.content.Context;

import com.earthgee.mutlidex.Multidex;

/**
 * Created by zhaoruixuan on 2017/6/19.
 */
public class TestApplication extends Application{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Multidex.install(base);
    }

}
