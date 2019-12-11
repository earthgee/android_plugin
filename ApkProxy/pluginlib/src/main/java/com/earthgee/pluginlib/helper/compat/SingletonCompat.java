package com.earthgee.pluginlib.helper.compat;

import com.earthgee.pluginlib.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

public class SingletonCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.util.Singleton");
        }
        return sClass;
    }

    public static boolean isSingleton(Object obj) {
        if (obj == null) {
            return false;
        } else {
            try {
                Class clazz = Class();
                return clazz.isInstance(obj);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    public static Object get(Object targetSingleton) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeMethod(targetSingleton,"get");
    }

}
