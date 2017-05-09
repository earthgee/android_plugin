package com.earthgee.library.util;

import android.os.IBinder;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/5/9.
 */
public class IDisplayManagerCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.hardware.display.IDisplayManager");
        }
        return sClass;
    }

    public static Object asInterface(IBinder iBinder) throws Exception{
        Class clazz=Class.forName("android.hardware.display.IDisplayManager$Stub");
        return MethodUtils.invokeStaticMethod(clazz,"asInterface",iBinder);
    }

}
