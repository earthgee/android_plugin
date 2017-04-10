package com.earthgee.library;

import android.content.Context;
import android.util.Log;

import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.MethodUtils;

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

        Object currentActivityThread=ActivityThreadCompat.currentActivityThread();
        Object mPackages=FieldUtils
    }

}



















