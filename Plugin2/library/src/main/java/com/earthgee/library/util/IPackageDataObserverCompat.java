package com.earthgee.library.util;

/**
 * Created by zhaoruixuan on 2017/5/2.
 */
public class IPackageDataObserverCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.content.pm.IPacakgeDataObserver");
        }
        return sClass;
    }

    public static boolean isIPackageDataObserver(Object obj) throws ClassNotFoundException{
        if(obj==null){
            return false;
        }else{
            Class clazz=Class();
            return clazz.isInstance(obj);
        }
    }

}



























