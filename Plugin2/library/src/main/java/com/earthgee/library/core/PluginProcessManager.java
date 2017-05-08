package com.earthgee.library.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.earthgee.library.stub.ActivityStub;
import com.earthgee.library.stub.ServiceStub;
import com.earthgee.library.util.ActivityThreadCompat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class PluginProcessManager {

    private static String sCurrentProcessName;
    private static Object sGetCurrentProcessNameLock=new Object();

    private static List<String> sProcessList=new ArrayList<>();

    private static WeakHashMap<Integer,Context> mFakedContext=new WeakHashMap<>(1);
    private static Object mServiceCache=null;

    public static final boolean isPluginProcess(Context context){
        String currentPorcessName=getCurrentProcessName(context);
        if(TextUtils.equals(currentPorcessName,context.getPackageName())){
            return false;
        }

        initProcessList(context);
        return !sProcessList.contains(currentPorcessName);
    }

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

    private static Context getBaseContext(Context c){
        if(c instanceof ContextWrapper){
            return ((ContextWrapper)c).getBaseContext();
        }
        return c;
    }

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
                SYSTEM_SERVICE_MAP=FieldUtils.readStaticField(baseContext.getClass(),"SYETEM_SERVICE_MAP");
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

    public static void fakeSystemService(Context hostContext,Context targetContext){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1&&
                !TextUtils.equals(hostContext.getPackageName(),targetContext.getPackageName())){
            fakeSystemServiceInner(hostContext,targetContext);
        }
    }

}
















































