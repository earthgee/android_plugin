package com.earthgee.library.util;

import android.os.IBinder;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IWifiManagerCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.net.wifi.IWifiManager");
        }
        return sClass;
    }

    public static Object asInterface(IBinder iBinder) throws Exception{
        Class clazz=Class.forName("android.net.wifi.IWifiManager$Stub");
        return MethodUtils.invokeStaticMethod(clazz,"asInterface",iBinder);
    }

}















