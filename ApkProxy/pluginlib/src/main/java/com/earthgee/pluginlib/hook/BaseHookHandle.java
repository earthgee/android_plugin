package com.earthgee.pluginlib.hook;

import android.content.Context;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseHookHandle {

    protected Context mHostContext;

    protected Map<String,HookedMethodHandler> sHookMethodHandlers=new HashMap<>(5);

    public BaseHookHandle(Context hostContext) {
        mHostContext = hostContext;
        init();
    }

    protected abstract void init();

    public Set<String> getHookedMethodNames(){
        return sHookMethodHandlers.keySet();
    }

    public HookedMethodHandler getHookedMethodHandler(Method method) {
        if (method != null) {
            return sHookedMethodHandlers.get(method.getName());
        } else {
            return null;
        }
    }

    protected Class<?> getHookedClass() throws ClassNotFoundException {
        return null;
    }

    protected HookedMethodHandler newBaseHandler() throws ClassNotFoundException {
        return null;
    }

    protected void addAllMethodFromHookedClass(){
        try{
            Class clazz=getHookedClass();
            if(clazz!=null){
                Method[] methods=clazz.getDeclaredMethods();
                if(methods!=null&&methods.length>0){
                    for(Method method:methods){
                       if(Modifier.isPublic(method.getModifiers())&&!sHookMethodHandlers.containsKey(method.getName())){
                           sHookMethodHandlers.put(method.getName(),newBaseHandler());
                       }
                    }
                }
            }
        }catch (Exception e){

        }
    }

}
