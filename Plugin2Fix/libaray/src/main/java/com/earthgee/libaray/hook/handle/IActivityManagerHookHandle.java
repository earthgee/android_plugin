package com.earthgee.libaray.hook.handle;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.libaray.core.Env;
import com.earthgee.libaray.hook.BaseHookHandle;
import com.earthgee.libaray.hook.HookedMethodHandler;
import com.earthgee.libaray.pm.PluginManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

        @Override
        protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
            //todo
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

}


















