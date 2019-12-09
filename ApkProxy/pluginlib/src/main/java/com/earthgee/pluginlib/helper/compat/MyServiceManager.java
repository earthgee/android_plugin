package com.earthgee.pluginlib.helper.compat;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public class MyServiceManager {

    private static Map<String, IBinder> mOriginServiceCache=new HashMap<>();

    static IBinder getOriginService(String serviceName){
        return mOriginServiceCache.get(serviceName);
    }

    public static void addOriginService(String serviceName,IBinder service){
        mOriginServiceCache.put(serviceName,service);
    }

}


















