package com.earthgee.library.hook.proxy;

import android.content.Context;
import android.os.Handler;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.handle.PluginCallback;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.util.ActivityThreadCompat;

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/5.
 * 将ActivityThread中的mH中的callback换掉 换成PluginCallback hook AMS处理后的过程 以更换要启动的activity为plugin
 */
public class PluginCallbackHook extends Hook{
    private List<PluginCallback> mCallbacks=new ArrayList<>(1);

    public PluginCallbackHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        Object target= ActivityThreadCompat.currentActivityThread();
        Class ActivityThreadClass=ActivityThreadCompat.activityThreadClass();

        Field mHField= FieldUtils.getField(ActivityThreadClass,"mH");
        Handler handler= (Handler) FieldUtils.readField(mHField,target);
        Field mCallbackField=FieldUtils.getField(Handler.class,"mCallback");

        Object mCallback=FieldUtils.readField(mCallbackField,handler);
        if(!PluginCallback.class.isInstance(mCallback)){
            PluginCallback value=mCallback!=null?
                    new PluginCallback(mHostContext,handler, (Handler.Callback) mCallback):
                    new PluginCallback(mHostContext,handler,null);
            value.setEnable(isEnable());
            mCallbacks.add(value);
            FieldUtils.writeField(mCallbackField,handler,value);
        }
    }
}




















