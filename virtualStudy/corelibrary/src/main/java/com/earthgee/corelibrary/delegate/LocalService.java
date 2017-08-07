package com.earthgee.corelibrary.delegate;

import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.internal.LoadedPlugin;
import com.earthgee.corelibrary.utils.PluginUtil;
import com.earthgee.corelibrary.utils.ReflectUtil;

import java.lang.reflect.Method;

/**
 * Created by zhaoruixuan on 2017/8/1.
 */
public class LocalService extends Service{

    public static final String EXTRA_TARGET="target";
    public static final String EXTRA_COMMAND="command";
    public static final String EXTRA_PLUGIN_LOCATION="plugin_location";

    public static final int EXTRA_COMMAND_START_SERVICE=1;
    public static final int EXTRA_COMMAND_STOP_SERVICE=2;
    public static final int EXTRA_COMMAND_BIND_SERVICE=3;
    public static final int EXTRA_COMMAND_UNBIND_SERVICE=4;

    private PluginManager mPluginManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPluginManager=PluginManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null==intent||!intent.hasExtra(EXTRA_TARGET)||!intent.hasExtra(EXTRA_COMMAND)){
            return START_STICKY;
        }

        Intent target=intent.getParcelableExtra(EXTRA_TARGET);
        int command=intent.getIntExtra(EXTRA_COMMAND,0);
        if(null==target||command<=0){
            return START_STICKY;
        }

        ComponentName component=target.getComponent();
        LoadedPlugin plugin=mPluginManager.getLoadedPlugin(component);

        switch (command){
            case EXTRA_COMMAND_START_SERVICE:{
                ActivityThread mainThread= (ActivityThread) ReflectUtil.getActivityThread(getBaseContext());
                IApplicationThread appThread=mainThread.getApplicationThread();
                Service service;

                if(this.mPluginManager.getComponentsHandler().isServiceAvailable(component)){
                    service=this.mPluginManager.getComponentsHandler().getService(component);
                }else{
                    try{
                        service= (Service) plugin.getClassLoader().loadClass(component.getClassName()).newInstance();

                        Application app=plugin.getApplication();
                        IBinder token=appThread.asBinder();
                        Method attach=service.getClass().getMethod("attach", Context.class,ActivityThread.class,
                                String.class,IBinder.class,Application.class,Object.class);
                        IActivityManager am=mPluginManager.getActivityManager();

                        attach.invoke(service,plugin.getPluginContext(),mainThread,component.getClassName(),token,app,am);
                        service.onCreate();
                        this.mPluginManager.getComponentsHandler().rememberService(component,service);
                    }catch (Throwable t){
                        return START_STICKY;
                    }
                }

                service.onStartCommand(target,0,this.mPluginManager.
                        getComponentsHandler().getServiceCounter(service).getAndIncrement());
                break;
            }
            case EXTRA_COMMAND_STOP_SERVICE:{
                Service service=this.mPluginManager.getComponentsHandler().forgetService(component);
                if(null!=service){
                    try{
                        service.onDestroy();
                    }catch (Exception e){
                    }
                }else{
                }
                break;
            }
            case EXTRA_COMMAND_BIND_SERVICE:{
                ActivityThread mainThread= (ActivityThread) ReflectUtil.getActivityThread(getBaseContext());
                IApplicationThread appThread=mainThread.getApplicationThread();
                Service service=null;

                if(this.mPluginManager.getComponentsHandler().isServiceAvailable(component)){
                    service=this.mPluginManager.getComponentsHandler().getService(component);
                }else{
                    try{
                        service= (Service) plugin.getClassLoader().loadClass(component.getClassName()).newInstance();

                        Application app=plugin.getApplication();
                        IBinder token=appThread.asBinder();
                        Method attach=service.getClass().getMethod("attach",Context.class,ActivityThread.class,
                                String.class,IBinder.class,Application.class,Object.class);
                        IActivityManager am=mPluginManager.getActivityManager();

                        attach.invoke(service,plugin.getPluginContext(),mainThread,component.getClassName(),token,app,am);
                        service.onCreate();
                        this.mPluginManager.getComponentsHandler().rememberService(component,service);
                    }catch (Throwable t){
                        t.printStackTrace();
                    }
                }
                try{
                    IBinder binder=service.onBind(target);
                    IBinder serviceConnection= PluginUtil.getBinder(intent.getExtras(),"sc");
                    IServiceConnection iServiceConnection=IServiceConnection.Stub.asInterface(serviceConnection);
                    iServiceConnection.connected(component,binder);
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            }
            case EXTRA_COMMAND_UNBIND_SERVICE:{
                Service service=this.mPluginManager.getComponentsHandler().forgetService(component);
                if(null!=service){
                    try {
                        service.onUnbind(target);
                        service.onDestroy();
                    }catch (Exception e){
                    }
                }
                break;
            }
        }

        return START_STICKY;
    }


}































