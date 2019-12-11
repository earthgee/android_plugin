package com.earthgee.pluginlib.hook.proxy;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.earthgee.pluginlib.helper.MyProxy;
import com.earthgee.pluginlib.helper.compat.ActivityManagerNativeCompat;
import com.earthgee.pluginlib.helper.compat.IActivityManagerCompat;
import com.earthgee.pluginlib.helper.compat.SingletonCompat;
import com.earthgee.pluginlib.hook.BaseHookHandle;
import com.earthgee.pluginlib.reflect.FieldUtils;
import com.earthgee.pluginlib.reflect.Utils;

import java.util.List;

/**
 * Created by zhaoruixuan on 2019/12/10.
 */
public class IActivityManagerHook extends ProxyHook{

    public IActivityManagerHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IActivityManagerHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Exception {
        Class cls= ActivityManagerNativeCompat.Class();
        Object obj=FieldUtils.readStaticField(cls,"gDefault");
        if(obj==null){
            ActivityManagerNativeCompat.getDefault();
            obj=FieldUtils.readStaticField(cls,"gDefault");
        }

        if(IActivityManagerCompat.isIActivityManager(obj)){
            setOldObj(obj);
            Class<?> objClass=mOldObj.getClass();
            List<Class<?>> interfaces= Utils.getAllInterfaces(objClass);
            Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
            Object proxiedActivityManager= MyProxy.newProxyInstance(objClass.getClassLoader(),ifs,this);
            FieldUtils.writeStaticField(cls,"gDefault",proxiedActivityManager);
        }else if(SingletonCompat.isSingleton(obj)){
            Object obj1=FieldUtils.readField(obj,"mInstance");
            if(obj1==null){
                SingletonCompat.get(obj);
                obj1=FieldUtils.readField(obj,"mInstance");
            }
            setOldObj(obj);
            List<Class<?>> interfaces= Utils.getAllInterfaces(mOldObj.getClass());
            Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
            Object object= MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(),ifs,this);
            Object iam1=ActivityManagerNativeCompat.getDefault();

            FieldUtils.writeField(obj,"mInstance",object);


        }
    }



}
