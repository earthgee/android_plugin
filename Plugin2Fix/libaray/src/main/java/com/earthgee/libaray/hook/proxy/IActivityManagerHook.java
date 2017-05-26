package com.earthgee.libaray.hook.proxy;

import android.content.Context;
import android.util.AndroidRuntimeException;

import com.earthgee.libaray.helper.ActivityManagerNativeCompat;
import com.earthgee.libaray.helper.IActivityManagerCompat;
import com.earthgee.libaray.helper.MyProxy;
import com.earthgee.libaray.helper.SingletonCompat;
import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.handle.IActivityManagerHookHandle;
import com.earthgee.libaray.reflect.FieldUtils;
import com.earthgee.libaray.reflect.Utils;

import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/26.
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
            Class[] ifs=interfaces!=null&&interfaces.size()>0?
                    interfaces.toArray(new Class[interfaces.size()]):new Class[0];
            Object proxiedActivityManager= MyProxy.newProxyInstance(objClass.getClassLoader(),ifs,this);
            FieldUtils.writeStaticField(cls,"gDefault",proxiedActivityManager);
        }else if (SingletonCompat.isSingleton(obj)) {
            Object obj1 = FieldUtils.readField(obj, "mInstance");
            if (obj1 == null) {
                SingletonCompat.get(obj);
                obj1 = FieldUtils.readField(obj, "mInstance");
            }
            setOldObj(obj1);
            List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
            Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
            final Object object = MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(), ifs, IActivityManagerHook.this);
            Object iam1 = ActivityManagerNativeCompat.getDefault();

            //这里先写一次，防止后面找不到Singleton类导致的挂钩子失败的问题。
            FieldUtils.writeField(obj, "mInstance", object);

            Object iam2 = ActivityManagerNativeCompat.getDefault();
            // 方式2
            if (iam1 == iam2) {
                //这段代码是废的，没啥用，写这里只是不想改而已。
                FieldUtils.writeField(obj, "mInstance", object);
            }
        } else {
            throw new AndroidRuntimeException("Can not install IActivityManagerNative hook");
        }
    }
}
















