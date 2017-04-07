package com.earthgee.library.util;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/4/6.
 */
public class SystemPropertiesCompat {

    private static Class<?> sClass;

    private static Class getMyClass() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.os.SystemProperties");
        }
        return sClass;
    }

    private static String getInner(String key,String defaultValue) throws Exception{
        Class clazz=getMyClass();
        return (String)MethodUtils.invokeStaticMethod(clazz,"get",key,defaultValue);
    }

    public static String get(String key,String defaultValue){
        try{
            return getInner(key,defaultValue);
        }catch (Exception e){
            e.printStackTrace();
            return defaultValue;
        }
    }

}
