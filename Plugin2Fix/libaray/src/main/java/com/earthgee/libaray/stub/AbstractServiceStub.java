package com.earthgee.libaray.stub;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zhaoruixuan on 2017/5/27.
 */
public class AbstractServiceStub extends Service{

    private boolean isRunning=false;

    private static ServicesManager mCreator=ServicesManager.getDefault();

    private Object sLock=new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning=true;
    }

    @Override
    public void onDestroy() {
        try{
            mCreator.onDestory();
        }catch (Exception e){
        }
        super.onDestroy();
        isRunning=false;
        try{
            synchronized (sLock){
                sLock.notifyAll();
            }
        }catch (Exception e){
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        try{
            if(intent!=null){
                if(intent.getBooleanExtra("ActionKillSelf",false)){
                    startKillSelf();
                    if(!ServicesManager.getDefault().hasServiceRunning()){
                        stopSelf(startId);
                        boolean stopService=getApplication().stopService(intent);
                    }
                }
            }else{
                mCreator.onStart(this,intent,0,startId);
            }
        }catch (Exception e){
        }
        super.onStart(intent, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        try {
            if (intent != null) {
                return mCreator.onBind(this, intent);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static void startKillService(Context context,Intent service){
        service.putExtra("ActionKillSelf",true);
        context.startService(service);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        try{
            if(rootIntent!=null){
                mCreator.onTaskRemoved(this,rootIntent);
            }
        }catch (Exception e){
        }
    }

    @Override
    public void onRebind(Intent intent) {
        try {
            if (intent != null) {
                mCreator.onRebind(this, intent);
            }
        } catch (Exception e) {
        }
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            if (intent != null) {
                return mCreator.onUnbind(intent);
            }
        } catch (Exception e) {
        }
        return false;
    }

}
















