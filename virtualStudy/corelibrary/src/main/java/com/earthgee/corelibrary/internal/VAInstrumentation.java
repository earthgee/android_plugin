package com.earthgee.corelibrary.internal;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.utils.PluginUtil;
import com.earthgee.corelibrary.utils.ReflectUtil;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class VAInstrumentation extends Instrumentation implements Handler.Callback{

    public static final int LAUNCH_ACTIVITY=100;

    private Instrumentation mBase;

    PluginManager mPluginManager;

    public VAInstrumentation(PluginManager pluginManager,Instrumentation base){
        this.mPluginManager=pluginManager;
        this.mBase=base;
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target
            , Intent intent, int requestCode, Bundle options){
        mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);
        if(intent.getComponent()!=null){
            mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
        }

        ActivityResult result=realExecStartActivity(who,contextThread,token,target,intent,requestCode,options);

        return result;
    }

    private ActivityResult realExecStartActivity(Context who,IBinder contextThread,IBinder token,Activity target
            , Intent intent,int requestCode,Bundle options){
        ActivityResult result=null;
        try{
            Class[] parameterTypes={Context.class,IBinder.class,IBinder.class,Activity.class,
                    Intent.class,int.class,Bundle.class};
            result= (ActivityResult) ReflectUtil.invoke(Instrumentation.class,mBase,"execStartActivity",parameterTypes,who,contextThread,token,
                    target,intent,requestCode,options);
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try{
            cl.loadClass(className);
        }catch (ClassNotFoundException e){
            LoadedPlugin plugin=this.mPluginManager.getLoadedPlugin(intent);
            String targetClassName=PluginUtil.getTargetActivity(intent);

            if(targetClassName!=null){
                Activity activity=mBase.newActivity(plugin.getClassLoader(),targetClassName,intent);
                activity.setIntent(intent);

                try{
                    ReflectUtil.setField(ContextThemeWrapper.class,activity,"mResources",plugin.getResources());
                }catch (Exception ignored){
                }

                return activity;
            }
        }

        return mBase.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        final Intent intent=activity.getIntent();
        if(PluginUtil.isIntentFromPlugin(intent)){
            Context base=activity.getBaseContext();
            try{
                LoadedPlugin plugin=this.mPluginManager.getLoadedPlugin(intent);
                ReflectUtil.setField(base.getClass(),base,"mResources",plugin.getResources());
                ReflectUtil.setField(ContextWrapper.class,activity,"mBase",plugin.getPluginContext());
                ReflectUtil.setField(Activity.class,activity,"mApplication",plugin.getApplication());
                ReflectUtil.setFieldNoException(ContextThemeWrapper.class,activity,"mBase",plugin.getPluginContext());

                ActivityInfo activityInfo=plugin.getActivityInfo(PluginUtil.getComponent(intent));
                if(activityInfo.screenOrientation!=ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
                    activity.setRequestedOrientation(activityInfo.screenOrientation);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        mBase.callActivityOnCreate(activity, icicle);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if(msg.what==LAUNCH_ACTIVITY){
            Object r=msg.obj;
            try{
                Intent intent= (Intent) ReflectUtil.getField(r.getClass(),r,"intent");
                intent.setExtrasClassLoader(VAInstrumentation.class.getClassLoader());
                ActivityInfo activityInfo= (ActivityInfo) ReflectUtil.getField(r.getClass(),r,"activityInfo");

                if(PluginUtil.isIntentFromPlugin(intent)){
                    int theme=PluginUtil.getTheme(mPluginManager.getHostContext(),intent);
                    if(theme!=0){
                        activityInfo.theme=theme;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return false;
    }



}

















