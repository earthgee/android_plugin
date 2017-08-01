package com.earthgee.corelibrary.internal;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by zhaoruixuan on 2017/7/25.
 */
public class PluginContext extends ContextWrapper{

    private final LoadedPlugin mPlugin;

    public PluginContext(LoadedPlugin plugin) {
        super(plugin.getPluginManager().getHostContext());
        this.mPlugin=plugin;
    }

    @Override
    public String getPackageName() {
        return mPlugin.getPackageName();
    }
}
