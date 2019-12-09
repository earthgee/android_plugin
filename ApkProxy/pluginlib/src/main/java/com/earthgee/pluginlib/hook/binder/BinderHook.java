package com.earthgee.pluginlib.hook.binder;

import android.content.Context;

import com.earthgee.pluginlib.hook.BaseHookHandle;
import com.earthgee.pluginlib.hook.Hook;

abstract class BinderHook extends Hook {

    private Object mOldObj;

    protected BinderHook(Context hostContext) {
        super(hostContext);
    }

    abstract Object getOldObj() throws Exception;

    void setOldObj(Object mOldObj) {
        this.mOldObj = mOldObj;
    }

    public abstract String getServiceName();

    @Override
    protected void onInstall(ClassLoader classLoader) throws Exception {
        //new Service
    }

}











