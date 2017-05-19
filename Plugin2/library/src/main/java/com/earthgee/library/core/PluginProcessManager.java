package com.earthgee.library.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;

import com.earthgee.library.hook.HookFactory;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.MethodUtils;
import com.earthgee.library.stub.ActivityStub;
import com.earthgee.library.stub.ServiceStub;
import com.earthgee.library.util.ActivityThreadCompat;
import com.earthgee.library.util.CompatibilityInfoCompat;
import com.earthgee.library.util.ProcessCompat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by zhaoruixuan on 2017/4/11.
 * 插件进程管理
 */
public class PluginProcessManager {

    private static String sCurrentProcessName;
    private static Object sGetCurrentProcessNameLock=new Object();

    private static Map<String,ClassLoader> sPluginClassLoaderCache=new WeakHashMap<>(1);
    private static Map<String,Object> sPluginLoadedApkCache=new WeakHashMap<>(1);

    private static List<String> sProcessList=new ArrayList<>();

    private static WeakHashMap<Integer,Context> mFakedContext=new WeakHashMap<>(1);
    private static Object mServiceCache=null;

    //是否是插件的进程
    public static final boolean isPluginProcess(Context context){
        String currentPorcessName=getCurrentProcessName(context);
        if(TextUtils.equals(currentPorcessName,context.getPackageName())){
            return false;
        }

        initProcessList(context);
        return !sProcessList.contains(currentPorcessName);
    }

    //找到目前的进程名字
    public static String getCurrentProcessName(Context context){
        if(context==null){
            return sCurrentProcessName;
        }

        synchronized (sGetCurrentProcessNameLock){
            if(sCurrentProcessName==null){
                ActivityManager activityManager=
                        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> infos=
                        activityManager.getRunningAppProcesses();
                if(infos==null) return null;

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

    //注意getPackageInfo已经被hook过了 所以这里找出所有插件组件的进程(包名)
    private static void initProcessList(Context context){
        try{
            if(sProcessList.size()>0){
                return;
            }
            sProcessList.add(context.getPackageName());
            PackageManager pm=context.getPackageManager();
            PackageInfo packageInfo=pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES|PackageManager.GET_SERVICES|
                            PackageManager.GET_PROVIDERS|PackageManager.GET_RECEIVERS);
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
                            &&info.processName!=null&&info.authority!=null&&info.authority.indexOf(PluginManager.AUTHORITY_NAME)<0){
                        sProcessList.add(info.processName);
                    }
                }
            }

            if(packageInfo.services!=null){
                for(ServiceInfo info:packageInfo.services){
                    if(!sProcessList.contains(info.processName)&&info.processName!=null
                            &&info.name!=null&&info.name.indexOf(ServiceStub.class.getSimpleName())<0){
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

    /**
     * 启动hook 由HookFactory接管
     * @param hostContext
     * @throws Throwable
     */
    public static void installHook(Context hostContext) throws Throwable{
        HookFactory.getInstance().installHook(hostContext,null);
    }

    public static void setHookEnable(boolean enable){
        HookFactory.getInstance().setHookEnable(enable);
    }

    public static void setHookEnable(boolean enable,boolean reinstallHook){
        HookFactory.getInstance().setHookEnable(enable,reinstallHook);
    }

    private static HashMap<String,Application> sApplicationCache=new HashMap<>(2);

    public static Application getPluginContext(String packageName) throws Exception{
        if(!sApplicationCache.containsKey(packageName)){
            Object at= ActivityThreadCompat.currentActivityThread();
            Object mAllApplications= FieldUtils.readField(at,"mAllApplications");
            if(mAllApplications instanceof List){
                List apps= (List) mAllApplications;
                for(Object o:apps){
                    if(o instanceof Application){
                        Application app= (Application) o;
                        if(!sApplicationCache.containsKey(app.getPackageName())){
                            sApplicationCache.put(app.getPackageName(),app);
                        }
                    }
                }
            }
        }
        return sApplicationCache.get(packageName);
    }

    /**
     * 获取ContextWrapper对应的mBase,即ContextImpl
     * @param c
     * @return
     */
    private static Context getBaseContext(Context c){
        if(c instanceof ContextWrapper){
            return ((ContextWrapper)c).getBaseContext();
        }
        return c;
    }

    //必须跳过的服务
    private static List<String> sSkipService=new ArrayList<>();

    static {
        sSkipService.add(Context.LAYOUT_INFLATER_SERVICE);
        sSkipService.add(Context.NOTIFICATION_SERVICE);
        sSkipService.add("storage");
        sSkipService.add("accessibility");
        sSkipService.add("audio");
        sSkipService.add("clipboard");
        sSkipService.add("media_router");
        sSkipService.add("wifi");
        sSkipService.add("captioning");
        sSkipService.add("account");
        sSkipService.add("activity");
        sSkipService.add("wifiscanner");
        sSkipService.add("rttmanager");
        sSkipService.add("tv_input");
        sSkipService.add("jobscheduler");
        sSkipService.add("sensorhub");
        sSkipService.add("servicediscovery");
    }

    private static void fakeSystemServiceInner(Context hostContext,Context targetContext){
        try{
            Context baseContext=getBaseContext(targetContext);
            //此context是否已做过处理
            if(mFakedContext.containsValue(baseContext)){
                return;
            }else if(mServiceCache!=null){
                FieldUtils.writeField(baseContext,"mServiceCache",mServiceCache);
                ContentResolver cr=baseContext.getContentResolver();
                if(cr!=null){
                    Object crctx=FieldUtils.readField(cr,"mContext");
                    if(crctx!=null){
                        FieldUtils.writeField(crctx,"mServiceCache",mServiceCache);
                    }
                }
                if(!mFakedContext.containsValue(baseContext)){
                    mFakedContext.put(baseContext.hashCode(),baseContext);
                }
                return;
            }
            Object SYSTEM_SERVICE_MAP=null;
            try{
                SYSTEM_SERVICE_MAP=FieldUtils.readStaticField(baseContext.getClass(),"SYSTEM_SERVICE_MAP");
            }catch (Exception e){
            }
            if(SYSTEM_SERVICE_MAP==null){
                try{
                    SYSTEM_SERVICE_MAP=FieldUtils.readStaticField(Class.forName("android.app.SystemServiceRegistry"),
                            "SYSTEM_SERVICE_FETCHERS");
                }catch (Exception e){
                }
            }

            if(SYSTEM_SERVICE_MAP!=null&&(SYSTEM_SERVICE_MAP instanceof Map)){
                Map<?,?> sSYSTEM_SERVICE_MAP= (Map<?, ?>) SYSTEM_SERVICE_MAP;
                Context originContext=getBaseContext(hostContext);

                Object mServiceCache=FieldUtils.readField(originContext,"mServiceCache");
                if(mServiceCache instanceof List){
                    ((List) mServiceCache).clear();
                }

                for(Object key:sSYSTEM_SERVICE_MAP.keySet()){
                    if(sSkipService.contains(key)){
                        continue;
                    }
                    Object serviceFetcher=sSYSTEM_SERVICE_MAP.get(key);
                    try{
                        Method getService=serviceFetcher.getClass().getMethod("getService",baseContext.getClass());
                        getService.invoke(serviceFetcher,originContext);
                    }catch (InvocationTargetException e){
                        Throwable cause=e.getCause();
                        if(cause!=null){
                        }else{
                        }
                    }catch (Exception e){
                    }
                }
                mServiceCache=FieldUtils.readField(originContext,"mServiceCache");
                FieldUtils.writeField(baseContext,"mServiceCache",mServiceCache);

                ContentResolver cr=baseContext.getContentResolver();
                if(cr!=null){
                    Object crctx=FieldUtils.readField(cr,"mContext");
                    if(crctx!=null){
                        FieldUtils.writeField(crctx,"mServiceCache",mServiceCache);
                    }
                }
            }
            if(!mFakedContext.containsValue(baseContext)){
                mFakedContext.put(baseContext.hashCode(),baseContext);
            }
        }catch (Exception e){
        }
    }

    /**
     * 这里为了解决某些插件调用系统服务时，系统服务必须要求要以host包名的身份去调用的问题。
     * 也就是在这里检查所有的binder hook都生效了
     * @param hostContext
     * @param targetContext
     */
    public static void fakeSystemService(Context hostContext,Context targetContext){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1&&
                !TextUtils.equals(hostContext.getPackageName(),targetContext.getPackageName())){
            fakeSystemServiceInner(hostContext,targetContext);
        }
    }

    //根据插件包名获取对应的classloader
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

    //将插件对应的LoadedApk加入ActivityThread中，并创建对应classloader
    public static void preLoadedApk(Context hostContext, ComponentInfo pluginInfo) throws Exception{
        if(pluginInfo==null&&hostContext==null){
            return;
        }
        if(pluginInfo!=null&&getPluginContext(pluginInfo.packageName)!=null){
            return;
        }

        boolean found=false;
        synchronized (sPluginLoadedApkCache){
            Object object=ActivityThreadCompat.currentActivityThread();
            if(object!=null){
                Object mPackagesObj=FieldUtils.readField(object,"mPackages");
                Object containsKeyObj= MethodUtils.invokeMethod(mPackagesObj,"containsKey",pluginInfo.packageName);
                if(containsKeyObj instanceof Boolean&&!(Boolean) containsKeyObj){
                    final Object loadedApk;
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB){
                        loadedApk=MethodUtils.invokeMethod(object,"getPackageInfoNoCheck",pluginInfo.applicationInfo,
                                CompatibilityInfoCompat.DEFAULT_COMPATIBILITY_INFO());
                    }else {
                        loadedApk=MethodUtils.invokeMethod(object,"getPackageInfoNoCheck",pluginInfo.applicationInfo);
                    }
                    sPluginLoadedApkCache.put(pluginInfo.packageName,loadedApk);

                    String optimizedDirectory=PluginDirHelper.getPluginDalvikCacheDir(hostContext,pluginInfo.packageName);
                    String libraryPath=PluginDirHelper.getPluginNativeLibraryDir(hostContext,pluginInfo.packageName);
                    String apk=pluginInfo.applicationInfo.publicSourceDir;

                    if(TextUtils.isEmpty(apk)){
                        pluginInfo.applicationInfo.publicSourceDir=PluginDirHelper.getPluginApkFile(hostContext,pluginInfo.packageName);
                        apk=pluginInfo.applicationInfo.publicSourceDir;
                    }

                    if(apk!=null){
                        ClassLoader classLoader=null;
                        try{
                            classLoader=new PluginClassLoader(apk,optimizedDirectory,libraryPath,hostContext.getClassLoader().getParent());
                        }catch (Exception e){
                        }
                        if(classLoader==null){
                            PluginDirHelper.cleanOptimizedDirectory(optimizedDirectory);
                            classLoader=new PluginClassLoader(apk,optimizedDirectory,libraryPath,hostContext.getClassLoader().getParent());
                        }
                        synchronized (loadedApk){
                            FieldUtils.writeDeclaredField(loadedApk,"mClassLoadeder",classLoader);
                        }
                        sPluginClassLoaderCache.put(pluginInfo.packageName,classLoader);
                        Thread.currentThread().setContextClassLoader(classLoader);
                        found=true;
                    }
                    ProcessCompat.setArgV0(pluginInfo.processName);
                }
            }
        }
    }

    public static void registerStaticReceiver(Context context, ApplicationInfo pluginApplicationInfo, ClassLoader cl) throws Exception{
//        List<ActivityInfo> infos=PluginManager.getInstance().getReceivers(pluginApplicationInfo.packageName,0);
//        if(infos!=null&&infos.size()>0){
//            CharSequence myPname=null;
//        }
    }
}
















































