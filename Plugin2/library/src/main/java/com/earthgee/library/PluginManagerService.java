package com.earthgee.library;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.earthgee.library.pm.IPluginManagerImpl;

/**
 * Created by zhaoruixuan on 2017/4/13.
 * 插件管理服务
 */
public class PluginManagerService extends Service{

    private IPluginManagerImpl mPluginPackageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        keepAlive();
        mPluginPackageManager=new IPluginManagerImpl(this);
        mPluginPackageManager.onCreate();
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

    @Override
    public void onDestroy() {
        try{
            mPluginPackageManager.onDestroy();
        }catch (Exception e){

        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mPluginPackageManager;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IActivityManagerHookHandle.getIntentSender.handlePendingIntent(this,intent);
        return super.onStartCommand(intent, flags, startId);
    }

}



















