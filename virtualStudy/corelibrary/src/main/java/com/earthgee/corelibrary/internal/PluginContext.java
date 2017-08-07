package com.earthgee.corelibrary.internal;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

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

    @Override
    public void startActivity(Intent intent) {
        ComponentsHandler componentsHandler=mPlugin.getPluginManager().getComponentsHandler();
        componentsHandler.transformIntentToExplicitAsNeeded(intent);
        super.startActivity(intent);
    }
}
