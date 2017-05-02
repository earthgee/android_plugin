package com.earthgee.library.util;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/5/2.
 */
public class SingletonCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.util.Singleton");
        }
        return sClass;
    }

    public static boolean isSingleton(Object obj){
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

    public static Object get(Object targetSingleton) throws Exception{
        return MethodUtils.invokeMethod(targetSingleton,"get");
    }

}




















