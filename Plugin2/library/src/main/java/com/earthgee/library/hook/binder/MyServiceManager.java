package com.earthgee.library.hook.binder;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class MyServiceManager {

    //保存所有未hook情况下取得的service IBinder引用对象
    private static Map<String,IBinder> mOriginServiceCache=new HashMap<>(1);
    //对mOriginServiceCache中binder引用对象做动态代理所得对象(后续不使用)
    private static Map<String,IBinder> mProxiedServiceCache=new HashMap<>(1);
    //正常情况下获得的binder代理对象做的动态代理对象
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


















