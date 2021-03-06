package com.earthgee.libaray.hook;

import android.content.Context;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public abstract class Hook {

    private boolean mEnable = false;

    protected Context mHostContext;
    protected BaseHookHandle mHookHandles;

    public void setEnable(boolean enable, boolean reInstallHook) {
        this.mEnable = enable;
    }

    public final void setEnable(boolean enable) {
        setEnable(enable, false);
    }

    public boolean isEnable() {
        return mEnable;
    }


    protected Hook(Context hostContext) {
        mHostContext = hostContext;
        mHookHandles = createHookHandle();
    }

    protected abstract BaseHookHandle createHookHandle();


    protected abstract void onInstall(ClassLoader classLoader) throws Throwable;

    protected void onUnInstall(ClassLoader classLoader) throws Throwable {

    }
}
