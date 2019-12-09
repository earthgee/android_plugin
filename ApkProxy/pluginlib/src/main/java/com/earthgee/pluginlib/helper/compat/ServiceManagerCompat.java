package com.earthgee.pluginlib.helper.compat;

import android.os.IBinder;

import com.earthgee.pluginlib.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

public class ServiceManagerCompat {

    private static Class sClass=null;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.os.ServiceManager");
        }
        return sClass;
    }

    public static IBinder getService(String name) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return (IBinder) MethodUtils.invokeStaticMethod(Class(),"getService",name);
    }

}
