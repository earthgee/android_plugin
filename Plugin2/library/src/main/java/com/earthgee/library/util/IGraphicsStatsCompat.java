package com.earthgee.library.util;

import android.os.IBinder;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IGraphicsStatsCompat {

    private static Class sClass;

    public static Class Class() throws Exception{
        if(sClass==null){
            sClass=Class.forName("android.view.IGraphicsStats");
        }
        return sClass;
    }

    public static Object asInterface(IBinder binder) throws Exception{
        Class clazz=Class.forName("android.view.IGraphicsStats$Stub");
        return MethodUtils.invokeStaticMethod(clazz,"asInterface",binder);
    }

}
