package com.earthgee.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by zhaoruixuan on 2017/8/1.
 */
public class PluginService extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("earthgee2","pluginService onBind");
        return new Test();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("earthgee2","pluginService onUnBind");
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("earthgee2","pluginService onCreate");
    }

    @Override
    public void onDestroy() {
        Log.d("earthgee2","pluginService onDestory");
        super.onDestroy();
    }

    private class Test extends ITest.Stub{

        @Override
        public void test() throws RemoteException {
            Log.d("earthgee2","hello world");
        }

    }

}
