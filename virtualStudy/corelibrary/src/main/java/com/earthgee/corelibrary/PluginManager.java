package com.earthgee.corelibrary;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.Application;
import android.app.IActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Singleton;

import com.earthgee.corelibrary.delegate.ActivityManagerProxy;
import com.earthgee.corelibrary.internal.ComponentsHandler;
import com.earthgee.corelibrary.internal.LoadedPlugin;
import com.earthgee.corelibrary.internal.PluginContentResolver;
import com.earthgee.corelibrary.internal.VAInstrumentation;
import com.earthgee.corelibrary.utils.PluginUtil;
import com.earthgee.corelibrary.utils.ReflectUtil;
import com.earthgee.corelibrary.utils.RunUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
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
    private IContentProvider mIContentProvider;

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

    public LoadedPlugin getLoadedPlugin(Intent intent){
        ComponentName component= PluginUtil.getComponent(intent);
        return getLoadedPlugin(component.getPackageName());
    }

    public LoadedPlugin getLoadedPlugin(ComponentName component){
        return this.getLoadedPlugin(component.getPackageName());
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

    public ComponentsHandler getComponentsHandler(){
        return mComponentsHandler;
    }

    public IActivityManager getActivityManager(){
        return mActivityManager;
    }

    public ResolveInfo resolveActivity(Intent intent){
        return this.resolveActivity(intent,0);
    }

    public ResolveInfo resolveActivity(Intent intent,int flags){
        for(LoadedPlugin plugin:this.mPlugins.values()){
            ResolveInfo resolveInfo=plugin.resolveActivity(intent,flags);
            if(null!=resolveInfo){
                return resolveInfo;
            }
        }

        return null;
    }

    public ResolveInfo resolveService(Intent intent,int flags){
        for(LoadedPlugin plugin:this.mPlugins.values()){
            ResolveInfo resolveInfo=plugin.resolveService(intent,flags);
            if(null!=resolveInfo){
                return resolveInfo;
            }
        }

        return null;
    }

    public ProviderInfo resolveContentProvider(String name,int flags){
        for(LoadedPlugin plugin:this.mPlugins.values()){
            ProviderInfo providerInfo=plugin.resolveContentProvider(name,flags);
            if(null!=providerInfo){
                return providerInfo;
            }
        }

        return null;
    }

    private void hookIContentProviderAsNeeded(){
        Uri uri=Uri.parse(PluginContentResolver.getUri(mContext));
        mContext.getContentResolver().call(uri,"wakeup",null,null);
        try{
            Field authority=null;
            Field mProvider=null;
            ActivityThread activityThread=
                    (ActivityThread) ReflectUtil.getActivityThread(mContext);
            Map mProviderMap= (Map) ReflectUtil.getField
                    (activityThread.getClass(),activityThread,"mProviderMap");
            Iterator iter=mProviderMap.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry entry= (Map.Entry) iter.next();
                Object key=entry.getKey();
                Object val=entry.getValue();
                String auth;
                if(key instanceof String){
                    auth= (String) key;
                }else{
                    if(authority==null){
                        authority=key.getClass().getDeclaredField("authority");
                        authority.setAccessible(true);
                    }
                    auth= (String) authority.get(key);
                }
                if(auth.equals(PluginContentResolver.getAuthority(mContext))){
                    if(mProvider==null){
                        mProvider=val.getClass().getDeclaredField("mProvider");
                        mProvider.setAccessible(true);
                    }
                    IContentProvider rawProvider= (IContentProvider) mProvider.get(val);
                    IContentProvider proxy=IContentProvider.newInstance(mContext,rawProvider);
                    mIContentProvider=proxy;
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized IContentProvider getIContentProvider(){
        if(mIContentProvider==null){
            hookIContentProviderAsNeeded();
        }

        return mIContentProvider;
    }

}


























