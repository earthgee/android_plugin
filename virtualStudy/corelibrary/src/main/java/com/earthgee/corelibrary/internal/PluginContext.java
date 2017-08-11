package com.earthgee.corelibrary.internal;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * Created by zhaoruixuan on 2017/7/25.
 */
public class PluginContext extends ContextWrapper{

    private final LoadedPlugin mPlugin;

    public PluginContext(LoadedPlugin plugin) {
        super(plugin.getPluginManager().getHostContext());
        this.mPlugin=plugin;
    }

    private Context getHostContext(){
        return getBaseContext();
    }

    @Override
    public ContentResolver getContentResolver() {
        return new PluginContentResolver(getHostContext());
    }

    @Override
    public String getPackageName() {
        return mPlugin.getPackageName();
    }

    @Override
    public Resources getResources() {
        return this.mPlugin.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return this.mPlugin.getAssets();
    }

    @Override
    public void startActivity(Intent intent) {
        ComponentsHandler componentsHandler=mPlugin.getPluginManager().getComponentsHandler();
        componentsHandler.transformIntentToExplicitAsNeeded(intent);
        super.startActivity(intent);
    }
}
