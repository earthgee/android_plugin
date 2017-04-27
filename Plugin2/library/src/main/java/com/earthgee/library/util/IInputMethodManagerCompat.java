package com.earthgee.library.util;

import android.os.IBinder;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IInputMethodManagerCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("com.android.internal.view.IInputMethodManager");
        }
        return sClass;
    }

    public static Object asInterface(IBinder binder) throws Exception{
        Class clazz=Class.forName("com.android.internal.view.IInputMethodManager$Stub");
        return MethodUtils.invokeStaticMethod(clazz,"asInterface",binder);
    }

}
