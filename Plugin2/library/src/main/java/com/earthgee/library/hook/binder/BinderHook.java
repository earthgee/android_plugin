package com.earthgee.library.hook.binder;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
abstract class BinderHook extends Hook implements InvocationHandler{

    private Object mOldObj;

    protected BinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        new ServiceManagerCacheBinderHook(mHostContext,getServiceName()).onInstall(classLoader);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }

    public abstract String getServiceName();

}
