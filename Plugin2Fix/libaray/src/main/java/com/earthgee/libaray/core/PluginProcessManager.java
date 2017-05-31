package com.earthgee.libaray.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

import com.earthgee.libaray.helper.ActivityThreadCompat;
import com.earthgee.libaray.hook.HookFactory;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.FieldUtils;
import com.earthgee.libaray.stub.ActivityStub;
import com.earthgee.libaray.stub.ServiceStub;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class PluginProcessManager {

    private static Map<String,ClassLoader> sPluginClassLoaderCache=new WeakHashMap<>();
    private static Object sGetCurrentProcessNameLock=new Object();
    private static String sCurrentProcessName;

    public static String getCurrentProcessName(Context context){
        if(context==null){
            return sCurrentProcessName;
        }

        synchronized (sGetCurrentProcessNameLock){
            if(sCurrentProcessName==null){
                ActivityManager activityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> infos=activityManager.getRunningAppProcesses();
                if(infos==null){
                    return null;
                }

                for(ActivityManager.RunningAppProcessInfo info:infos){
                    if(info.pid==android.os.Process.myPid()){
                        sCurrentProcessName=info.processName;
                        return sCurrentProcessName;
                    }
                }
            }
        }
        return sCurrentProcessName;
    }

    private static List<String> sProcessList=new ArrayList<>();

    private static void initProcessList(Context context){
        try{
            if(sProcessList.size()>0){
                return;
            }

            sProcessList.add(context.getPackageName());

            PackageManager pm=context.getPackageManager();
            PackageInfo packageInfo=pm.getPackageInfo(context.getPackageName(),PackageManager.GET_RECEIVERS
                    |PackageManager.GET_ACTIVITIES|PackageManager.GET_SERVICES|PackageManager.GET_PROVIDERS);

            if(packageInfo.receivers!=null){
                for(ActivityInfo info:packageInfo.receivers){
                    if(!sProcessList.contains(info.processName)){
                        sProcessList.add(info.processName);
                    }
                }
            }

            if(packageInfo.providers!=null){
                for(ProviderInfo info:packageInfo.providers){
                    if(!sProcessList.contains(info.processName)
                            &&info.processName!=null
                            &&info.authority!=null
                            &&info.authority.indexOf(PluginManager.STUB_AUTHORITY_NAME)<0){
                        sProcessList.add(info.processName);
                    }
                }
            }

            if(packageInfo.services!=null){
                for(ServiceInfo info:packageInfo.services){
                    if(!sProcessList.contains(info.processName)
                            &&info.processName!=null
                            &&info.name!=null
                            &&info.name.indexOf(ServiceStub.class.getSimpleName())<0){
                        sProcessList.add(info.processName);
                    }
                }
            }

            if(packageInfo.activities!=null){
                for(ActivityInfo info:packageInfo.activities){
                    if(!sProcessList.contains(info.processName)&&info.processName!=null
                            &&info.name!=null&&info.name.indexOf(ActivityStub.class.getSimpleName())<0){
                        sProcessList.add(info.processName);
                    }
                }
            }
        }catch (PackageManager.NameNotFoundException e){

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

    public static ClassLoader getPluginClassLoader(String pkg) throws Exception{
        ClassLoader classLoader=sPluginClassLoaderCache.get(pkg);
        if(classLoader==null){
            Application app=getPluginContext(pkg);
            if(app!=null){
                sPluginClassLoaderCache.put(app.getPackageName(),app.getClassLoader());
            }
        }
        return sPluginClassLoaderCache.get(pkg);
    }

    private static HashMap<String,Application> sApplicationsCache=new HashMap<>();

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
    public static final boolean isPluginProcess(Context context){
        String currentProcessName=getCurrentProcessName(context);
        if(TextUtils.equals(currentProcessName,context.getPackageName())){
            return false;
        }

        initProcessList(context);
        return !sProcessList.contains(currentProcessName);
    }



}















