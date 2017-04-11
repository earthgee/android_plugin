package com.earthgee.library.util;

import android.os.IBinder;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class ServiceManagerCompat {

    private static Class sClass=null;

    public static final Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.os.ServiceManager");
        }
        return sClass;
    }

    public static IBinder getService(String name) throws ClassNotFoundException{
        return 
    }

}
