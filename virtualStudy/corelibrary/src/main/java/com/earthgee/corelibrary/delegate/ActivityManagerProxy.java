package com.earthgee.corelibrary.delegate;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.utils.PluginUtil;

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
        if("startService".equals(method.getName())){
            try{
                return startService(proxy,method,args);
            }catch (Throwable e){
            }
        }

        try{
            return method.invoke(mActivityManager,args);
        }catch (Throwable th){
            Throwable c=th.getCause();
            //todo

            Throwable cause=th;

            throw c!=null?c:th;
        }
    }

    private Object startService(Object proxy,Method method,Object[] args) throws Throwable{
        IApplicationThread appThread= (IApplicationThread) args[0];
        Intent target= (Intent) args[1];
        ResolveInfo resolveInfo=this.mPluginManager.resolveService(target,0);
        if(null==resolveInfo||null==resolveInfo.serviceInfo){
            return method.invoke(this.mActivityManager, args);
        }

        return startDelegateServiceForTarget(target,resolveInfo.serviceInfo,null,LocalService.EXTRA_COMMAND_START_SERVICE);
    }

    private ComponentName startDelegateServiceForTarget(Intent target, ServiceInfo serviceInfo, Bundle extras,int command){
        Intent wrapperIntent=wrapperTargetIntent(target,serviceInfo,extras,command);
        return mPluginManager.getHostContext().startService(wrapperIntent);
    }

    private Intent wrapperTargetIntent(Intent target,ServiceInfo serviceInfo,Bundle extras,int command){
        target.setComponent(new ComponentName(serviceInfo.packageName,serviceInfo.name));
        String pluginLocation=mPluginManager.getLoadedPlugin(target.getComponent()).getLocation();

        boolean local= PluginUtil.isLocalService(serviceInfo);
        Class<? extends Service> delegate=local?LocalService.class:RemoteService.class;
        Intent intent=new Intent();
        intent.setClass(mPluginManager.getHostContext(),delegate);
        intent.putExtra(LocalService.EXTRA_TARGET,target);
        intent.putExtra(LocalService.EXTRA_COMMAND,command);
        intent.putExtra(LocalService.EXTRA_PLUGIN_LOCATION,pluginLocation);
        if(extras!=null){
            intent.putExtras(extras);
        }

        return intent;
    }

}













