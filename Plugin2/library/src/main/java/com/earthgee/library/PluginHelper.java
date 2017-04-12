package com.earthgee.library;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

import com.earthgee.library.core.PluginProcessManager;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.MethodUtils;
import com.earthgee.library.util.ActivityThreadCompat;

import java.util.HashMap;

/**
 * Created by zhaoruixuan on 2017/4/6.
 */
public class PluginHelper {

    private static final String TAG=PluginHelper.class.getSimpleName();

    private static PluginHelper sInstance=null;
    private Context mContext;

    private PluginHelper(){

    }

    public static final PluginHelper getInstance(){
        if(sInstance==null){
            sInstance=new PluginHelper();
        }
        return sInstance;
    }

    public void applicationAttachBaseContext(Context baseContext){
        MyCrashHandler.getInstance().register(baseContext);
    }

    public void applicationOnCreate(final Context baseContext){
        mContext=baseContext;
        initPlugin(baseContext);
    }

    /**
     * 初始化插件
     * @param baseContext
     */
    private void initPlugin(Context baseContext){
        long b=System.currentTimeMillis();
        try{
            fixMiuiLbeSecurity();
        }catch (Throwable e){
        }

        try{
            PluginPatchManager.getInstance().init(baseContext);
            PluginProcessManager.installHook(baseContext);
        }catch (Throwable e){
        }

        try{
            if(PluginProcessManager.isPluginProcess(baseContext)){
                PluginProcessManager.setHookEnable(true);
            }else {
                PluginProcessManager.setHookEnable(false);
            }
        }catch (Throwable e){
        }
    }

    private void fixMiuiLbeSecurity() throws Exception{
        Class ApplicationLoaders=Class.forName("android.app.ApplicationLoaders");
        Object applicationLoaders= MethodUtils.invokeStaticMethod(ApplicationLoaders,"getDefault");
        Object mLoaders= FieldUtils.readField(applicationLoaders,"mLoaders",true);
        if(mLoaders instanceof HashMap){
            HashMap oldValue= (HashMap) mLoaders;
            if("com.lbe.security.client.ClientContainer$MonitoredLoaderMap".
                    equals(mLoaders.getClass().getName())){
                HashMap value=new HashMap();
                value.putAll(oldValue);
                FieldUtils.writeField(applicationLoaders,"mLoaders",value,true);
            }
        }

        Object currentActivityThread= ActivityThreadCompat.currentActivityThread();
        Object mPackages=FieldUtils.readField(currentActivityThread,"mPackages",true);
        if(mPackages instanceof HashMap){
            HashMap oldValue= (HashMap) mPackages;
            if("com.lbe.security.client.ClientContainer$MonitoredPackageMap".
                    equals(mPackages.getClass().getName())){
                HashMap value=new HashMap();
                value.putAll(oldValue);
                FieldUtils.writeField(currentActivityThread,"mPackages",value,true);
            }
        }

        if(Looper.getMainLooper()==Looper.myLooper()){
            final MessageQueue queue=Looper.myQueue();
            try{
                Object mMessages=FieldUtils.readField(queue,"mMessages",true);
                if(mMessages instanceof Message){
                    findLbeMessageAndRemoveIt((Message)mMessages);
                }
            }catch (Exception e){

            }
        }
    }

    private void findLbeMessageAndRemoveIt(Message message){
        if(message==null) return;

        Runnable callback=message.getCallback();
        if(message.what==0&&callback!=null){
            if(callback.getClass().getName().indexOf("com.lbe.security.client")>=0){
                message.getTarget().removeCallbacks(callback);
            }
        }

        try{
            Object nextObj=FieldUtils.readField(message,"next",true);
            if(nextObj!=null){
                Message next= (Message) nextObj;
                findLbeMessageAndRemoveIt(next);
            }
        }catch (Exception e){
        }
    }

}



















