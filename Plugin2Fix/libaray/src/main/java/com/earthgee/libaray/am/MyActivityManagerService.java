package com.earthgee.libaray.am;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.libaray.IApplicationCallback;
import com.earthgee.libaray.helper.AttributeCache;
import com.earthgee.libaray.helper.Utils;
import com.earthgee.libaray.pm.IPluginManagerImpl;
import com.earthgee.libaray.reflect.FieldUtils;
import com.earthgee.libaray.stub.AbstractServiceStub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class MyActivityManagerService extends BaseActivityManagerService {

    private StaticProcessList mStaticProcessList = new StaticProcessList();
    private RunningProcessList mRunningProcessList = new RunningProcessList();

    public MyActivityManagerService(Context hostContext) {
        super(hostContext);
        mRunningProcessList.setContext(mHostContext);
    }

    @Override
    public void onCreate(IPluginManagerImpl pluginManagerImpl) throws Exception {
        super.onCreate(pluginManagerImpl);
        AttributeCache.init(mHostContext);
        mStaticProcessList.onCreate(mHostContext);
        mRunningProcessList.setContext(mHostContext);
    }

    @Override
    public ActivityInfo selectStubActivityInfo(int callingPid, int callingUid, ActivityInfo targetInfo) throws RemoteException {
        runProcessGC();

        boolean Window_windowIsTranslucent = false;
        boolean Window_windowIsFloating = false;
        boolean Window_windowShowWallpaper = false;
        try {
            Class<?> R_Styleable_Class = Class.forName("com.android.internal.R$styleable");
            int[] R_Styleable_Window = (int[]) FieldUtils.readStaticField(R_Styleable_Class, "Window");
            int R_Styleable_Window_windowIsTranslucent = (int) FieldUtils.readStaticField(R_Styleable_Class, "Window_windowIsTranslucent");
            int R_Styleable_Window_windowIsFloating = (int) FieldUtils.readStaticField(R_Styleable_Class, "Window_windowIsFloating");
            int R_Styleable_Window_windowShowWallpaper = (int) FieldUtils.readStaticField(R_Styleable_Class, "Window_windowShowWallpaper");

            AttributeCache.Entry ent = AttributeCache.instance().get(targetInfo.packageName, targetInfo.theme,
                    R_Styleable_Window);
            if (ent != null && ent.array != null) {
                Window_windowIsTranslucent = ent.array.getBoolean(R_Styleable_Window_windowIsTranslucent, false);
                Window_windowIsFloating = ent.array.getBoolean(R_Styleable_Window_windowIsFloating, false);
                Window_windowShowWallpaper = ent.array.getBoolean(R_Styleable_Window_windowShowWallpaper, false);
            }

        } catch (Throwable e) {
        }

        boolean useDialogStyle = Window_windowIsTranslucent || Window_windowIsFloating || Window_windowShowWallpaper;

        //先从正在运行的进程中查找看是否有符合条件的进程，如果有则直接使用之
        String stubProcessName1 = mRunningProcessList.getStubProcessByTarget(targetInfo);
        if (stubProcessName1 != null) {
            List<ActivityInfo> stubInfos = mStaticProcessList.getActivityInfoForProcessName(stubProcessName1, useDialogStyle);
            for (ActivityInfo stubInfo : stubInfos) {
                if (stubInfo.launchMode == targetInfo.launchMode) {
                    mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                    return stubInfo;
                } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                    mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                }
            }
        }

        List<String> stubProcessNames = mStaticProcessList.getProcessNames();
        for (String stubProcessName : stubProcessNames) {
            List<ActivityInfo> stubInfos = mStaticProcessList.getActivityInfoForProcessName(stubProcessName, useDialogStyle);
            if (mRunningProcessList.isProcessRunning(stubProcessName)) {
                if (mRunningProcessList.isPkgEmpty(stubProcessName)) {
                    for (ActivityInfo stubInfo : stubInfos) {
                        if (stubInfo.launchMode == targetInfo.launchMode) {
                            if (stubInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            }
                        }
                    }
                } else if (mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName, stubProcessName, targetInfo.processName)) {
                    for (ActivityInfo stubInfo : stubInfos) {
                        if (stubInfo.launchMode == targetInfo.launchMode) {
                            if (stubInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                                mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                                return stubInfo;
                            }
                        }
                    }
                }
            } else {
                for (ActivityInfo stubInfo : stubInfos) {
                    if (stubInfo.launchMode == targetInfo.launchMode) {
                        if (stubInfo.launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        } else if (!mRunningProcessList.isStubInfoUsed(stubInfo, targetInfo, stubProcessName1)) {
                            mRunningProcessList.setTargetProcessName(stubInfo, targetInfo);
                            return stubInfo;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ServiceInfo selectStubServiceInfo(int callingPid, int callingUid, ServiceInfo targetInfo) throws RemoteException {
        runProcessGC();

        String stubProcessName1=mRunningProcessList.getStubProcessByTarget(targetInfo);
        if(stubProcessName1!=null){
            List<ServiceInfo> stubInfos=mStaticProcessList.getServiceInfoForProcessName(stubProcessName1);
            for(ServiceInfo stubInfo:stubInfos){
                if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                    mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                    return stubInfo;
                }
            }
        }

        List<String> stubProcessNames=mStaticProcessList.getProcessNames();
        for(String stubProcessName:stubProcessNames){
            List<ServiceInfo> stubInfos=mStaticProcessList.getServiceInfoForProcessName(stubProcessName);
            if(mRunningProcessList.isProcessRunning(stubProcessName)){
                if(mRunningProcessList.isPkgEmpty(stubProcessName)){
                    for(ServiceInfo stubInfo:stubInfos){
                        if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                            return stubInfo;
                        }
                    }
                }else if(mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName,stubProcessName,targetInfo.processName)){
                    for(ServiceInfo stubInfo:stubInfos){
                        if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                            return stubInfo;
                        }
                    }
                }
            }else{
                for(ServiceInfo stubInfo:stubInfos){
                    if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                        mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                        return stubInfo;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public ProviderInfo selectStubProviderInfo(int callingPid, int callingUid, ProviderInfo targetInfo) throws RemoteException {
        runProcessGC();

        String stubProcessName1=mRunningProcessList.getStubProcessByTarget(targetInfo);
        if(stubProcessName1!=null){
            List<ProviderInfo> stubInfos=mStaticProcessList.getProviderInfoForProcessName(stubProcessName1);
            for(ProviderInfo stubInfo:stubInfos){
                if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                    mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                    return stubInfo;
                }
            }
        }

        List<String> stubProcessNames=mStaticProcessList.getProcessNames();
        for(String stubProcessName:stubProcessNames){
            List<ProviderInfo> stubInfos=mStaticProcessList.getProviderInfoForProcessName(stubProcessName);
            if(mRunningProcessList.isProcessRunning(stubProcessName)){
                if(mRunningProcessList.isPkgEmpty(stubProcessName)){
                    for(ProviderInfo stubInfo:stubInfos){
                        if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                            return stubInfo;
                        }
                    }
                }else if(mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName,stubProcessName,targetInfo.processName)){
                    for(ProviderInfo stubInfo:stubInfos){
                        if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                            return stubInfo;
                        }
                    }
                }
            }else{
                for(ProviderInfo stubInfo:stubInfos){
                    if(!mRunningProcessList.isStubInfoUsed(stubInfo)){
                        mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                        return stubInfo;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getPackageNamesByPid(int pid) {
        return null;
    }

    private static final Comparator<ActivityManager.RunningAppProcessInfo> sProcessComparator = new Comparator<ActivityManager.RunningAppProcessInfo>() {
        @Override
        public int compare(ActivityManager.RunningAppProcessInfo lhs, ActivityManager.RunningAppProcessInfo rhs) {
            if (lhs.importance == rhs.importance) {
                return 0;
            } else if (lhs.importance > rhs.importance) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    @Override
    public boolean registerApplicationCallback(int callingPid, int callingUid, IApplicationCallback callback) {
        boolean b = super.registerApplicationCallback(callingPid, callingUid, callback);
        mRunningProcessList.addItem(callingPid, callingUid);
        //插件进程不会走这里
        if (callingPid == android.os.Process.myPid()) {
            String stubProcessName = Utils.getProcessName(mHostContext, callingPid);
            String targetProcessName = Utils.getProcessName(mHostContext, callingPid);
            String targetPkg = mHostContext.getPackageName();
            mRunningProcessList.setProcessName(callingPid, stubProcessName, targetProcessName, targetPkg);
        }
        if (TextUtils.equals(mHostContext.getPackageName(), Utils.getProcessName(mHostContext, callingPid))) {
            String stubProcessName = mHostContext.getPackageName();
            String targetProcessName = mHostContext.getPackageName();
            String targetPkg = mHostContext.getPackageName();
            mRunningProcessList.setProcessName(callingPid, stubProcessName, targetProcessName, targetPkg);
        }
        return b;
    }

    @Override
    public void onReportMyProcessName(int callingPid, int callingUid, String stubProcessName, String targetProcessName, String targetPkg) {
        mRunningProcessList.setProcessName(callingPid, stubProcessName, targetProcessName, targetPkg);
    }

    @Override
    public void onActivityCreated(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo) {
        mRunningProcessList.addActivityInfo(callingPid, callingUid, stubInfo, targetInfo);
    }

    @Override
    public void onServiceCreated(int callingPid, int callingUid, ServiceInfo stubInfo, ServiceInfo targetInfo) {
        mRunningProcessList.addServiceInfo(callingPid,callingUid,stubInfo,targetInfo);
    }

    @Override
    public void onServiceDestory(int callingPid, int callingUid, ServiceInfo stubInfo, ServiceInfo targetInfo) {
        mRunningProcessList.removeServiceInfo(callingPid, callingUid, stubInfo, targetInfo);
        runProcessGC();
    }

    @Override
    public void onProviderCreated(int callingPid, int callingUid, ProviderInfo stubInfo, ProviderInfo targetInfo) {
        mRunningProcessList.addProviderInfo(callingPid,callingUid,stubInfo,targetInfo);
    }

    private void runProcessGC() {
        if (mHostContext == null) {
            return;
        }

        ActivityManager am = (ActivityManager) mHostContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return;
        }

        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        List<ActivityManager.RunningAppProcessInfo> myInfos = new ArrayList<>();
        if (infos == null || infos.size() < 0) {
            return;
        }

        List<String> pns = mStaticProcessList.getOtherProcessNames();
        pns.add(mHostContext.getPackageName());
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.uid == android.os.Process.myUid()
                    && info.pid != android.os.Process.myPid()
                    && !pns.contains(info.processName) &&
                    mRunningProcessList.isPlugin(info.pid)
                    && !mRunningProcessList.isPersistentApplication(info.pid)) {
                myInfos.add(info);
            }
        }
        Collections.sort(myInfos, sProcessComparator);
        for (ActivityManager.RunningAppProcessInfo myInfo : myInfos) {
            if (myInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE) {
                doGc(myInfo);
            } else if (myInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY) {
                doGc(myInfo);
            } else if (myInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                doGc(myInfo);
            } else if (myInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
                doGc(myInfo);
            } else if (myInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {

            } else if (myInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {

            } else if (myInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

            }
        }
    }

    private void doGc(ActivityManager.RunningAppProcessInfo myInfo){
        int activityCount=mRunningProcessList.getActivityCountByPid(myInfo.pid);
        int serviceCount=mRunningProcessList.getServiceCountByPid(myInfo.pid);
        int providerCount=mRunningProcessList.getProviderCountByPid(myInfo.pid);
        if(activityCount<=0&&serviceCount<=0&&providerCount<=0){
            try{
                android.os.Process.killProcess(myInfo.pid);
            }catch (Exception e){
            }
        }else if(activityCount<=0&&serviceCount>0){
            List<String> names=mRunningProcessList.getStubServiceByPid(myInfo.pid);
            if(names!=null&&names.size()>0){
                for(String name:names){
                    Intent service=new Intent();
                    service.setClassName(mHostContext.getPackageName(),name);
                    AbstractServiceStub.startKillService(mHostContext,service);
                }
            }
        }
    }
//
//    @Override
//    public List<String> getPackageNamesByPid(int pid) {
//        //todo
//        return new ArrayList<String>(mRunningProcessList.getPackageNameByPid(pid));
//    }
//
    @Override
    public String getProcessNameByPid(int pid) {
        //todo
        return mRunningProcessList.getTargetProcessNameByPid(pid);
    }


    @Override
    protected void onProcessDied(int pid, int uid) {
        //todo
    }

    @Override
    public void onDestory() {
        //todo
    }
}











































