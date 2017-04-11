package com.earthgee.library.hook.binder;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.reflect.Utils;
import com.earthgee.library.util.MyProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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
        mOldObj=getOldObj();
        Class<?> clazz=mOldObj.getClass();
        List<Class<?>> interfaces= Utils.getAllInterfaces(clazz);
        Class[] ifs=interfaces!=null&&interfaces.size()>0?
                interfaces.toArray(new Class[interfaces.size()]):new Class[0];
        Object proxiedObj= MyProxy.newProxyInstance(clazz.getClassLoader(),ifs,this);
        MyServiceManager.addProxiedObj(getServiceName(),proxiedObj);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }

    public abstract String getServiceName();

    abstract Object getOldObj() throws Exception;

    void setOldObj(Object mOldObj){
        this.mOldObj=mOldObj;
    }

}
