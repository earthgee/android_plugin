package com.earthgee.libaray.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.earthgee.libaray.helper.ActivityThreadCompat;
import com.earthgee.libaray.helper.CompatibilityInfoCompat;
import com.earthgee.libaray.helper.ProcessCompat;
import com.earthgee.libaray.hook.HookFactory;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.FieldUtils;
import com.earthgee.libaray.reflect.MethodUtils;
import com.earthgee.libaray.stub.ActivityStub;
import com.earthgee.libaray.stub.ServiceStub;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class PluginProcessManager {

    private static Map<String, ClassLoader> sPluginClassLoaderCache = new WeakHashMap<>();
    private static Object sGetCurrentProcessNameLock = new Object();
    private static String sCurrentProcessName;
    private static Map<String, Object> sPluginLoadedApkCache = new WeakHashMap<>();

    public static String getCurrentProcessName(Context context) {
        if (context == null) {
            return sCurrentProcessName;
        }

        synchronized (sGetCurrentProcessNameLock) {
            if (sCurrentProcessName == null) {
                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> infos = activityManager.getRunningAppProcesses();
                if (infos == null) {
                    return null;
                }

                for (ActivityManager.RunningAppProcessInfo info : infos) {
                    if (info.pid == android.os.Process.myPid()) {
                        sCurrentProcessName = info.processName;
                        return sCurrentProcessName;
                    }
                }
            }
        }
        return sCurrentProcessName;
    }

    private static List<String> sProcessList = new ArrayList<>();

    private static void initProcessList(Context context) {
        try {
            if (sProcessList.size() > 0) {
                return;
            }

            sProcessList.add(context.getPackageName());

            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS
                    | PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES | PackageManager.GET_PROVIDERS);

            if (packageInfo.receivers != null) {
                for (ActivityInfo info : packageInfo.receivers) {
                    if (!sProcessList.contains(info.processName)) {
                        sProcessList.add(info.processName);
                    }
                }
            }

            if (packageInfo.providers != null) {
                for (ProviderInfo info : packageInfo.providers) {
                    if (!sProcessList.contains(info.processName)
                            && info.processName != null
                            && info.authority != null
                            && info.authority.indexOf(PluginManager.STUB_AUTHORITY_NAME) < 0) {
                        sProcessList.add(info.processName);
                    }
                }
            }

            if (packageInfo.services != null) {
                for (ServiceInfo info : packageInfo.services) {
                    if (!sProcessList.contains(info.processName)
                            && info.processName != null
                            && info.name != null
                            && info.name.indexOf(ServiceStub.class.getSimpleName()) < 0) {
                        sProcessList.add(info.processName);
                    }
                }
            }

            if (packageInfo.activities != null) {
                for (ActivityInfo info : packageInfo.activities) {
                    if (!sProcessList.contains(info.processName) && info.processName != null
                            && info.name != null && info.name.indexOf(ActivityStub.class.getSimpleName()) < 0) {
                        sProcessList.add(info.processName);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
    }

    public static void installHook(Context hostContext) throws Exception {
        HookFactory.getInstance().installHook(hostContext, null);
    }

    public static void setHookEnable(boolean enable) {
        HookFactory.getInstance().setHookEnable(enable);
    }

    public static void setHookEnable(boolean enable, boolean reinstallHook) {
        HookFactory.getInstance().setHookEnable(enable, reinstallHook);
    }

    public static ClassLoader getPluginClassLoader(String pkg) throws Exception {
        ClassLoader classLoader = sPluginClassLoaderCache.get(pkg);
        if (classLoader == null) {
            Application app = getPluginContext(pkg);
            if (app != null) {
                sPluginClassLoaderCache.put(app.getPackageName(), app.getClassLoader());
            }
        }
        return sPluginClassLoaderCache.get(pkg);
    }

    private static HashMap<String, Application> sApplicationsCache = new HashMap<>();

    public static Application getPluginContext(String packageName) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        if (!sApplicationsCache.containsKey(packageName)) {
            Object at = ActivityThreadCompat.currentActivityThread();
            Object mAllApplications = FieldUtils.readField(at, "mAllApplications");
            if (mAllApplications instanceof List) {
                List apps = (List) mAllApplications;
                for (Object o : apps) {
                    if (o instanceof Application) {
                        Application app = (Application) o;
                        if (!sApplicationsCache.containsKey(app.getPackageName())) {
                            sApplicationsCache.put(app.getPackageName(), app);
                        }
                    }
                }
            }
        }
        return sApplicationsCache.get(packageName);
    }

    //是不是插件的进程 package:process1 process2 ...
    public static final boolean isPluginProcess(Context context) {
        String currentProcessName = getCurrentProcessName(context);
        if (TextUtils.equals(currentProcessName, context.getPackageName())) {
            return false;
        }

        initProcessList(context);
        return !sProcessList.contains(currentProcessName);
    }

    public static void preLoadApk(Context hostContext, ComponentInfo pluginInfo) throws Exception {
        if (pluginInfo == null && hostContext == null) {
            return;
        }
        if (pluginInfo != null && getPluginContext(pluginInfo.packageName) != null) {
            return;
        }

        boolean found = false;
        synchronized (sPluginLoadedApkCache) {
            Object object = ActivityThreadCompat.currentActivityThread();
            if (object != null) {
                Object mPackagesObj = FieldUtils.readField(object, "mPackages");
                Object containsKeyObj = MethodUtils.invokeMethod(mPackagesObj, "containsKey", pluginInfo.packageName);
                if (containsKeyObj instanceof Boolean && !(Boolean) containsKeyObj) {
                    final Object loadedApk;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        loadedApk = MethodUtils.invokeMethod(object, "getPackageInfoNoCheck", pluginInfo.applicationInfo, CompatibilityInfoCompat.DEFAULT_COMPATIBILITY_INFO());
                    } else {
                        loadedApk = MethodUtils.invokeMethod(object, "getPackageInfoNoCheck", pluginInfo.applicationInfo);
                    }
                    sPluginLoadedApkCache.put(pluginInfo.packageName, loadedApk);

                    String optimizedDirectory = PluginDirHelper.getPluginDalvikCacheDir(hostContext, pluginInfo.packageName);
                    String libraryPath = PluginDirHelper.getPluginNativeLibraryDir(hostContext, pluginInfo.packageName);
                    String apk = pluginInfo.applicationInfo.publicSourceDir;
                    if (TextUtils.isEmpty(apk)) {
                        pluginInfo.applicationInfo.processName =
                                PluginDirHelper.getPluginApkFile(hostContext, pluginInfo.packageName);
                        apk = pluginInfo.applicationInfo.publicSourceDir;
                    }
                    if (apk != null) {
                        ClassLoader classLoader = null;
                        try {
                            classLoader = new PluginClassLoader(apk, optimizedDirectory, libraryPath, hostContext.getClassLoader().getParent());
                        } catch (Exception e) {
                        }
                        if (classLoader == null) {
                            PluginDirHelper.cleanOptimizedDirectory(optimizedDirectory);
                            classLoader = new PluginClassLoader(apk, optimizedDirectory, libraryPath, hostContext.getClassLoader().getParent());
                        }
                        synchronized (loadedApk){
                            FieldUtils.writeDeclaredField(loadedApk,"mClassLoader",classLoader);
                        }
                        sPluginClassLoaderCache.put(pluginInfo.packageName,classLoader);
                        Thread.currentThread().setContextClassLoader(classLoader);
                        found=true;
                    }
                    ProcessCompat.setArgV0(pluginInfo.processName);
                }
            }
        }
        if(found){
            PluginProcessManager.preMakeApplication(hostContext,pluginInfo);
        }
    }

    private static AtomicBoolean mExec=new AtomicBoolean(false);
    private static Handler sHandle=new Handler(Looper.getMainLooper());

    private static void preMakeApplication(Context hostContext,ComponentInfo pluginInfo){
        try{
            final Object loadedApk=sPluginLoadedApkCache.get(pluginInfo.packageName);
            if(loadedApk!=null){
                Object mApplication=FieldUtils.readField(loadedApk,"mAppliaction");
                if(mApplication!=null){
                    return;
                }

                if(Looper.getMainLooper()!=Looper.myLooper()){
                    final Object lock=new Object();
                    mExec.set(false);
                    sHandle.post(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                MethodUtils.invokeMethod(loadedApk,"makeApplication",false,ActivityThreadCompat.getInstrumentation());
                            }catch (Exception e){
                            }finally {
                                mExec.set(true);
                                synchronized (lock){
                                    lock.notifyAll();
                                }
                            }
                        }
                    });
                    if(!mExec.get()){
                        synchronized (lock){
                            try{
                                lock.wait();
                            }catch (InterruptedException e){
                            }
                        }
                    }
                }else{
                    MethodUtils.invokeMethod(loadedApk,"makeApplication",false,ActivityThreadCompat.getInstrumentation());
                }
            }
        }catch (Exception e){
        }
    }

    public static void registerStaticReceiver(Context context, ApplicationInfo pluginApplicationInfo,ClassLoader cl) throws Exception{
        List<ActivityInfo> infos=PluginManager.getInstance().getReceivers(pluginApplicationInfo.packageName,0);
        if(infos!=null&&infos.size()>0){
            CharSequence myPname=null;
            try{
                myPname=PluginManager.getInstance().getProcessNameByPid(android.os.Process.myPid());
            }catch (Exception e){
            }
            for(ActivityInfo info:infos){
                if(TextUtils.equals(info.processName,myPname)){
                    List<IntentFilter> filters=PluginManager.getInstance().getReceiverIntentFilter(info);
                    for(IntentFilter filter:filters){
                        BroadcastReceiver receiver= (BroadcastReceiver) cl.loadClass(info.name).newInstance();
                        context.registerReceiver(receiver,filter);
                    }
                }
            }
        }
    }

}











































