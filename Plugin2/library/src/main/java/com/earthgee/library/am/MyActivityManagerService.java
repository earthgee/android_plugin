package com.earthgee.library.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;

import com.earthgee.library.IApplicationCallback;
import com.earthgee.library.pm.IPluginManagerImpl;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.util.AttributeCache;

import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/14.
 */
public class MyActivityManagerService extends BaseActivityManagerService{

    private RunningProcessList mRunningProcessList=new RunningProcessList();
    private StaticProcessList mStaticProcessList=new StaticProcessList();

    public MyActivityManagerService(Context hostContext){
        super(hostContext);
        mRunningProcessList.setContext(hostContext);
    }

    @Override
    public void onCreate(IPluginManagerImpl pluginManagerImpl) throws Exception {
        super.onCreate(pluginManagerImpl);
        AttributeCache.init(mHostContext);
        mStaticProcessList.onCreate(mHostContext);
        mRunningProcessList.setContext(mHostContext);
    }

    @Override
    public void onDestroy() {
        mRunningProcessList.clear();
        mStaticProcessList.clear();
        runProcessGC();
        super.onDestroy();
    }

    @Override
    public List<String> getPackageNamesByPid(int pid) {
        return null;
    }

    //为目标插件activity寻找合适的host插桩activity
    @Override
    public ActivityInfo selectStubActivityInfo(int callingPid, int callingUid, ActivityInfo targetInfo) throws RemoteException {
        runProcessGC();
        boolean Window_windowIsTranslucent=false;
        boolean Window_windowIsFloating=false;
        boolean Window_windowShowWallpaper=false;
        try{
            Class<?> R_Styleable_Class=Class.forName("com.android.internal.R$styleable");
            int[] R_styleable_Window= (int[]) FieldUtils.readStaticField(R_Styleable_Class,"Window");
            int R_Styleable_Window_windowIsTranslucent= (int) FieldUtils.readStaticField(R_Styleable_Class,"Window_windowIsTranslucent");
            int R_Styleable_Window_windowIsFloating= (int) FieldUtils.readStaticField(R_Styleable_Class,"Window_windowIsFloating");
            int R_Styleable_Window_windowShowWallpaper= (int) FieldUtils.readStaticField(R_Styleable_Class,"Window_windowShowWallpaper");

            AttributeCache.Entry ent=AttributeCache.instance().get(targetInfo.packageName,targetInfo.theme,R_styleable_Window);
            if(ent!=null&&ent.array!=null){
                Window_windowIsTranslucent=ent.array.getBoolean(R_Styleable_Window_windowIsTranslucent,false);
                Window_windowIsFloating=ent.array.getBoolean(R_Styleable_Window_windowIsFloating,false);
                Window_windowShowWallpaper=ent.array.getBoolean(R_Styleable_Window_windowShowWallpaper,false);
            }
        }catch (Throwable e){
        }

        boolean useDialogStyle=Window_windowIsTranslucent||Window_windowIsFloating||Window_windowShowWallpaper;

        String stubProcessName1=mRunningProcessList.getStubProcessByTarget(targetInfo);
        if(stubProcessName1!=null){
            List<ActivityInfo> stubInfos=mStaticProcessList.getActivityInfoForProcessName(stubProcessName1,useDialogStyle);
            for(ActivityInfo stubInfo:stubInfos){
                if(stubInfo.launchMode==targetInfo.launchMode){
                    if(stubInfo.launchMode==ActivityInfo.LAUNCH_MULTIPLE){
                        mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                        return stubInfo;
                    }else if(!mRunningProcessList.isStubInfoUsed(stubInfo,targetInfo,stubProcessName1)){
                        mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                        return stubInfo;
                    }
                }
            }
        }

        List<String> stubProcessNames=mStaticProcessList.getProcessNames();
        for(String stubProcessName:stubProcessNames){
            List<ActivityInfo> stubInfos=mStaticProcessList.getActivityInfoForProcessName(stubProcessName,useDialogStyle);
            if(mRunningProcessList.isProcessRunning(stubProcessName)){
                if(mRunningProcessList.isPkgEmpty(stubProcessName)){
                    for(ActivityInfo stubInfo:stubInfos){
                        if(stubInfo.launchMode==targetInfo.launchMode){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                            return stubInfo;
                        }else if(!mRunningProcessList.isStubInfoUsed(stubInfo,targetInfo,stubProcessName)){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                            return stubInfo;
                        }
                    }
                }else if(mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName,stubProcessName,targetInfo.processName)){
                    for(ActivityInfo stubInfo:stubInfos){
                        if(stubInfo.launchMode==targetInfo.launchMode){
                            if(stubInfo.launchMode==ActivityInfo.LAUNCH_MULTIPLE){
                                mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                                return stubInfo;
                            }else if(!mRunningProcessList.isStubInfoUsed(stubInfo,targetInfo,stubProcessName1)){
                                mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                                return stubInfo;
                            }
                        }
                    }
                }
            }else{
                for(ActivityInfo stubInfo:stubInfos){
                    if(stubInfo.launchMode==targetInfo.launchMode){
                        if(stubInfo.launchMode==ActivityInfo.LAUNCH_MULTIPLE){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                            return stubInfo;
                        }else if(!mRunningProcessList.isStubInfoUsed(stubInfo,targetInfo,stubProcessName1)){
                            mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
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
        return null;
    }

    @Override
    public ProviderInfo selectStubProviderInfo(int callingPid, int callingUid, ProviderInfo targetInfo) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo getTargetServiceInfo(int callingPid, int callingUid, ServiceInfo stubInfo) throws RemoteException {
        return null;
    }

    private void runProcessGC(){
        //todo
        if(mHostContext==null){

        }
    }

    @Override
    public void onReportMyProcessName(int callingPid, int callingUid, String stubProcessName, String targetProcessName, String targetPkg) {
        mRunningProcessList.setProcessName(callingPid,stubProcessName,targetProcessName,targetPkg);
    }

    @Override
    public boolean registerApplicationCallback(int callingPid, int callingUid, IApplicationCallback callback) {
        boolean b=super.registerApplicationCallback(callingPid, callingUid, callback);
        mRunningProcessList.addItem(callingPid,callingUid);
    }
}






















