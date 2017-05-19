package com.earthgee.library.util;

import com.earthgee.library.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/5/19.
 */
public class ProcessCompat {

    public static final void setArgV0(String name){
        try{
            MethodUtils.invokeStaticMethod(Class.forName("android.os.Process"),"setArgV0",name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
