package com.earthgee.library.util;

import android.os.IBinder;

import com.earthgee.library.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ISmsCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("com.android.internal.telephony.ISms");
        }
        return sClass;
    }

    public static Object asInterface(IBinder binder) throws Exception {
        Class clazz = Class.forName("com.android.internal.telephony.ISms$Stub");
        return MethodUtils.invokeStaticMethod(clazz, "asInterface", binder);
    }

}
