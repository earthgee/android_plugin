package com.earthgee.library.hook.handle;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;

import com.earthgee.library.am.RunningActivities;
import com.earthgee.library.core.Env;
import com.earthgee.library.core.PluginProcessManager;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.HookFactory;
import com.earthgee.library.hook.binder.IWindowManagerBinderHook;
import com.earthgee.library.hook.proxy.IPackageManagerHook;
import com.earthgee.library.pm.PluginManager;

/**
 * Created by zhaoruixuan on 2017/5/8.
 */
public class PluginInstrumentation extends Instrumentation{

    protected Instrumentation mTarget;
    private final Context mHostContext;
    private boolean enable=true;

    public void setEnable(boolean enable){
        this.enable=enable;
    }

    public PluginInstrumentation(Context hostContext,Instrumentation target){
        mTarget=target;
        mHostContext=hostContext;
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if(enable){
            IWindowManagerBinderHook.fixWindowManagerHook(activity);
            IPackageManagerHook.fixContextPackageManager(activity);
            try{
                PluginProcessManager.fakeSystemService(mHostContext,activity);
            }catch (Exception e){
            }

            try{
                onActivityCreated(activity);
            }catch (RemoteException e){
            }

            try{
                fixBaseContextImplOpsPackage(activity.getBaseContext());
            }catch (Exception e){
            }

            try{
                fixBaseContextImplContentResolverOpsPackage(activity.getBaseContext());
            }catch (Exception e){
            }
        }

        if(mTarget!=null){
            mTarget.callActivityOnCreate(activity,icicle);
        }else{
            super.callActivityOnCreate(activity, icicle);
        }
    }

    private void onActivityCreated(Activity activity) throws RemoteException{
        try{
            Intent targetIntent=activity.getIntent();
            if(targetIntent!=null){
                ActivityInfo targetInfo=targetIntent.getParcelableExtra(Env.EXTRA_TARGET_INFO);
                ActivityInfo stubInfo=targetIntent.getParcelableExtra(Env.EXTRA_STUB_INFO);
                if(targetInfo!=null&&stubInfo!=null){
                    RunningActivities.onActivityCreate(activity,targetInfo,stubInfo);
                    if(activity.getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED&&
                            targetInfo.screenOrientation!=ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
                        activity.setRequestedOrientation(targetInfo.screenOrientation);
                    }
                    PluginManager.getInstance().onActivityCreate(stubInfo,targetInfo);
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                        fixTaskDescription(activity,targetInfo);
                    }
                }
            }
        }catch (Exception e){
        }
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        if(mTarget!=null){
            mTarget.callActivityOnDestroy(activity);
        }else{
            super.callActivityOnDestroy(activity);
        }
        RunningActivities.onActivityDestory(activity);

        if(enable){
            try{
                onActivityDestory(activity);
            }catch (Exception e){
            }
        }
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        if(enable){
            IPackageManagerHook.fixContextPackageManager(app);
            try{
                PluginProcessManager.fakeSystemService(mHostContext,app);
            }catch (Exception e){
            }

            try{
                fixBaseContextImplOpsPackage(app.getBaseContext());
            }catch (Exception e){
            }

            try{
                fixBaseContextImplResolverOpsPackage(app.getBaseContext());
            }catch (Exception e){
            }
        }

        try{
            HookFactory.getInstance().onCallApplicationOnCreate(mHostContext,app);
        }catch (Exception e){
        }

        if(mTarget!=null){
            mTarget.callApplicationOnCreate(app);
        }else{
            super.callApplicationOnCreate(app);
        }

        if(enable){
            try{
                PluginProcessManager.registerStaticReceiver(app,app.getApplicationInfo(),app.getClassLoader());
            }catch (Exception e){
            }
        }
        super.callApplicationOnCreate(app);
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        try{
            Intent newIntent=intent.getParcelableExtra(Env.EXTRA_TARGET_INTENT);
            if(newIntent!=null){
                intent=newIntent;
            }
        }catch (Throwable e){
        }

        if(enable){
            try{
                onActivityOnNewIntent(activity,intent);
            }catch (RemoteException e){
            }
        }

        if(mTarget!=null){
            mTarget.callActivityOnNewIntent(activity,intent);
        }else{
            super.callActivityOnNewIntent(activity, intent);
        }
    }
}

























