package com.earthgee.pluginlib.helper.compat;

import android.os.IBinder;

import com.earthgee.pluginlib.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by zhaoruixuan on 2019/12/9.
 */
public class IClipboardCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.content.IClipboard");
        }
        return sClass;
    }

    public static Object asInterface(IBinder binder) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class clazz=Class.forName("android.content.IClipboard$Stub");
        return MethodUtils.invokeStaticMethod(clazz,"asInterface",binder);
    }

}
