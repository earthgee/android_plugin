package com.earthgee.libaray.core;

import android.content.Context;

import com.earthgee.libaray.hook.HookFactory;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class PluginProcessManager {

    public static void installHook(Context hostContext) throws Exception {
        HookFactory.getInstance().installHook(hostContext, null);
    }

    public static void setHookEnable(boolean enable) {
        HookFactory.getInstance().setHookEnable(enable);
    }

    public static void setHookEnable(boolean enable, boolean reinstallHook) {
        HookFactory.getInstance().setHookEnable(enable, reinstallHook);
    }

}
