package com.earthgee.pluginlib.hook.proxy;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.earthgee.pluginlib.helper.compat.ActivityManagerNativeCompat;
import com.earthgee.pluginlib.hook.BaseHookHandle;
import com.earthgee.pluginlib.reflect.FieldUtils;

/**
 * Created by zhaoruixuan on 2019/12/10.
 */
public class IActivityManagerHook extends ProxyHook{

    protected IActivityManagerHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Exception {
        Class cls= ActivityManagerNativeCompat.Class();
        Object obj=FieldUtils.readStaticField(cls,"gDefault");
        if(obj==null){
            ActivityManagerNativeCompat.getDefault();
            obj=FieldUtils.readStaticField(cls,"gDefault");
        }


    }

}
