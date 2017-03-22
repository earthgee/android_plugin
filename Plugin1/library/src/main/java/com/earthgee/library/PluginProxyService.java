package com.earthgee.library;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zhaoruixuan on 2017/3/22.
 */
public class PluginProxyService extends Service implements PluginServiceAttachable{

    private PluginProxyServiceImpl mImpl=new PluginProxyServiceImpl(this);
    private PluginServiceInterface mRemoteService;
    private PluginManager mPluginManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mRemoteService==null){
            mImpl.init(intent);
        }
        super.onStartCommand(intent, flags, startId);
        return mRemoteService.onStartCommand(intent, flags, startId);
    }

    @Override
    public void attach(PluginServiceInterface remoteService) {
        mRemoteService=remoteService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(mRemoteService==null){
            mImpl.init(intent);
        }
        return mRemoteService.onBind(intent);
    }

    @Override
    public void onDestroy() {
        mRemoteService.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mRemoteService.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        mRemoteService.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        mRemoteService.onTrimMemory(level);
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        return mRemoteService.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        mRemoteService.onRebind(intent);
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mRemoteService.onTaskRemoved(rootIntent);
        super.onTaskRemoved(rootIntent);
    }

}













