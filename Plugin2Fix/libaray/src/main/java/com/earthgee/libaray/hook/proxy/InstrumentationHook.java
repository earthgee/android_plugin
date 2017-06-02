package com.earthgee.libaray.hook.proxy;

import android.app.Instrumentation;
import android.content.Context;

import com.earthgee.libaray.helper.ActivityThreadCompat;
import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.Hook;
import com.earthgee.libaray.hook.handle.PluginInstrumentation;
import com.earthgee.libaray.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/6/2.
 */
public class InstrumentationHook extends Hook{

    private List<PluginInstrumentation> mPluginInstrumentations=new ArrayList<>();

    public InstrumentationHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    public void setEnable(boolean enable, boolean reInstallHook) {
        if (reInstallHook) {
            try {
                onInstall(null);
            } catch (Throwable throwable) {
            }
        }

        for (PluginInstrumentation pit : mPluginInstrumentations) {
            pit.setEnable(enable);
        }

        super.setEnable(enable,reInstallHook);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        Object target= ActivityThreadCompat.currentActivityThread();
        Class ActivityThreadClass=ActivityThreadCompat.activityThreadClass();

        Field mInstrumentationField=FieldUtils.getField(ActivityThreadClass,"mInstrumentation");
        Instrumentation mInstrumentation=
                (Instrumentation) FieldUtils.readField(mInstrumentationField,target);
        if(!PluginInstrumentation.class.isInstance(mInstrumentation)){
            PluginInstrumentation pit=new PluginInstrumentation(mHostContext,mInstrumentation);
            pit.setEnable(isEnable());
            mPluginInstrumentations.add(pit);
            FieldUtils.writeField(mInstrumentationField,target,pit);
        }
    }
}
















