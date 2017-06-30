package com.earthgee.library;

import java.lang.reflect.Field;

/**
 * Created by zhaoruixuan on 2017/6/30.
 */
public class ReflectionUtils {

    public static Object getFiled(Object obj,Class<?> cl,String field) throws Exception{
        Field localField=cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }

    public static void setField(Object obj,Class<?> cl,String field,Object value) throws Exception{
        Field localField=cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj,value);
    }

}























