package com.earthgee.library.util;

import com.earthgee.library.reflect.FieldUtils;

/**
 * Created by zhaoruixuan on 2017/5/9.
 */
public class CompatibilityInfoCompat {

    private static Class sClass;

    private static Class getMyClass() throws ClassNotFoundException{
        if(sClass==null){
            sClass=Class.forName("android.content.res.CompatibilityInfo");
        }
        return sClass;
    }

    private static Object sDefaultCompatibilityInfo;

    public static Object DEFAULT_COMPATIBILITY_INFO() throws Exception{
        if(sDefaultCompatibilityInfo==null){
            sDefaultCompatibilityInfo= FieldUtils.readStaticField(getMyClass(),"DEFAULT_COMPATIBILITY_INFO");
        }
        return sDefaultCompatibilityInfo;
    }

}
