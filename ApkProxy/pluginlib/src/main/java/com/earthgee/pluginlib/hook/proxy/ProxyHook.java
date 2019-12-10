package com.earthgee.pluginlib.hook.proxy;

import android.content.Context;

import com.earthgee.pluginlib.hook.Hook;
import com.earthgee.pluginlib.hook.HookedMethodHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2019/12/10.
 */
public abstract class ProxyHook extends Hook implements InvocationHandler {

    protected Object mOldObj;

    protected ProxyHook(Context hostContext) {
        super(hostContext);
    }

    public void setOldObj(Object oldObj){
        this.mOldObj=oldObj;
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
            }
            return method.invoke(mOldObj,args);
        }catch (Exception e){

        }
        return null;
    }
}
