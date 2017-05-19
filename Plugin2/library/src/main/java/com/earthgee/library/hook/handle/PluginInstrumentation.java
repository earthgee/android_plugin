package com.earthgee.library.hook.handle;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import com.earthgee.library.am.RunningActivities;
import com.earthgee.library.core.Env;
import com.earthgee.library.core.PluginProcessManager;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.HookFactory;
import com.earthgee.library.hook.binder.IWindowManagerBinderHook;
import com.earthgee.library.hook.proxy.IPackageManagerHook;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/8.
 * 对Instrumentation进行hook
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
    public void callActivityOnCreate(Activity activity, Bundle icicle)
    {
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
                    PluginManager.getInstance().onActivityCreated(stubInfo,targetInfo);
                    if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                        fixTaskDescription(activity,targetInfo);
                    }
                }
            }
        }catch (Exception e){
        }
    }

    private void fixTaskDescription(Activity activity,ActivityInfo targetInfo){
        try{
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                PackageManager pm=mHostContext.getPackageManager();
                String label=String.valueOf(targetInfo.loadLabel(pm));
                Drawable icon=targetInfo.loadIcon(pm);
                Bitmap bitmap=null;
                if(icon instanceof BitmapDrawable){
                    bitmap=((BitmapDrawable)icon).getBitmap();
                }
                if(bitmap!=null){
                    activity.setTaskDescription(new ActivityManager.TaskDescription(label,bitmap));
                }else{
                    activity.setTaskDescription(new ActivityManager.TaskDescription(label));
                }
            }
        }catch (Throwable e){
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

    private void onActivityDestory(Activity activity) throws RemoteException{
        Intent targetIntent=activity.getIntent();
        if(targetIntent!=null){
            ActivityInfo targetInfo=targetIntent.getParcelableExtra(Env.EXTRA_TARGET_INFO);
            ActivityInfo stubInfo=targetIntent.getParcelableExtra(Env.EXTRA_STUB_INFO);
            if(targetInfo!=null&&stubInfo!=null){
                PluginManager.getInstance().onActivityDestory(stubInfo,targetInfo);
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
                fixBaseContextImplContentResolverOpsPackage(app.getBaseContext());
            }catch (Exception e){
            }
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

    private void onActivityOnNewIntent(Activity activity,Intent intent) throws RemoteException{
        try{
            Intent targetIntent=activity.getIntent();
            if(targetIntent!=null){
                ActivityInfo targetInfo=targetIntent.getParcelableExtra(Env.EXTRA_TARGET_INFO);
                ActivityInfo stubInfo=targetIntent.getParcelableExtra(Env.EXTRA_STUB_INFO);
                if(targetInfo!=null&&stubInfo!=null){
                    RunningActivities.onActivityOnNewIntent(activity,targetInfo,stubInfo,intent);
                    PluginManager.getInstance().onActivityOnNewIntent(stubInfo,targetInfo,intent);
                }
            }
        }catch (Exception e){
        }
    }

    private void fixBaseContextImplOpsPackage(Context context) throws IllegalAccessException{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                &&context!=null&&!TextUtils.equals(context.getPackageName(),mHostContext.getPackageName())){
            Context baseContext=context;
            Class clazz=baseContext.getClass();
            Field mOpPackageName=FieldUtils.getDeclaredField(clazz,"mOpPackageName",true);
            if(mOpPackageName!=null){
                Object valueObj=mOpPackageName.get(baseContext);
                if(valueObj instanceof String){
                    String opPackageName= (String) valueObj;
                    if(!TextUtils.equals(opPackageName,mHostContext.getPackageName())){
                        mOpPackageName.set(baseContext,mHostContext.getPackageName());
                    }
                }
            }
        }
    }

    private void fixBaseContextImplContentResolverOpsPackage(Context context) throws IllegalAccessException{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1&&context!=null
                &&!TextUtils.equals(context.getPackageName(),mHostContext.getPackageName())){
            Context baseContext=context;
            Class clazz=baseContext.getClass();
            Field mContentResolver=FieldUtils.getDeclaredField(clazz,"mContentResolver",true);
            if(mContentResolver!=null){
                Object valueObj=mContentResolver.get(baseContext);
                if(valueObj instanceof ContentResolver){
                    ContentResolver contentResolver= (ContentResolver) valueObj;
                    Field mPackageName=FieldUtils.getDeclaredField(ContentResolver.class,"mPackageName",true);
                    Object mPackageNameValueObj=mPackageName.get(contentResolver);
                    if(mPackageNameValueObj!=null&&mPackageNameValueObj instanceof String){
                        String packageName= (String) mPackageNameValueObj;
                        if(!TextUtils.equals(packageName,mHostContext.getPackageName())){
                            mPackageName.set(contentResolver,mHostContext.getPackageName());
                        }
                    }
                }
            }
        }
    }



}

























