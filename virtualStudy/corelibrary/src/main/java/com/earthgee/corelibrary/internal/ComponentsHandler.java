package com.earthgee.corelibrary.internal;

import android.content.Context;

import com.earthgee.corelibrary.PluginManager;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class ComponentsHandler {

    private PluginManager mPluginManager;
    private Context mContext;

    public ComponentsHandler(PluginManager pluginManager){
        mPluginManager=pluginManager;
        mContext=pluginManager.getHostContext();
    }

}
