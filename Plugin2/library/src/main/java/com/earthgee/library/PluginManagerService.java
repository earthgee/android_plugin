package com.earthgee.library;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zhaoruixuan on 2017/4/13.
 * 插件管理服务
 */
public class PluginManagerService extends Service{

    @Override
    public void onCreate() {
        super.onCreate();
        keepAlive();

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
        return null;
    }
}
