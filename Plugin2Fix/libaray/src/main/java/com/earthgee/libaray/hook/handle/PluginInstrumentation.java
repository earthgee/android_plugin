package com.earthgee.libaray.hook.handle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.earthgee.libaray.am.RunningActivities;
import com.earthgee.libaray.core.Env;
import com.earthgee.libaray.core.PluginProcessManager;
import com.earthgee.libaray.hook.HookFactory;
import com.earthgee.libaray.hook.proxy.IPackageManagerHook;
import com.earthgee.libaray.pm.PluginManager;
import com.earthgee.libaray.reflect.FieldUtils;

import java.lang.reflect.Field;

/**
 * Created by zhaoruixuan on 2017/6/2.
 */
public class PluginInstrumentation extends Instrumentation{

    protected Instrumentation mTarget;
    private final Context mHostContext;
    private boolean enable=true;

    public void setEnable(boolean enable){
        this.enable=enable;
        this.enable=true;
    }

    public PluginInstrumentation(Context hostContext, Instrumentation target) {
        mTarget = target;
        mHostContext = hostContext;
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if(enable){
            //todo
            //IWindowManagerBinderHook.fixWindowManagerHook(activity);
            IPackageManagerHook.fixContextPackageManager(activity);
            //todo

            try{
                onActivityCreated(activity);
            }catch (RemoteException e){
            }

            try {
                fixBaseContextImplOpsPackage(activity.getBaseContext());
            } catch (Exception e) {
            }

            try {
                fixBaseContextImplContentResolverOpsPackage(activity.getBaseContext());
            } catch (Exception e) {
            }
        }
        if (mTarget != null) {
            mTarget.callActivityOnCreate(activity, icicle);
        } else {
            super.callActivityOnCreate(activity, icicle);
        }
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        if(enable){
            IPackageManagerHook.fixContextPackageManager(app);
            try{
                //todo
               // PluginProcessManager.fakeSystemService(mHostContext,app);
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
                Log.d("earthgee2","register static");
                PluginProcessManager.registerStaticReceiver(app,app.getApplicationInfo(),app.getClassLoader());
            }catch (Exception e){
            }
        }
    }

    private void fixBaseContextImplOpsPackage(Context context) throws IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && context != null && !TextUtils.equals(context.getPackageName(), mHostContext.getPackageName())) {
            Context baseContext = context;
            Class clazz = baseContext.getClass();
            Field mOpPackageName = FieldUtils.getDeclaredField(clazz, "mOpPackageName", true);
            if (mOpPackageName != null) {
                Object valueObj = mOpPackageName.get(baseContext);
                if (valueObj instanceof String) {
                    String opPackageName = ((String) valueObj);
                    if (!TextUtils.equals(opPackageName, mHostContext.getPackageName())) {
                        mOpPackageName.set(baseContext, mHostContext.getPackageName());
                    }
                }
            }
        }
    }

    private void fixBaseContextImplContentResolverOpsPackage(Context context) throws IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && context != null && !TextUtils.equals(context.getPackageName(), mHostContext.getPackageName())) {
            Context baseContext = context;
            Class clazz = baseContext.getClass();
            Field mContentResolver = FieldUtils.getDeclaredField(clazz, "mContentResolver", true);
            if (mContentResolver != null) {
                Object valueObj = mContentResolver.get(baseContext);
                if (valueObj instanceof ContentResolver) {
                    ContentResolver contentResolver = ((ContentResolver) valueObj);
                    Field mPackageName = FieldUtils.getDeclaredField(ContentResolver.class, "mPackageName", true);
                    Object mPackageNameValueObj = mPackageName.get(contentResolver);
                    if (mPackageNameValueObj != null && mPackageNameValueObj instanceof String) {
                        String packageName = ((String) mPackageNameValueObj);
                        if (!TextUtils.equals(packageName, mHostContext.getPackageName())) {
                            mPackageName.set(contentResolver, mHostContext.getPackageName());
                        }
                    }
                }
            }
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
                    if(activity.getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            && targetInfo.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void fixTaskDescription(Activity activity, ActivityInfo targetInfo) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PackageManager pm = mHostContext.getPackageManager();
                String lablel = String.valueOf(targetInfo.loadLabel(pm));
                Drawable icon = targetInfo.loadIcon(pm);
                Bitmap bitmap = null;
                if (icon instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable) icon).getBitmap();
                }
                if (bitmap != null) {
                    activity.setTaskDescription(new android.app.ActivityManager.TaskDescription(lablel, bitmap));
                } else {
                    activity.setTaskDescription(new android.app.ActivityManager.TaskDescription(lablel));
                }
            }
        } catch (Throwable e) {
        }
    }

}













