package com.earthgee.library.hook.binder;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.reflect.FieldUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class ServiceManagerCacheBinderHook extends Hook implements InvocationHandler{

    private String mServiceName;

    protected ServiceManagerCacheBinderHook(Context hostContext,String servicename) {
        super(hostContext);
        mServiceName=servicename;
        setEnable(true);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        Object sCacheObj= FieldUtils.readStaticField()
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return new ServiceManagerHookHandle(mHostContext);
    }
}
