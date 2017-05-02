package com.earthgee.library.util;

/**
 * Created by zhaoruixuan on 2017/5/2.
 */
public class ParceledListSliceCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.content.pm.ParceledListSlice");
        }
        return sClass;
    }

    public static boolean isParceledListSlice(Object obj){
        if(obj==null){
            return false;
        }else{
            try{
                Class clazz=Class();
                return clazz.isInstance(obj);
            }catch (ClassNotFoundException e){
                return false;
            }
        }
    }

}
