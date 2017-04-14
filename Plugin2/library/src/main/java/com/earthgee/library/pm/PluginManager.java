package com.earthgee.library.pm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

import com.earthgee.library.BuildConfig;
import com.earthgee.library.IPluginManager;
import com.earthgee.library.PluginManagerService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        }

        return null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
