package com.earthgee.pluginlib.hook;

import android.content.Context;

public abstract class Hook {

    private boolean mEnable=false;

    protected Context mHostContext;
    protected BaseHookHandle mHookHandles;

    public final void setEnable(boolean enable) {
        this.mEnable = enable;
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












