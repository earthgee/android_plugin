package com.earthgee.corelibrary.delegate;

import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.internal.LoadedPlugin;

import java.io.File;

/**
 * Created by zhaoruixuan on 2017/8/1.
 */
public class RemoteService extends LocalService{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null){
            return super.onStartCommand(intent, flags, startId);
        }

        Intent target=intent.getParcelableExtra(EXTRA_TARGET);
        if(target!=null){
            String pluginLocation=intent.getStringExtra(EXTRA_PLUGIN_LOCATION);
            ComponentName component=target.getComponent();
            LoadedPlugin plugin= PluginManager.getInstance(this).getLoadedPlugin(component);
            if(plugin==null&&pluginLocation!=null){
                try{
                    PluginManager.getInstance(this).loadPlugin(new File(pluginLocation));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        return super.onStartCommand(intent,flags,startId);
    }
}































