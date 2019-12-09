package com.earthgee.pluginlib.core;

import android.content.Context;

import com.earthgee.pluginlib.hook.HookFactory;

public class PluginProcessManager {

    public static void installHook(Context hostContext) throws Exception{
        HookFactory.getInstance().installHook(hostContext,null);
    }

}
