package com.earthgee.corelibrary;

import android.app.ActivityManagerNative;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.util.Singleton;

import com.earthgee.corelibrary.delegate.ActivityManagerProxy;
import com.earthgee.corelibrary.internal.ComponentsHandler;
import com.earthgee.corelibrary.internal.LoadedPlugin;
import com.earthgee.corelibrary.internal.VAInstrumentation;
import com.earthgee.corelibrary.utils.ReflectUtil;
import com.earthgee.corelibrary.utils.RunUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhaoruixuan on 2017/7/24.
 */
public class PluginManager {

    private static volatile PluginManager sInstance=null;

    private Context mContext;
    private ComponentsHandler mComponentsHandler;
    private Map<String,LoadedPlugin> mPlugins=new ConcurrentHashMap<>();

    private Instrumentation mInstrumentation;
    private IActivityManager mActivityManager;

    public static PluginManager getInstance(Context base){
        if(sInstance==null){
            synchronized (PluginManager.class){
                if(sInstance==null){
                    sInstance=new PluginManager(base);
                }
            }
        }

        return sInstance;
    }

    private PluginManager(Context context){
        Context app=context.getApplicationContext();
        if(app==null){
            this.mContext=context;
        }else{
            this.mContext=((Application)app).getBaseContext();
        }
        prepare();
    }

    private void prepare(){
        Systems.sHostContext=getHostContext();
        hookInstrumentationAndHandler();
        hookSystemServices();
    }

    public void init(){
        mComponentsHandler=new ComponentsHandler(this);
    }

    private void hookInstrumentationAndHandler(){
        try{
            Instrumentation baseInstrumentation= ReflectUtil.getInstrumentation(this.mContext);
            if(baseInstrumentation.getClass().getName().contains("lbe")){
                System.exit(0);
            }

            final VAInstrumentation instrumentation=new VAInstrumentation(this,baseInstrumentation);
            Object activityThread=ReflectUtil.getActivityThread(mContext);
            ReflectUtil.setInstrumentation(activityThread,instrumentation);
            ReflectUtil.setHandlerCallback(this.mContext,instrumentation);
            this.mInstrumentation=instrumentation;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Context getHostContext(){
        return mContext;
    }

    private void hookSystemServices(){
        try{
            Singleton<IActivityManager> defaultSingleton=
                    (Singleton<IActivityManager>) ReflectUtil.getField(ActivityManagerNative.class,null,"gDefault");
            IActivityManager activityManagerProxy= ActivityManagerProxy.newInstance(this,defaultSingleton.get());

            ReflectUtil.setField(defaultSingleton.getClass().getSuperclass(),defaultSingleton,"mInstance",activityManagerProxy);

            if(defaultSingleton.get()==activityManagerProxy){
                this.mActivityManager=activityManagerProxy;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadPlugin(File apk) throws Exception{
        if(null==apk){
            throw new IllegalArgumentException("error: apk is null.");
        }

        if(!apk.exists()){
            throw new FileNotFoundException(apk.getAbsolutePath());
        }

        LoadedPlugin plugin=LoadedPlugin.create(this,this.mContext,apk);
        if(null!=plugin){
            mPlugins.put(plugin.getPackageName(),plugin);
            plugin.invokeApplication();
        }else{
            throw new RuntimeException("can't load plugin which is invalid: "+apk.getAbsolutePath());
        }
    }

    public LoadedPlugin getLoadedPlugin(String packageName){
        return this.mPlugins.get(packageName);
    }

    public List<LoadedPlugin> getAllLoadedPlugins(){
        List<LoadedPlugin> list=new ArrayList<>();
        list.addAll(mPlugins.values());
        return list;
    }

    public Instrumentation getInstrumentation(){
        return this.mInstrumentation;
    }


}


























