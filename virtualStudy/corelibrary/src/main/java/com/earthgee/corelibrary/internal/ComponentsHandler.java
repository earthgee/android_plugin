package com.earthgee.corelibrary.internal;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.ArrayMap;

import com.earthgee.corelibrary.PluginManager;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class ComponentsHandler {

    private PluginManager mPluginManager;
    private Context mContext;

    private HashMap<ComponentName,Service> mServices=new HashMap<>();
    private HashMap<Service,AtomicInteger> mServiceCounters=new HashMap<>();
    private HashMap<IBinder,Intent> mBoundServices=new HashMap<>();

    private StubActivityInfo mStubActivityInfo=new StubActivityInfo();

    public ComponentsHandler(PluginManager pluginManager){
        mPluginManager=pluginManager;
        mContext=pluginManager.getHostContext();
    }

    public Intent transformIntentToExplicitAsNeeded(Intent intent){
        ComponentName component=intent.getComponent();
        if(component==null){
            ResolveInfo info=mPluginManager.resolveActivity(intent);
            if(info!=null&&info.activityInfo!=null){
                component=new ComponentName(info.activityInfo.packageName,info.activityInfo.name);
                intent.setComponent(component);
            }
        }

        return intent;
    }

    public void markIntentIfNeeded(Intent intent){
        if(intent.getComponent()==null){
            return;
        }

        String targetPackageName=intent.getComponent().getPackageName();
        String targetClassName=intent.getComponent().getClassName();
        if(!targetPackageName.equals(mContext.getPackageName())
                &&mPluginManager.getLoadedPlugin(targetPackageName)!=null){
            intent.putExtra(Constants.KEY_IS_PLUGIN,true);
            intent.putExtra(Constants.KEY_TARGET_PACKAGE,targetPackageName);
            intent.putExtra(Constants.KEY_TARGET_ACTIVITY,targetClassName);
            dispatchStubActivity(intent);
        }
    }

    private void dispatchStubActivity(Intent intent){
        ComponentName component=intent.getComponent();
        String targetClassName=intent.getComponent().getClassName();
        LoadedPlugin loadedPlugin=mPluginManager.getLoadedPlugin(intent);
        ActivityInfo info=loadedPlugin.getActivityInfo(component);
        if(info==null){
            throw new RuntimeException("can not find "+component);
        }
        int launchMode=info.launchMode;
        Resources.Theme themeObj=loadedPlugin.getResources().newTheme();
        themeObj.applyStyle(info.theme,true);
        String stubActivity=mStubActivityInfo.getStubActivity(targetClassName,launchMode,themeObj);
        intent.setClassName(mContext,stubActivity);
    }

    public Service getService(ComponentName component){
        return mServices.get(component);
    }

    public void rememberService(ComponentName componentName,Service service){
        synchronized (mServices){
            mServices.put(componentName,service);
            mServiceCounters.put(service,new AtomicInteger(0));
        }
    }

    public Service forgetService(ComponentName component){
        synchronized (this.mServices){
            Service service=this.mServices.remove(component);
            this.mServiceCounters.remove(service);
            return service;
        }
    }

    public void remberIServiceConnection(IBinder iServiceConnection,Intent intent){
        synchronized (this.mBoundServices){
            mBoundServices.put(iServiceConnection, intent);
        }
    }

    public Intent forgetIServiceConnection(IBinder iServiceConnection){
        synchronized (this.mBoundServices){
            Intent intent=this.mBoundServices.remove(iServiceConnection);
            return intent;
        }
    }

    public boolean isServiceAvailable(ComponentName component){
        return this.mServices.containsKey(component);
    }

    public AtomicInteger getServiceCounter(Service service){
        return this.mServiceCounters.get(service);
    }

}
















