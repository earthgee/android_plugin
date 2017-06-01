package com.earthgee.libaray.hook;

import android.content.Context;

import com.earthgee.libaray.hook.proxy.IActivityManagerHook;
import com.earthgee.libaray.hook.proxy.IPackageManagerHook;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class HookFactory {

    private static HookFactory sInstance = null;

    private HookFactory() {
    }

    public static HookFactory getInstance() {
        synchronized (HookFactory.class) {
            if (sInstance == null) {
                sInstance = new HookFactory();
            }
        }
        return sInstance;
    }

    private List<Hook> mHookList = new ArrayList<Hook>(3);

    public void setHookEnable(boolean enable) {
        synchronized (mHookList) {
            for (Hook hook : mHookList) {
                hook.setEnable(enable);
            }
        }
    }

    public void setHookEnable(boolean enable, boolean reinstallHook) {
        synchronized (mHookList) {
            for (Hook hook : mHookList) {
                hook.setEnable(enable, reinstallHook);
            }
        }
    }

    public void installHook(Hook hook, ClassLoader cl) {
        try {
            hook.onInstall(cl);
            synchronized (mHookList) {
                mHookList.add(hook);
            }
        } catch (Throwable throwable) {
        }
    }

    public final void installHook(Context context, ClassLoader classLoader) throws Exception {
        installHook(new IPackageManagerHook(context),classLoader);
        installHook(new IActivityManagerHook(context),classLoader);
        installHook(new PluginCallbackHook(context),classLoader);
    }

}
















