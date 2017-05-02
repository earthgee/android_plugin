package com.earthgee.library.hook.proxy;

import android.content.Context;
import android.content.pm.PackageManager;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IPackageManagerHookHandle;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.Utils;
import com.earthgee.library.util.ActivityThreadCompat;
import com.earthgee.library.util.MyProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IPackageManagerHook extends ProxyHook{
    public IPackageManagerHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IPackageManagerHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        Object currentActivityThread= ActivityThreadCompat.currentActivityThread();
        setOldObj(FieldUtils.readField(currentActivityThread,"sPackageManager"));
        Class<?> iPmClass=mOldObj.getClass();
        List<Class<?>> interfaces= Utils.getAllInterfaces(iPmClass);
        Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
        Object newPm= MyProxy.newProxyInstance(iPmClass.getClassLoader(),ifs,this);
        FieldUtils.writeField(currentActivityThread,"sPackageManager",newPm);
        PackageManager pm=mHostContext.getPackageManager();
        Object mPM=FieldUtils.readField(pm,"mPM");
        if(mPM!=newPm){
            FieldUtils.writeField(pm,"mPM",newPm);
        }
    }

    public static void fixContextPackageManager(Context context){
        try{
            Object currentActivityThread=ActivityThreadCompat.currentActivityThread();
            Object newPm=FieldUtils.readField(currentActivityThread,"sPackageManager");
            PackageManager pm=context.getPackageManager();
            Object mPM=FieldUtils.readField(pm,"mPM");
            if(mPM!=newPm){
                FieldUtils.writeField(pm,"mPM",newPm);
            }
        }catch (Exception e){
        }
    }

}
