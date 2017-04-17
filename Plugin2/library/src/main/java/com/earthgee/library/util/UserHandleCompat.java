package com.earthgee.library.util;

import android.os.UserHandle;

import com.earthgee.library.reflect.MethodUtils;

/**
 * Created by zhaoruixuan on 2017/4/17.
 */
public class UserHandleCompat {

    public static int getCallingUserId(){
        try{
            return (int) MethodUtils.invokeMethod(UserHandle.class,"getCallingUserId");
        }catch (Exception e){
        }
        return 0;
    }

}
