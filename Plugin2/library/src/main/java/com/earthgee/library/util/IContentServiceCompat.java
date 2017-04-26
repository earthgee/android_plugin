package com.earthgee.library.util;

import android.os.IBinder;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/4/26.
 */
public class IContentServiceCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.content.IContentService");
        }
        return sClass;
    }

    public static Object asInterface(IBinder binder) throws Exception{
        Class clazz=Class.forName("android.content.IContentService$Stub");
        return MethodUtils.invokeStaticMethod(clazz,"asInterface",binder);
    }

}
