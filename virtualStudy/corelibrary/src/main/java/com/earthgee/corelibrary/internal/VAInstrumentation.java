package com.earthgee.corelibrary.internal;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.earthgee.corelibrary.PluginManager;
import com.earthgee.corelibrary.utils.ReflectUtil;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class VAInstrumentation extends Instrumentation implements Handler.Callback{

    private Instrumentation mBase;

    PluginManager mPluginManager;

    public VAInstrumentation(PluginManager pluginManager,Instrumentation base){
        this.mPluginManager=pluginManager;
        this.mBase=base;
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target
            , Intent intent, int requestCode, Bundle options){
//        Log.d("earthgee2","instrumentation execStartActivity hook");
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
    public boolean handleMessage(Message msg) {
        return false;
    }



}

















