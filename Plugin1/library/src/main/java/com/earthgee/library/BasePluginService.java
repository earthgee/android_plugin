package com.earthgee.library;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.IBinder;

import com.earthgee.library.utils.Constants;

/**
 * Created by zhaoruixuan on 2017/3/22.
 */
public class BasePluginService extends Service implements PluginServiceInterface{

    private Service mProxyService;
    private PluginPackage mPluginPackage;
    protected Service that=this;
    private int mFrom= Constants.FROM_INTERNAL;

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onRebind(Intent intent) {

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

    }

    @Override
    public void attach(Service proxyService, PluginPackage pluginPackage) {
        mProxyService=proxyService;
        mPluginPackage=pluginPackage;
        that=mProxyService;
        mFrom=Constants.FROM_EXTERNAL;
    }

    protected boolean inInternalCall(){
        return mFrom==Constants.FROM_INTERNAL;
    }

    @Override
    public Resources getResources() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getResources();
        }
        return mProxyService.getResources();
    }

    @Override
    public AssetManager getAssets() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getAssets();
        }
        return mProxyService.getAssets();
    }

    @Override
    public Object getSystemService(String name) {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getSystemService(name);
        }
        return mProxyService.getSystemService(name);
    }

    @Override
    public ClassLoader getClassLoader() {
        if(mFrom==Constants.FROM_INTERNAL){
            return super.getClassLoader();
        }
        return mProxyService.getClassLoader();
    }



}














