package com.earthgee.library.hook.binder;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class MyServiceManager {

    private static Map<String,IBinder> mOriginServiceCache=new HashMap<>(1);
    private static Map<String,IBinder> mProxiedServiceCache=new HashMap<>(1);
    private static Map<String,Object> mProxiedObjCache=new HashMap<>(1);

    static IBinder getOriginService(String servicename){
        return mOriginServiceCache.get(servicename);
    }

    public static void addOriginService(String servicename,IBinder service){
        mOriginServiceCache.put(servicename,service);
    }

    static void addProxiedServiceCache(String servicename,IBinder proxyService){
        mProxiedServiceCache.put(servicename, proxyService);
    }

    static Object getProxiedObj(String servicename){
        return mProxiedObjCache.get(servicename);
    }

    static void addProxiedObj(String servicename,Object obj){
        mProxiedObjCache.put(servicename,obj);
    }

}


















