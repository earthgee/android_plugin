package com.earthgee.libaray.am;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.RemoteException;

import com.earthgee.libaray.helper.AttributeCache;
import com.earthgee.libaray.pm.IPluginManagerImpl;
import com.earthgee.libaray.reflect.FieldUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class MyActivityManagerService extends BaseActivityManagerService{

    private StaticProcessList mStaticProcessList=new StaticProcessList();
    private RunningProcessList mRunningProcessList=new RunningProcessList();

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
        //todo
        //runProcessGC();

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

        String stubProcessName1=mRunningProcessList.getStubProcessByTarget(targetInfo);
        if(stubProcessName1!=null){
            List<ActivityInfo> stubInfos=mStaticProcessList.getActivityInfoForProcessName(stubProcessName1,useDialogStyle);
            for(ActivityInfo stubInfo:stubInfos){
                if(stubInfo.launchMode==targetInfo.launchMode){
                    mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                    return stubInfo;
                }else if(!mRunningProcessList.isStubInfoUsed(stubInfo,targetInfo,stubProcessName1)){
                    mRunningProcessList.setTargetProcessName(stubInfo,targetInfo);
                }
            }
        }

        List<String> stubProcessNames=mStaticProcessList.getProcessNames();
        for(String stubProcessName:stubProcessNames){
            List<ActivityInfo> stubInfos=mStaticProcessList.getActivityInfoForProcessName(stubProcessName,useDialogStyle);
            if(mRunningProcessList.isProcessRunning(stubProcessName)){
                if (mRunningProcessList.isPkgEmpty(stubProcessName)) {
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
                }else if(mRunningProcessList.isPkgCanRunInProcess(targetInfo.packageName,stubProcessName,targetInfo.processName)){
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
            }else{
                for(ActivityInfo stubInfo:stubInfos){
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

//    private void runProcessGC(){
//        if(mHostContext==null){
//            return;
//        }
//
//        ActivityManager am= (ActivityManager) mHostContext.getSystemService(Context.ACTIVITY_SERVICE);
//        if(am==null){
//            return;
//        }
//
//        List<ActivityManager.RunningAppProcessInfo> infos=am.getRunningAppProcesses();
//        List<ActivityManager.RunningAppProcessInfo> myInfos=new ArrayList<>();
//        if(infos==null||infos.size()<0){
//            return;
//        }
//
//        List<String> pns=mStaticProcessList.getOtherProcessNames();
//        pns.add(mHostContext.getPackageName());
//        for(ActivityManager.RunningAppProcessInfo info:infos){
//            if(info.uid==android.os.Process.myUid()
//                    &&info.pid!=android.os.Process.myPid()
//                    &&!pns.contains(info.processName)&&
//                    mRunningProcessList.isPlugin(info.pid)
//                    &&!mRunningProcessList.isPersistentApplication(info.pid)){
//                myInfos.add(info);
//            }
//        }
//        Collections.sort(myInfos,sProcessComparator);
//        for(ActivityManager.RunningAppProcessInfo myInfo:myInfos){
//            if(myInfo.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE){
//                doGc(myInfo);
//            }else if(myInfo.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY){
//                doGc(myInfo);
//            }else if(myInfo.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
//                doGc(myInfo);
//            }else if(myInfo.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE){
//                doGc(myInfo);
//            }else if(myInfo.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE){
//
//            }else if(myInfo.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE){
//
//            }else if(myInfo.importance== ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
//
//            }
//        }
//    }
//
//    private void doGc(ActivityManager.RunningAppProcessInfo myInfo){
//        int activityCount=mRunningProcessList.getActivityCountByPid(myInfo.pid);
//        int serviceCount=mRunningProcessList.getServiceCountByPid(myInfo.pid);
//        int providerCount=mRunningProcessList.getProviderCountByPid(myInfo.pid);
//        if(activityCount<=0&&serviceCount<=0&&providerCount<=0){
//            try{
//                android.os.Process.killProcess(myInfo.pid);
//            }catch (Exception e){
//            }
//        }else if(activityCount<=0&&serviceCount>0){
//            //todo
//        }
//    }
//
//    @Override
//    public List<String> getPackageNamesByPid(int pid) {
//        //todo
//        return new ArrayList<String>(mRunningProcessList.getPackageNameByPid(pid));
//    }
//
//    @Override
//    public String getProcessNameByPid(int pid) {
//        //todo
//        return mRunningProcessList.getTargetProcessNameByPid(pid);
//    }

}











































