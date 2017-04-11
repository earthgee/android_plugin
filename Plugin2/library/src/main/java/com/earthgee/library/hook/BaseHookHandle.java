package com.earthgee.library.hook;

import android.content.Context;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public abstract class BaseHookHandle {

    protected Context mHostContext;

    protected Map<String,HookedMethodHandler> sHookedMethodHandlers=
            new HashMap<>(5);

    public BaseHookHandle(Context hostContext){
        mHostContext=hostContext;
        init();
    }

    protected abstract void init();

    public HookedMethodHandler getHookedMethodHandler(Method method){
        if(method!=null){
            return sHookedMethodHandlers.get(method.getName());
        }else{
            return null;
        }
    }

    public Set<String> getHookedMethodNames(){
        return sHookedMethodHandlers.keySet();
    }

    protected Class<?> getHookedClass() throws ClassNotFoundException{
        return null;
    }

    protected HookedMethodHandler newBaseHandler() throws ClassNotFoundException{
        return null;
    }

    protected void addAllMethodFromHookedClass(){
        try{
            Class clazz=getHookedClass();
            if(clazz!=null){
                Method[] methods=clazz.getDeclaredMethods();
                if(methods!=null&&methods.length>0){
                    for(Method method:methods){
                        if(Modifier.isPublic(method.getModifiers())&&
                                !sHookedMethodHandlers.containsKey(method.getName())){
                            sHookedMethodHandlers.put(method.getName(),newBaseHandler());
                        }
                    }
                }
            }
        }catch (ClassNotFoundException e){
        }
    }

}





















