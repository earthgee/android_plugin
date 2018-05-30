package com.earthgee.bundle.hack;

import android.app.Application;
import android.app.Instrumentation;
import android.content.res.Resources;

import java.lang.ref.WeakReference;

/**
 * Created by zhaoruixuan on 2018/4/26.
 */
public class AndroidHack {

    private static Object _mLoadedApk;
    private static Object _sActivityThread;

    public static Object getActivityThread() throws Exception{
        if(_sActivityThread==null){
            _sActivityThread=SysHacks.ActivityThread_currentActivityThread.invoke(null,new Object[0]);
        }
        return _sActivityThread;
    }

    public static Instrumentation getInstrumentation() throws Exception{
        Object activityThread=getActivityThread();
        if (activityThread != null) {
            return SysHacks.ActivityThread_mInstrumentation.get(activityThread);
        }
        throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
    }

    public static void injectInstrumentationHook(Instrumentation instrumentation) throws Exception{
        Object activityThread=getActivityThread();
        if(activityThread==null){
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        SysHacks.ActivityThread_mInstrumentation.set(activityThread,instrumentation);
    }

    public static Object getLoadedApk(Object obj,String str) throws Exception{
        if(_mLoadedApk==null){
            WeakReference weakReference= (WeakReference) SysHacks.ActivityThread_mPackages.get(obj).get(str);
            if(weakReference!=null){
                _mLoadedApk=weakReference.get();
            }
        }
        return _mLoadedApk;
    }

    public static void injectResources(Application application, Resources resources) throws Exception{
        Object activityThread=getActivityThread();
        if(activityThread==null){
            throw new Exception("Failed to get ActivityThread.sCurrentActivityThread");
        }
        Object loadedApk=getLoadedApk(activityThread,application.getPackageName());
        if(loadedApk==null){
            throw new Exception("Failed to get ActivityThread.mLoadedApk");
        }
        SysHacks.LoadedApk_mResources.set(loadedApk,resources);
        SysHacks.ContextImpl_mResources.set(application.getBaseContext(),resources);
        SysHacks.ContextImpl_mTheme.set(application.getBaseContext(),null);
    }

}
