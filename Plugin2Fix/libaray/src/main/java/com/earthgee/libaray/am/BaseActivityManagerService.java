package com.earthgee.libaray.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.earthgee.libaray.IApplicationCallback;
import com.earthgee.libaray.pm.IPluginManagerImpl;

import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public abstract class BaseActivityManagerService {

    protected Context mHostContext;

    public BaseActivityManagerService(Context hostContext) {
        mHostContext = hostContext;
    }

    private RemoteCallbackList<IApplicationCallback> mRemoteCallbackList;

    public void onCreate(IPluginManagerImpl pluginManagerImpl) throws Exception{
        if(mRemoteCallbackList==null){
            mRemoteCallbackList=new MyRemoteCallbackList();
        }
    }

    private static class ProcessCookie{
        private ProcessCookie(int pid,int uid){
            this.pid=pid;
            this.uid=uid;
        }

        private final int pid;
        private final int uid;
    }

    private class MyRemoteCallbackList extends RemoteCallbackList<IApplicationCallback> {
        @Override
        public void onCallbackDied(IApplicationCallback callback, Object cookie) {
            super.onCallbackDied(callback, cookie);
            if (cookie != null && cookie instanceof ProcessCookie) {
                ProcessCookie p = (ProcessCookie) cookie;
                onProcessDied(p.pid, p.uid);
            }
        }
    }

    protected void onProcessDied(int pid, int uid) {
        //wtf
    }

    public abstract ActivityInfo selectStubActivityInfo(int callingPid, int callingUid, ActivityInfo targetInfo) throws RemoteException;
    public abstract List<String> getPackageNamesByPid(int pid);
    public abstract ServiceInfo selectStubServiceInfo(int callingPid, int callingUid, ServiceInfo targetInfo) throws RemoteException;

    public String getProcessNameByPid(int pid) {
        return null;
    }

    public void onReportMyProcessName(int callingPid, int callingUid, String stubProcessName, String targetProcessName, String targetPkg) {
    }

    protected void sendCallback(Bundle extra){
        if(mRemoteCallbackList!=null){
            int i=mRemoteCallbackList.beginBroadcast();
            while (i>0){
                i--;
                try{
                    mRemoteCallbackList.getBroadcastItem(i).onCallback(extra);
                }catch (RemoteException e){
                }
            }
            mRemoteCallbackList.finishBroadcast();
        }
    }

    public boolean registerApplicationCallback(int callingPid,int callingUid,IApplicationCallback callback){
        return mRemoteCallbackList.register(callback,new ProcessCookie(callingPid,callingUid));
    }

    public boolean unregisterApplicationCallback(int callingPid,int callingUid,IApplicationCallback callback){
        return mRemoteCallbackList.unregister(callback);
    }

    public void onActivityCreated(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo) {
    }

    public void onDestory(){
        mRemoteCallbackList.kill();
        mRemoteCallbackList=null;
    }

}





















