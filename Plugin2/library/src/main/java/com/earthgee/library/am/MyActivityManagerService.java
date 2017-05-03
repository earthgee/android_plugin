package com.earthgee.library.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;

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

}
