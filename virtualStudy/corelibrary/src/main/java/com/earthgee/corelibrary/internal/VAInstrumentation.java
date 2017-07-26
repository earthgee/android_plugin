package com.earthgee.corelibrary.internal;

import android.app.Instrumentation;
import android.os.Handler;
import android.os.Message;

import com.earthgee.corelibrary.PluginManager;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class VAInstrumentation extends Instrumentation implements Handler.Callback{

    private Instrumentation mBase;

    PluginManager mPluginManager;

    public VAInstrumentation(PluginManager pluginManager,Instrumentation base){
        this.mPluginManager=pluginManager;
        this.mBase=base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }



}

















