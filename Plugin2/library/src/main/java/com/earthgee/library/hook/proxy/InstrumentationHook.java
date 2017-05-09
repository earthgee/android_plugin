package com.earthgee.library.hook.proxy;

import android.app.Instrumentation;
import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.handle.PluginInstrumentation;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.util.ActivityThreadCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/8.
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
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        Object target= ActivityThreadCompat.currentActivityThread();
        Class ActivityThreadClass=ActivityThreadCompat.activityThreadClass();

        Field mInstrumentationField= FieldUtils.getField(ActivityThreadClass,"mInstrumentation");
        Instrumentation mInstrumentation= (Instrumentation) FieldUtils.readField(mInstrumentationField,target);
        if(!PluginInstrumentation.class.isInstance(mInstrumentation)){
            PluginInstrumentation pit=new PluginInstrumentation(mHostContext,mInstrumentation);
            pit.setEnable(isEnable());
            mPluginInstrumentations.add(pit);
            FieldUtils.writeField(mInstrumentationField,target,pit);
        }
    }
}






















