package com.earthgee.library;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;

import com.earthgee.library.core.PluginProcessManager;
import com.earthgee.library.pm.PluginManager;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.MethodUtils;
import com.earthgee.library.util.ActivityThreadCompat;

import java.util.HashMap;

/**
 * Created by zhaoruixuan on 2017/4/6.
 * 单例，整个流程的调用
 */
public class PluginHelper implements ServiceConnection{

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
     * @param baseContext 每个进程的application的base context(ContextImpl)
     */
    private void initPlugin(Context baseContext){
        //一些兼容性问题，与主线无关
        try{
            fixMiuiLbeSecurity();
        }catch (Throwable e){
        }


        try{
            //这里只是赋值context
            PluginPatchManager.getInstance().init(baseContext);
            //开启hook流程
            PluginProcessManager.installHook(baseContext);
        }catch (Throwable e){
        }

        try{
            //如果是插件的进程 就打开hook(插件进程的hook?)
            if(PluginProcessManager.isPluginProcess(baseContext)){
                PluginProcessManager.setHookEnable(true);
            }else {
                PluginProcessManager.setHookEnable(false);
            }
        }catch (Throwable e){
        }

        //自定义插件包管理器
        try{
            //当插件包服务处理后，回调
            PluginManager.getInstance().addServiceConnection(PluginHelper.this);
            PluginManager.getInstance().init(baseContext);
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

    //PluginManagerService bind成功
    //开启hook(host进程的hook)
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        PluginProcessManager.setHookEnable(true,true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}



















