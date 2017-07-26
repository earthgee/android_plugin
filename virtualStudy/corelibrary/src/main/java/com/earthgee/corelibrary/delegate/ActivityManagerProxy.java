package com.earthgee.corelibrary.delegate;

import android.app.ActivityManager;
import android.app.IActivityManager;

import com.earthgee.corelibrary.PluginManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class ActivityManagerProxy implements InvocationHandler{

    public static IActivityManager newInstance(PluginManager pluginManager,IActivityManager activityManager){
        return (IActivityManager) Proxy.newProxyInstance(activityManager.getClass().getClassLoader(),
                new Class[] {IActivityManager.class}, new ActivityManagerProxy(pluginManager,activityManager));
    }

    private PluginManager mPluginManager;
    private IActivityManager mActivityManager;

    public ActivityManagerProxy(PluginManager pluginManager,IActivityManager activityManager){
        this.mPluginManager=pluginManager;
        this.mActivityManager=activityManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}













