package com.earthgee.library.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.earthgee.library.IApplicationCallback;
import com.earthgee.library.pm.IPluginManagerImpl;
import com.earthgee.library.pm.parser.PluginPackageParser;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/14.
 */
public abstract class BaseActivityManagerService {

    //service回调client的列表
    private RemoteCallbackList<IApplicationCallback> mRemoteCallbackList;

    protected Context mHostContext;

    public BaseActivityManagerService(Context hostContext){
        mHostContext=hostContext;
    }

    public abstract List<String> getPackageNamesByPid(int pid);

    public abstract ActivityInfo selectStubActivityInfo(int callingPid,int callingUid,ActivityInfo targetInfo) throws RemoteException;

    public abstract ServiceInfo selectStubServiceInfo(int callingPid,int callingUid,ServiceInfo targetInfo) throws RemoteException;

    public abstract ProviderInfo selectStubProviderInfo(int callingPid,int callingUid,ProviderInfo targetInfo) throws RemoteException;

    public abstract ServiceInfo getTargetServiceInfo(int callingPid,int callingUid,ServiceInfo stubInfo) throws RemoteException;

    public void onPkgDeleted(Map<String,PluginPackageParser> pluginCache,PluginPackageParser parser,String packageName) throws RemoteException{
    }

    public void onPkgInstalled(Map<String,PluginPackageParser> pluginCache,PluginPackageParser parser,String packageName) throws RemoteException{
    }

    private static class ProcessCookie{
        private final int pid;
        private final int uid;

        private ProcessCookie(int pid,int uid){
            this.pid=pid;
            this.uid=uid;
        }
    }

    public boolean registerApplicationCallback(int callingPid,int callingUid,IApplicationCallback callback){
        return mRemoteCallbackList.register(callback,new ProcessCookie(callingPid,callingUid));
    }

    public boolean unregisterApplicationCallback(int callingPid,int callingUid,IApplicationCallback callback){
        return mRemoteCallbackList.unregister(callback);
    }

    public void onCreate(IPluginManagerImpl pluginManagerImpl) throws Exception{
        if(mRemoteCallbackList==null){
            mRemoteCallbackList=new MyRemoteCallbackList();
        }
    }

    public void onReportMyProcessName(int callingPid,int callingUid,String stubProcessName,String targetProcessName,String targetPkg) {

    }

    private class MyRemoteCallbackList extends RemoteCallbackList<IApplicationCallback>{

        @Override
        public void onCallbackDied(IApplicationCallback callback, Object cookie) {
            super.onCallbackDied(callback, cookie);
            if(cookie!=null&&cookie instanceof ProcessCookie){
                ProcessCookie p= (ProcessCookie) cookie;
                onProcessDied(p.pid,p.uid);
            }
        }
    }

    private static class ProcessCookie{
        private final int pid;
        private final int uid;

        private ProcessCookie(int pid,int uid){
            this.pid=pid;
            this.uid=uid;
        }
    }

    public void onDestroy(){
        mRemoteCallbackList.kill();
        mRemoteCallbackList=null;
    }

}





















