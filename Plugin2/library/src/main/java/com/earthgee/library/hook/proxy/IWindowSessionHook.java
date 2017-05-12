package com.earthgee.library.hook.proxy;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IWindowSessionInvokeHandle;

/**
 * Created by zhaoruixuan on 2017/5/12.
 */
public class IWindowSessionHook extends ProxyHook{

    public IWindowSessionHook(Context hostContext, Object oldObj) {
        super(hostContext);
        setOldObj(oldObj);
        mHookHandles=createHookHandle();
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IWindowSessionInvokeHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {

    }
}
