package com.earthgee.library.pm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.library.BuildConfig;
import com.earthgee.library.IApplicationCallback;
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
 */
public class PluginManager implements ServiceConnection{

    public static final String AUTHORITY_NAME= BuildConfig.AUTHORITY_NAME;

    private List<WeakReference<ServiceConnection>> sServiceConnection=
            Collections.synchronizedList(new ArrayList<WeakReference<ServiceConnection>>(1));

    private static PluginManager instance=null;
    private Context mHostContext;

    public static PluginManager getInstance(){
        if(instance==null){
            instance=new PluginManager();
        }
        return instance;
    }

    public void addServiceConnection(ServiceConnection sc){
        sServiceConnection.add(new WeakReference<ServiceConnection>(sc));
    }

    public void init(Context hostContext){
        mHostContext=hostContext;
        connectToService();
    }

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

    private IPluginManager mPluginManager;

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

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mPluginManager=IPluginManager.Stub.asInterface(service);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mPluginManager.waitForReady();
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


}




























