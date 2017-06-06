package com.earthgee.libaray.hook.handle;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.libaray.PluginPatchManager;
import com.earthgee.libaray.am.RunningActivities;
import com.earthgee.libaray.core.Env;
import com.earthgee.libaray.core.PluginProcessManager;
import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.HookedMethodHandler;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.FieldUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhaoruixuan on 2017/5/26.
 */
public class IActivityManagerHookHandle extends BaseHookHandle{
    public IActivityManagerHookHandle(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected void init() {
        sHookedMethodHandlers.put("startActivity",new startActivity(mHostContext));
        sHookedMethodHandlers.put("getRunningAppProcesses",new getRunningAppProcesses(mHostContext));
        sHookedMethodHandlers.put("startService",new startService(mHostContext));
    }

    //todo replace pacakge name
    private static class startActivity extends HookedMethodHandler{

        public startActivity(Context hostContext) {
            super(hostContext);
        }

        protected boolean doReplaceIntentForStartActivityAPILow(Object[] args) throws RemoteException{
            int intentOfArgIndex = findFirstIntentIndexInArgs(args);
            if(args!=null&&args.length>1&&intentOfArgIndex>=0){
                Intent intent= (Intent) args[intentOfArgIndex];
                ActivityInfo activityInfo=resolveActivity(intent);
                if(activityInfo!=null&&isPackagePlugin(activityInfo.packageName)){
                    ComponentName component=selectProxyActivity(intent);
                    if(component!=null){
                        Intent newIntent=new Intent();
                        newIntent.setComponent(component);
                        newIntent.putExtra(Env.EXTRA_TARGET_INTENT,intent);
                        newIntent.setFlags(intent.getFlags());
                        if (TextUtils.equals(mHostContext.getPackageName(), activityInfo.packageName)) {
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                        args[intentOfArgIndex] = newIntent;
                    }
                }
            }
            return true;
        }

        private void setIntentClassLoader(Intent intent,ClassLoader classLoader){
            try{
                Bundle mExtras= (Bundle) FieldUtils.readField(intent,"mExtras");
                if(mExtras!=null){
                    mExtras.setClassLoader(classLoader);
                }else{
                    Bundle value=new Bundle();
                    value.setClassLoader(classLoader);
                    FieldUtils.writeField(intent,"mExtras",value);
                }
            }catch (Exception e){
            }finally {
                intent.setExtrasClassLoader(classLoader);
            }
        }

        protected boolean doReplaceIntentForStartActivityAPIHigh(Object[] args) throws RemoteException{
            int intentOfArgIndex=findFirstIntentIndexInArgs(args);
            if(args!=null&&args.length>1&&intentOfArgIndex>=0){
                Intent intent= (Intent) args[intentOfArgIndex];
                if(!PluginPatchManager.getInstance().canStartPluginActivity(intent)){
                    PluginPatchManager.getInstance().startPluginActivity(intent);
                    return false;
                }

                ActivityInfo activityInfo=resolveActivity(intent);
                //是插件的组件进行hook
                if(activityInfo!=null&&isPackagePlugin(activityInfo.packageName)){
                    ComponentName component=selectProxyActivity(intent);
                    if(component!=null){
                        Intent newIntent=new Intent();
                        try{
                            ClassLoader pluginClassLoader= PluginProcessManager.getPluginClassLoader(component.getPackageName());
                            setIntentClassLoader(newIntent,pluginClassLoader);
                        }catch (Exception e){
                        }
                        newIntent.setComponent(component);
                        newIntent.putExtra(Env.EXTRA_TARGET_INTENT,intent);
                        newIntent.setFlags(intent.getFlags());

                        String callingPackage= (String) args[1];
                        if(TextUtils.equals(mHostContext.getPackageName(),callingPackage)){
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }

                        args[intentOfArgIndex]=newIntent;
                        args[1]=mHostContext.getPackageName();
                    }
                }
            }
            return true;
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            RunningActivities.beforeStartActivity();
            boolean bRet=true;
            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.JELLY_BEAN_MR2){
                bRet=doReplaceIntentForStartActivityAPILow(args);
            }else{
                bRet=doReplaceIntentForStartActivityAPIHigh(args);
            }
            if(!bRet){
                setFakedResult(Activity.RESULT_CANCELED);
                return true;
            }
            return super.beforeInvoke(receiver, method, args);
        }
    }

    private static class getRunningAppProcesses extends HookedMethodHandler{

        public getRunningAppProcesses(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(invokeResult!=null&&invokeResult instanceof List){
                List<Object> infos= (List<Object>) invokeResult;
                if(infos.size()>0){
                    for(Object info:infos){
                        if(info instanceof ActivityManager.RunningAppProcessInfo){
                            ActivityManager.RunningAppProcessInfo myInfo= (ActivityManager.RunningAppProcessInfo) info;
                            if(myInfo.uid!= Process.myUid()){
                                continue;
                            }

                            List<String> pkgs=PluginManager.getInstance().getPackagesNameByPid(myInfo.pid);
                            String processName=PluginManager.getInstance().getProcessNameByPid(myInfo.pid);
                            if(processName!=null){
                                myInfo.processName=processName;
                            }
                            if(pkgs!=null&&pkgs.size()>0){
                                ArrayList<String> ls=new ArrayList<>();
                                if(myInfo.pkgList!=null){
                                    for(String s:myInfo.pkgList){
                                        if(!ls.contains(s)){
                                            ls.add(s);
                                        }
                                    }
                                }
                                for(String s:pkgs){
                                    if(!ls.contains(s)){
                                        ls.add(s);
                                    }
                                }
                                myInfo.pkgList=ls.toArray(new String[ls.size()]);
                            }
                        }
                    }
                }
            }
        }
    }

    private static ActivityInfo resolveActivity(Intent intent) throws RemoteException{
        return PluginManager.getInstance().resolveActivityInfo(intent,0);
    }

    private static int findFirstIntentIndexInArgs(Object[] args) {
        if (args != null && args.length > 0) {
            int i = 0;
            for (Object arg : args) {
                if (arg != null && arg instanceof Intent) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    private static boolean isPackagePlugin(String packageName) throws RemoteException{
        return PluginManager.getInstance().isPluginPackage(packageName);
    }

    private static ComponentName selectProxyActivity(Intent intent){
        try{
            if(intent!=null){
                ActivityInfo proxyInfo=PluginManager.getInstance().selectStubActivityInfo(intent);
                if(proxyInfo!=null){
                    return new ComponentName(proxyInfo.packageName,proxyInfo.name);
                }
            }
        }catch (Exception e){
        }
        return null;
    }

    private static class startService extends HookedMethodHandler{

        private ServiceInfo info=null;

        public startService(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            info=replaceFirstServiceIntentOfArgs(args);
            return super.beforeInvoke(receiver, method, args);
        }

        @Override
        protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Throwable {
            if(invokeResult instanceof ComponentName){
                if(info!=null){
                    setFakedResult(new ComponentName(info.packageName,info.name));
                }
            }
            info=null;
            super.afterInvoke(receiver, method, args, invokeResult);
        }
    }

    private static ServiceInfo replaceFirstServiceIntentOfArgs(Object[] args) throws RemoteException{
        int intentOfArgIndex=findFirstIntentIndexInArgs(args);
        if(args!=null&&args.length>1&&intentOfArgIndex>=0){
            Intent intent= (Intent) args[intentOfArgIndex];
            ServiceInfo serviceInfo=resolveService(intent);
            if(serviceInfo!=null&&isPackagePlugin(serviceInfo.packageName)){
                ServiceInfo proxyService=selectProxyService(intent);
                if(proxyService!=null){
                    Intent newIntent=new Intent();
                    newIntent.setAction(proxyService.name+new Random().nextInt());
                    newIntent.setClassName(proxyService.packageName,proxyService.name);
                    newIntent.putExtra(Env.EXTRA_TARGET_INTENT,intent);
                    newIntent.setFlags(intent.getFlags());
                    args[intentOfArgIndex]=newIntent;
                    return serviceInfo;
                }
            }
        }
        return null;
    }

    private static ServiceInfo resolveService(Intent intent) throws RemoteException{
        return PluginManager.getInstance().resolveServiceInfo(intent,0);
    }

    private static ServiceInfo selectProxyService(Intent intent) {
        try {
            if (intent != null) {
                ServiceInfo proxyInfo = PluginManager.getInstance().selectStubServiceInfo(intent);
                if (proxyInfo != null) {
                    return proxyInfo;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


















