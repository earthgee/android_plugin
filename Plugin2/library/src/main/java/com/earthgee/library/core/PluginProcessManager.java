package com.earthgee.library.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Looper;
import android.text.TextUtils;

import com.earthgee.library.hook.HookFactory;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.stub.ActivityStub;
import com.earthgee.library.stub.ServiceStub;
import com.earthgee.library.util.ActivityThreadCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class PluginProcessManager {

    private static String sCurrentProcessName;
    private static Object sGetCurrentProcessNameLock=new Object();

    private static List<String> sProcessList=new ArrayList<>();

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

}
















































