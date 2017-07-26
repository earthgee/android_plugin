package com.earthgee.corelibrary.internal;

import android.content.Context;
import android.content.ContextWrapper;

/**
 * Created by zhaoruixuan on 2017/7/25.
 */
public class PluginContext extends ContextWrapper{

    public PluginContext(LoadedPlugin plugin) {
        super(plugin.getPluginManager().getHostContext());

    }

}
