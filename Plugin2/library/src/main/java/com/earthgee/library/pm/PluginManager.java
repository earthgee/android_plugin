package com.earthgee.library.pm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.library.BuildConfig;
import com.earthgee.library.IApplicationCallback;
import com.earthgee.library.IPackageDataObserver;
import com.earthgee.library.IPluginManager;
import com.earthgee.library.PluginManagerService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/4/12.
 * 虚拟pms发起者
 */
public class PluginManager implements ServiceConnection{

    public static final String ACTION_PACKAGE_ADDED="com.earthgee.plugin.PACKAGE_ADDED";
    public static final String ACTION_PACKAGE_REMOVED="com.eathgee.plugin.PACKAGE_REMOVED";
    public static final String ACTION_SHORTCUT_PROXY="com.earthgee.plugin.ACTION_SHORTCUT_PROXY";

    public static final String AUTHORITY_NAME= BuildConfig.AUTHORITY_NAME;

    //单进程单flag可容纳最大activity数
    public static final int STUB_NO_ACTIVITY_MAX_NUM=4;

    private List<WeakReference<ServiceConnection>> sServiceConnection=
            Collections.synchronizedList(new ArrayList<WeakReference<ServiceConnection>>(1));

    private static PluginManager instance=null;
    private Context mHostContext;

    public Context getHostContext(){
        return mHostContext;
    }

    public static PluginManager getInstance(){
        if(instance==null){
            instance=new PluginManager();
        }
        return instance;
    }

    public void addServiceConnection(ServiceConnection sc){
        sServiceConnection.add(new WeakReference<ServiceConnection>(sc));
    }

    //插件pms开始工作
    public void init(Context hostContext){
        mHostContext=hostContext;
        connectToService();
    }

    /**
     * start,bind插件解析service
     */
    public void connectToService(){
        if(mPluginManager==null){
            try{
                Intent intent=new Intent(mHostContext, PluginManagerService.class);
                intent.setPackage(mHostContext.getPackageName());
                mHostContext.startService(intent);
                mHostContext.bindService(intent,this,Context.BIND_AUTO_CREATE);
            }catch (Exception e){
            }
        }
    }

    //IPluginManager binder java端代理对象
    private IPluginManager mPluginManager;

    //PluginManagerService绑定成功
    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mPluginManager=IPluginManager.Stub.asInterface(service);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mPluginManager.waitForReady();
                    //向service注册一个回调，并且通知自建AMS进程的启动
                    mPluginManager.registerApplicationCallback(new IApplicationCallback.Stub(){
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
                            sc.onServiceConnected(name,service);
                        }else{
                            iterator.remove();
                        }
                    }

                    //read
                    mPluginManager.asBinder().linkToDeath(new IBinder.DeathRecipient(){
                        @Override
                        public void binderDied() {
                            onServiceDisconnected(name);
                        }
                    },0);
                }catch (Throwable e){
                }finally {
                    try{
                        synchronized (mWaitLock){
                            mWaitLock.notifyAll();
                        }
                    }catch (Exception e){
                    }
                }
            }
        }).start();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mPluginManager=null;

        Iterator<WeakReference<ServiceConnection>> iterator=sServiceConnection.iterator();
        while (iterator.hasNext()){
            WeakReference<ServiceConnection> wsc=iterator.next();
            ServiceConnection sc=wsc!=null?wsc.get():null;
            if(sc!=null){
                sc.onServiceDisconnected(name);
            }else{
                iterator.remove();
            }
        }
        connectToService();
    }

    private Object mWaitLock=new Object();

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

    public ProviderInfo resolveContentProvider(String name,Integer flags) throws RemoteException{
        try {
            if(mPluginManager!=null&&name!=null){
                return mPluginManager.resolveContentProvider(name,flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ProviderInfo selectStubProviderInfo(String name) throws RemoteException{
        try {
            if(mPluginManager!=null){
                return mPluginManager.selectStubProviderInfo(name);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public PackageInfo getPackageInfo(String packageName,int flags) throws RemoteException{
        try {
            if(mPluginManager!=null){
                return mPluginManager.getPackageInfo(packageName, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ActivityInfo getActivityInfo(ComponentName className,int flags) throws Exception{
        try{
            if(className==null){
                return null;
            }

            if(mPluginManager!=null&&className!=null){
                return mPluginManager.getActivityInfo(className,flags);
            }
        }catch (RemoteException e){
        }catch (Exception e){
        }

        return null;
    }

    public PermissionInfo getPermissionInfo(String name,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null&&name!=null){
                return mPluginManager.getPermissionInfo(name, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null&&group!=null){
                return mPluginManager.queryPermissionsByGroup(group, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name,int flags) throws RemoteException{
        try {
            if(mPluginManager!=null&&name!=null){
                return mPluginManager.getPermissionGroupInfo(name,flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException{
        try{
            if(mPluginManager!=null){
                return mPluginManager.getAllPermissionGroups(flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ApplicationInfo getApplicationInfo(String packageName,int flags) throws RemoteException{
        try {
            if(mPluginManager!=null&&packageName!=null){
                return mPluginManager.getApplicationInfo(packageName, flags);
            }
        }catch (RemoteException e){
        }catch (Exception e){
        }
        return null;
    }

    public ActivityInfo getReceiverInfo(ComponentName className,int flags) throws Exception{
        if(className==null){
            return null;
        }
        try{
            if(mPluginManager!=null&&className!=null){
                return mPluginManager.getReceiverInfo(className,flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ServiceInfo getServiceInfo(ComponentName className,int flags) throws Exception{
        if(className==null){
            return null;
        }

        try{
            if(mPluginManager!=null&&className!=null){
                return mPluginManager.getServiceInfo(className, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ProviderInfo getProviderInfo(ComponentName className,int flags) throws Exception{
        try{
            if(mPluginManager!=null&&className!=null){
                return mPluginManager.getProviderInfo(className, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public int checkSignatures(String pkg0,String pkg1) throws RemoteException{
        try{
            if(mPluginManager!=null){
                return mPluginManager.checkSignatures(pkg0, pkg1);
            }else{
                return PackageManager.SIGNATURE_NO_MATCH;
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return PackageManager.SIGNATURE_NO_MATCH;
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

    public List<ResolveInfo> queryIntentActivities(Intent intent,String resolvedType,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null&&intent!=null){
                return mPluginManager.queryIntentActivities(intent, resolvedType, flags);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentReceivers(intent, resolvedType, flags);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public ResolveInfo resolveService(Intent intent, String resolvedType, Integer flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.resolveService(intent, resolvedType, flags);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentServices(intent, resolvedType, flags);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentContentProviders(intent, resolvedType, flags);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public List<PackageInfo> getInstalledPackages(int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getInstallPackages(flags);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getInstalledApplications(flags);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    public void forceStopPackage(String packageName) throws RemoteException{
        try{
            if(mPluginManager!=null){
                mPluginManager.forceStopPackage(packageName);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
    }

    public ActivityInfo resolveActivityInfo(Intent intent,int flags) throws RemoteException{
        try{
            if(mPluginManager!=null){
                if(intent.getComponent()!=null){
                    return mPluginManager.getActivityInfo(intent.getComponent(),flags);
                }else{
                    ResolveInfo resolveInfo=mPluginManager.resolveIntent(intent,
                            intent.resolveTypeIfNeeded(mHostContext.getContentResolver()),flags);
                    if(resolveInfo!=null&&resolveInfo.activityInfo!=null){
                        return resolveInfo.activityInfo;
                    }
                }
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ActivityInfo selectStubActivityInfo(Intent pluginInfo) throws RemoteException{
        try{
            if(mPluginManager!=null){
                return mPluginManager.selectStubActivityInfoByIntent(pluginInfo);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public ActivityInfo selectStubActivityInfo(ActivityInfo pluginInfo) throws RemoteException{
        try{
            if(mPluginManager!=null){
                return mPluginManager.selectStubActivityInfo(pluginInfo);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
        return null;
    }

    public boolean isConnected(){
        return mHostContext!=null&&mPluginManager!=null;
    }

    public void reportMyProcessName(String stubProcessName,String targetProcessName,
                                    String targetPkg) throws RemoteException{
        try{
            if(mPluginManager!=null){
                mPluginManager.reportMyProcessName(stubProcessName, targetProcessName, targetPkg);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
    }

    public void deleteApplicationCacheFiles(String packageName,final Object observer) throws RemoteException{
        try{
            if(mPluginManager!=null&&packageName!=null){
                mPluginManager.deleteApplicationCacheFiles(packageName, new IPackageDataObserver.Stub() {
                    @Override
                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {

                    }

                });
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
    }

    public void clearApplicationUserData(String packageName,final Object observer) throws RemoteException{
        try{
            if(mPluginManager!=null&&packageName!=null){
                mPluginManager.clearApplicationUserData(packageName,new IPackageDataObserver.Stub(){

                    @Override
                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {

                    }
                });
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
    }

    public void onActivityCreated(ActivityInfo stubInfo,ActivityInfo targetInfo) throws RemoteException{
        try{
            if(mPluginManager!=null){
                mPluginManager.onActivityCreated(stubInfo, targetInfo);
            }
        }catch (RemoteException e){
            throw e;
        }catch (Exception e){
        }
    }

    public void waitForConnected(){
        if(isConnected()){
            return;
        }else {
            try{
                synchronized (mWaitLock){
                    mWaitLock.wait();
                }
            }catch (InterruptedException e){

            }
        }
    }

    public void onActivityOnNewIntent(ActivityInfo stubInfo,ActivityInfo targetInfo,Intent intent) throws RemoteException{
        //todo
    }

    public void onActivityDestory(ActivityInfo stubInfo,ActivityInfo targetInfo) throws RemoteException{
        //todo
    }

}




























