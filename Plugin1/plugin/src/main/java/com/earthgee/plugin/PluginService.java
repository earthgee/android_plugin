package com.earthgee.plugin;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.earthgee.library.BasePluginService;

/**
 * Created by zhaoruixuan on 2017/3/22.
 */
public class PluginService extends BasePluginService{

    private MyBinder binder=new MyBinder();

    private class MyBinder extends Binder implements PluginBridge{

        @Override
        public int sum(int a, int b) {
            return a+b;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(that,"service onCreate",Toast.LENGTH_SHORT).show();
        Log.d("earthgee1","service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("earthgee1","service onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
