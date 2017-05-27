package com.earthgee.libaray.am;

import android.content.Context;
import android.content.pm.ActivityInfo;
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

    private class MyRemoteCallbackList extends RemoteCallbackList<IApplicationCallback> {
        @Override
        public void onCallbackDied(IApplicationCallback callback, Object cookie) {
            super.onCallbackDied(callback, cookie);
            //todo
//            if (cookie != null && cookie instanceof ProcessCookie) {
//                ProcessCookie p = (ProcessCookie) cookie;
//                onProcessDied(p.pid, p.uid);
//            }
        }
    }

    public abstract ActivityInfo selectStubActivityInfo(int callingPid, int callingUid, ActivityInfo targetInfo) throws RemoteException;
    public abstract List<String> getPackageNamesByPid(int pid);

    public String getProcessNameByPid(int pid) {
        return null;
    }

}





















