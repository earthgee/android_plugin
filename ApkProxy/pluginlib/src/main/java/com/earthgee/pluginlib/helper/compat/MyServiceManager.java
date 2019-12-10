package com.earthgee.pluginlib.helper.compat;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public class MyServiceManager {

    private static Map<String, IBinder> mOriginServiceCache=new HashMap<>();
    private static Map<String,IBinder> mProxiedServiceCache=new HashMap<>();
    private static Map<String,Object> mProxiedObjCache=new HashMap<>();

    public static IBinder getOriginService(String serviceName){
        return mOriginServiceCache.get(serviceName);
    }

    public static void addOriginService(String serviceName,IBinder service){
        mOriginServiceCache.put(serviceName,service);
    }

    public static void addProxiedServiceCache(String serviceName,IBinder proxyService){
        mProxiedServiceCache.put(serviceName,proxyService);
    }

    public static Object getProxiedObj(String serviceName){
        return mProxiedObjCache.get(serviceName);
    }

    public static void addProxiedObj(String serviceName,Object obj){
        mProxiedObjCache.put(serviceName,obj);
    }

}


















