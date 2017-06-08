package com.earthgee.libaray.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.libaray.BuildConfig;
import com.earthgee.libaray.IApplicationCallback;
import com.earthgee.libaray.IPluginManager;
import com.earthgee.libaray.PluginManagerService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by zhaoruixuan on 2017/5/25.
 */
public class PluginManager implements ServiceConnection{

    public static final String ACTION_PACKAGE_ADDED = "com.morgoo.droidplugin.PACKAGE_ADDED";
    public static final String ACTION_PACKAGE_REMOVED = "com.morgoo.droidplugin.PACKAGE_REMOVED";

    public static final String EXTRA_APP_PERSISTENT = "com.morgoo.droidplugin.EXTRA_APP_PERSISTENT";

    public static final String STUB_AUTHORITY_NAME = BuildConfig.AUTHORITY_NAME;

    public static final int STUB_NO_ACTIVITY_MAX_NUM = 4;

    private Context mHostContext;
    private static PluginManager sInstance = null;

    private List<WeakReference<ServiceConnection>> sServiceConnection =
            Collections.synchronizedList(new ArrayList<WeakReference<ServiceConnection>>(1));

    public void addServiceConnection(ServiceConnection sc) {
        sServiceConnection.add(new WeakReference<ServiceConnection>(sc));
    }

    public void removeServiceConnection(ServiceConnection sc){
        Iterator<WeakReference<ServiceConnection>> iterator=sServiceConnection.iterator();
        while (iterator.hasNext()){
            WeakReference<ServiceConnection> wsc=iterator.next();
            if(wsc.get()==sc){
                iterator.remove();
            }
        }
    }

    public boolean isConnected(){
        return mHostContext!=null&&mPluginManager!=null;
    }

    public static PluginManager getInstance() {
        if (sInstance == null) {
            sInstance = new PluginManager();
        }
        return sInstance;
    }

    @Override
    public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
        mPluginManager=IPluginManager.Stub.asInterface(iBinder);
        new Thread(){
            @Override
            public void run() {
                try{
                    mPluginManager.waitForReady();
                    mPluginManager.registerApplicationCallback(new IApplicationCallback.Stub() {
                        @Override
                        public Bundle onCallback(Bundle extra) throws RemoteException {
                            return extra;
                        }
                    });

                    Iterator<WeakReference<ServiceConnection>> iterator=sServiceConnection.iterator();
                    while (iterator.hasNext()){
                        WeakReference<ServiceConnection> wsc=iterator.next();
                        ServiceConnection sc=wsc!=null?wsc.get():null;
                        if(sc!=null){
                            sc.onServiceConnected(componentName, iBinder);
                        }else{
                            iterator.remove();
                        }
                    }

                    mPluginManager.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            onServiceDisconnected(componentName);
                        }
                    },0);
                }catch (Exception e){
                }finally {
                    try{
                        synchronized (mWaitLock){
                            mWaitLock.notifyAll();
                        }
                    }catch (Exception e){
                    }
                }
            }
        }.start();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mPluginManager=null;

        Iterator<WeakReference<ServiceConnection>> iterator=sServiceConnection.iterator();
        while (iterator.hasNext()){
            WeakReference<ServiceConnection> wsc=iterator.next();
            ServiceConnection sc=wsc!=null?wsc.get():null;
            if(sc!=null){
                sc.onServiceDisconnected(componentName);
            }else{
                iterator.remove();
            }
        }
        connectToService();
    }

    private Object mWaitLock=new Object();

    public void waitForConnected(){
        if(isConnected()){
            return;
        }else{
            try{
                synchronized (mWaitLock){
                    mWaitLock.wait();
                }
            }catch (InterruptedException e){
            }
        }
    }

    private IPluginManager mPluginManager;

    public void connectToService() {
        if (mPluginManager == null) {
            try {
                Intent intent = new Intent(mHostContext, PluginManagerService.class);
                intent.setPackage(mHostContext.getPackageName());
                mHostContext.startService(intent);
                mHostContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
            }
        }
    }

    public void init(Context hostContext){
        mHostContext=hostContext;
        connectToService();
    }

    public Context getHostContext(){
        return mHostContext;
    }

    //////////////////////api
    public boolean isPluginPackage(String packageName) throws RemoteException{
        try{
            if(mHostContext==null){
                return false;
            }

            if(TextUtils.equals(mHostContext.getPackageName(),packageName)){
                return false;
            }

            if(mPluginManager!=null&&packageName!=null){
                return mPluginManager.isPluginPackage(packageName);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }

        return false;
    }

    public boolean isPluginPackage(ComponentName className) throws RemoteException{
        if(className==null){
            return false;
        }
        return isPluginPackage(className.getPackageName());
    }

    public PackageInfo getPackageInfo(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getPackageInfo(packageName, flags);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public int installPackage(String filePath,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null){
                int result=mPluginManager.installPackage(filePath,flags);
                return result;
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return -1;
    }

    public List<PackageInfo> getInstalledPackages(int flags) throws RemoteException{
        try{
            if(mPluginManager!=null){
                return mPluginManager.getInstalledPackages(flags);
            }
        }catch (RemoteException e){
        }catch (Exception e){
        }
        return null;
    }

    public void deletePackage(String packageName,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null){
                mPluginManager.deletePackage(packageName, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent,String resolvedType,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null&&intent!=null){
                return mPluginManager.queryIntentActivities(intent, resolvedType, flags);
            }
        }catch (RemoteException e){
        }catch (Exception e){
        }

        return null;
    }

    public ResolveInfo resolveIntent(Intent intent,String resolvedType,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null&&intent!=null){
                return mPluginManager.resolveIntent(intent, resolvedType, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ActivityInfo resolveActivityInfo(Intent intent,int flags) throws RemoteException{
        try {
            if (mPluginManager != null) {
                if (intent.getComponent() != null) {
                    return mPluginManager.getActivityInfo(intent.getComponent(), flags);
                } else {
                    ResolveInfo resolveInfo = mPluginManager.resolveIntent(intent, intent.resolveTypeIfNeeded(mHostContext.getContentResolver()), flags);
                    if (resolveInfo != null && resolveInfo.activityInfo != null) {
                        return resolveInfo.activityInfo;
                    }
                }
            } else {
            }
            return null;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public ActivityInfo selectStubActivityInfo(Intent pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubActivityInfoByIntent(pluginInfo);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public List<String> getPackagesNameByPid(int pid) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getPackageNameByPid(pid);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public String getProcessNameByPid(int pid) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getProcessNameByPid(pid);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public int checkSignatures(String pkg0, String pkg1) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.checkSignatures(pkg0, pkg1);
            } else {
                return PackageManager.SIGNATURE_NO_MATCH;
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            return PackageManager.SIGNATURE_NO_MATCH;
        }
    }

    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {

        try {
            if (className == null) {
                return null;
            }
            if (mPluginManager != null && className != null) {
                return mPluginManager.getActivityInfo(className, flags);
            } else {
            }
        } catch (RemoteException e) {
        } catch (Exception e) {
        }
        return null;
    }

    public void reportMyProcessName(String stubProcessName, String targetProcessName, String targetPkg) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.reportMyProcessName(stubProcessName, targetProcessName, targetPkg);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
    }

    public void onActivityCreated(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onActivityCreated(stubInfo, targetInfo);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
    }

    public ServiceInfo resolveServiceInfo(Intent intent, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                if (intent.getComponent() != null) {
                    return mPluginManager.getServiceInfo(intent.getComponent(), flags);
                } else {
                    ResolveInfo resolveInfo = mPluginManager.resolveIntent(intent, intent.resolveTypeIfNeeded(mHostContext.getContentResolver()), flags);
                    if (resolveInfo != null && resolveInfo.serviceInfo != null) {
                        return resolveInfo.serviceInfo;
                    }
                }
            } else {
            }
            return null;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public ServiceInfo selectStubServiceInfo(ServiceInfo pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubServiceInfo(pluginInfo);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public ServiceInfo selectStubServiceInfo(Intent pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubServiceInfoByIntent(pluginInfo);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public void onServiceCreated(ServiceInfo stubInfo, ServiceInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onServiceCreated(stubInfo, targetInfo);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
    }

    public void onServiceDestory(ServiceInfo stubInfo, ServiceInfo targetInfo) {
        try {
            if (mPluginManager != null) {
                mPluginManager.onServiceDestory(stubInfo, targetInfo);
            } else {
            }
        } catch (Exception e) {
        }
    }

    public List<ActivityInfo> getReceivers(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getReceivers(packageName, flags);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getReceiverIntentFilter(info);
            } else {
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

}

















