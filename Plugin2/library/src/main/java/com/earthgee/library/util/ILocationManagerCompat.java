package com.earthgee.library.util;

import android.os.IBinder;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ILocationManagerCompat {

    public static Class sClass;

    public static Class Class() throws Exception{
        if(sClass==null){
            sClass=Class.forName("android.location.ILocationManager");
        }
        return sClass;
    }

    public static Object asInterface(IBinder binder) throws Exception{
        Class clazz=Class.forName("android.location.ILocationManager$Stub");
        return MethodUtils.invokeStaticMethod(clazz,"asInterface",binder);
    }

}
