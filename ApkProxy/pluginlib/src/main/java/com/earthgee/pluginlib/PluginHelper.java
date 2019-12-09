package com.earthgee.pluginlib;

import android.content.Context;

import com.earthgee.pluginlib.core.PluginProcessManager;

public class PluginHelper {

    private static PluginHelper sInstance = null;

    private Context mContext;

    private PluginHelper() {
    }

    public static final PluginHelper getInstance() {
        if (sInstance == null) {
            sInstance = new PluginHelper();
        }
        return sInstance;
    }

    public void applicationOnCreate(final Context baseContext) {
        mContext = baseContext;
        initPlugin(baseContext);
    }

    private void initPlugin(Context baseContext){
        try{
            PluginProcessManager.installHook(baseContext);
        }catch (Exception e){
        }
    }

}











