package com.earthgee.libaray;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.earthgee.libaray.pm.IPluginManagerImpl;

/**
 * Created by zhaoruixuan on 2017/5/25.
 */
public class PluginManagerService extends Service{

    private IPluginManagerImpl mPluginManager;

    @Override
    public void onCreate() {
        super.onCreate();
        keepAlive();
        mPluginManager=new IPluginManagerImpl(this);
        mPluginManager.onCreate();
    }

    private void keepAlive(){
        try{
            Notification notification=new Notification();
            notification.flags|=Notification.FLAG_NO_CLEAR;
            notification.flags|=Notification.FLAG_ONGOING_EVENT;
            startForeground(0,notification);
        }catch (Throwable e){
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPluginManager;
    }

    @Override
    public void onDestroy() {
        try{
            mPluginManager.onDestory();
        }catch (Exception e){
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}

















