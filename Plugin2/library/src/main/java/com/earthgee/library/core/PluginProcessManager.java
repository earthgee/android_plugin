package com.earthgee.library.core;

import android.content.Context;

import com.earthgee.library.hook.HookFactory;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class PluginProcessManager {

    public static void installHook(Context hostContext) throws Throwable{
        HookFactory.getInstance().installHook(hostContext,null);
    }

}
