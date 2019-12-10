package com.earthgee.pluginlib.hook.binder;

import android.content.Context;

import com.earthgee.pluginlib.helper.MyProxy;
import com.earthgee.pluginlib.helper.compat.MyServiceManager;
import com.earthgee.pluginlib.hook.BaseHookHandle;
import com.earthgee.pluginlib.hook.Hook;
import com.earthgee.pluginlib.hook.HookedMethodHandler;
import com.earthgee.pluginlib.reflect.Utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

abstract class BinderHook extends Hook implements InvocationHandler {

    private Object mOldObj;

    protected BinderHook(Context hostContext) {
        super(hostContext);
    }

    abstract Object getOldObj() throws Exception;

    void setOldObj(Object mOldObj) {
        this.mOldObj = mOldObj;
    }

    public abstract String getServiceName();

    @Override
    protected void onInstall(ClassLoader classLoader) throws Exception {
        new ServiceManagerCacheBinderHook(mHostContext,getServiceName()).onInstall(classLoader);
        mOldObj=getOldObj();
        Class<?> clazz=mOldObj.getClass();
        List<Class<?>> interfaces= Utils.getAllInterfaces(clazz);
        Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
        Object proxiedObj= MyProxy.newProxyInstance(clazz.getClassLoader(),ifs,this);
        MyServiceManager.addProxiedObj(getServiceName(),proxiedObj);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try{
            if(!isEnable()){
                return method.invoke(mOldObj,args);
            }
            HookedMethodHandler hookedMethodHandler=mHookHandles.getHookedMethodHandler(method);
            if(hookedMethodHandler!=null){
                return hookedMethodHandler.doHookInner(mOldObj,method,args);
            }else{
                return method.invoke(mOldObj,args);
            }
        }catch (Exception e){

        }
        return null;
    }

}











