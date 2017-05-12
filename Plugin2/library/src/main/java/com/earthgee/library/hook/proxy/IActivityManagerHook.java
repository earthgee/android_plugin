package com.earthgee.library.hook.proxy;

import android.content.Context;
import android.util.AndroidRuntimeException;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IActivityManagerHookHandle;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.Utils;
import com.earthgee.library.util.ActivityManagerNativeCompat;
import com.earthgee.library.util.IActivityManagerCompat;
import com.earthgee.library.util.MyProxy;
import com.earthgee.library.util.SingletonCompat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/2.
 * 对ams的hook
 * 对ActivityManagerNative的gDefault变量进行hook
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
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try{
            return super.invoke(proxy, method, args);
        }catch (SecurityException e){
            String msg=String.format("msg[%s],args[%s]",e.getMessage(), Arrays.toString(args));
            SecurityException e1=new SecurityException(msg);
            e1.initCause(e);
            throw e1;
        }
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        Class cls= ActivityManagerNativeCompat.Class();
        Object obj= FieldUtils.readStaticField(cls,"gDefault");
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
            setOldObj(obj1);
            List<Class<?>> interfaces=Utils.getAllInterfaces(mOldObj.getClass());
            Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
            final Object object=MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(),ifs,this);
            Object iam1=ActivityManagerNativeCompat.getDefault();

            FieldUtils.writeField(obj,"mInstance",object);
        }else{
            throw new AndroidRuntimeException("Can not install IActivityManagerNative hook");
        }
    }

}
















