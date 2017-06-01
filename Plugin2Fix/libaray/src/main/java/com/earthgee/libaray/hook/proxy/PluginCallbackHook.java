package com.earthgee.libaray.hook.proxy;

import android.content.Context;
import android.os.Handler;

import com.earthgee.libaray.helper.ActivityManagerNativeCompat;
import com.earthgee.libaray.helper.ActivityThreadCompat;
import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.Hook;
import com.earthgee.libaray.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/6/1.
 */
public class PluginCallbackHook extends Hook{

    private List<PluginCallback> mCallbacks=new ArrayList<>();

    protected PluginCallbackHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    public void setEnable(boolean enable, boolean reInstallHook) {
        if(reInstallHook){
            try{
                onInstall(null);
            }catch (Exception e){
            }
        }
        for(PluginCallback callback:mCallbacks){
            callback.setEnable(enable);
        }
        super.setEnable(enable, reInstallHook);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Exception {
        Object target= ActivityThreadCompat.currentActivityThread();
        Class ActivityThreadClass= ActivityManagerNativeCompat.activityThreadClass();

        Field mHField= FieldUtils.getField(ActivityThreadClass,"mH");
        Handler handler= (Handler) FieldUtils.readField(mHField,target);
        Field mCallbackField=FieldUtils.getField(Handler.class,"mCallback");
        Object mCallback=FieldUtils.readField(mCallbackField,handler);
        if(!PluginCallback.class.isInstance(mCallback)){
            PluginCallback value=mCallback!=null?new PluginCallback(mHostContext,handler,(Handler.Callback)mCallback):
                    new PluginCallback(mHostContext,handler,null);
            value.setEnable(isEnable());
            mCallbacks.add(value);
            FieldUtils.writeField(mCallbackField,handler,value);
        }
    }
}



























