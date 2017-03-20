package com.earthgee.library;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.earthgee.library.utils.Config;
import com.earthgee.library.utils.Constants;

import java.lang.reflect.Constructor;

/**
 * Created by zhaoruixuan on 2017/3/18.
 */
public class PluginProxyImpl {

    private String mClass;
    private String mPackageName;

    private PluginPackage mPluginPackage;
    private PluginManager mPluginManager;

    private AssetManager mAssetManager;
    private Resources mResources;
    private Resources.Theme mTheme;

    private ActivityInfo mActivityInfo;
    private Activity mProxyActivity;
    protected PluginInterface mPluginActivity;
    public ClassLoader mPluginClassLoader;

    public PluginProxyImpl(Activity activity){
        mProxyActivity=activity;
    }

    public void onCreate(Intent intent){
        intent.setExtrasClassLoader(Config.sPluginClassLoader);

        mPackageName=intent.getStringExtra(Constants.EXTRA_PACKAGE);
        mClass=intent.getStringExtra(Constants.EXTRA_CLASS);

        mPluginManager=PluginManager.getInstance(mProxyActivity);
        mPluginPackage=mPluginManager.getPackage(mPackageName);
        mAssetManager=mPluginPackage.assetManager;
        mResources=mPluginPackage.resources;

        initializeActivityInfo();
        handleActivityInfo();
        launchTargetActivity();
    }

    private void initializeActivityInfo(){
        PackageInfo packageInfo=mPluginPackage.packageInfo;
        if((packageInfo.activities!=null)&&(packageInfo.activities.length>0)){
            if(mClass==null){
                mClass=packageInfo.activities[0].name;
            }
        }

        int defaultTheme=packageInfo.applicationInfo.theme;
        for(ActivityInfo a:packageInfo.activities){
            if(a.name.equals(mClass)){
                mActivityInfo=a;
                if(mActivityInfo.theme==0){
                    if(defaultTheme!=0){
                        mActivityInfo.theme=defaultTheme;
                    }else{
                        if(Build.VERSION.SDK_INT>=14){
                            mActivityInfo.theme=android.R.style.Theme_DeviceDefault;
                        }else{
                            mActivityInfo.theme=android.R.style.Theme;
                        }
                    }
                }
            }
        }
    }

    private void handleActivityInfo(){
        if(mActivityInfo.theme>0){
            mProxyActivity.setTheme(mActivityInfo.theme);
        }

        Resources.Theme superTheme=mProxyActivity.getTheme();
        mTheme=mResources.newTheme();
        mTheme.setTo(superTheme);

        try{
            mTheme.applyStyle(mActivityInfo.theme,true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void launchTargetActivity(){
        try {
            Class<?> localClass=getClassLoader().loadClass(mClass);
            Object instance=localClass.newInstance();
            mPluginActivity= (PluginInterface) instance;
            ((PluginAttachable)mProxyActivity).attach(mPluginActivity);
            mPluginActivity.attach(mProxyActivity,mPluginPackage);

            Bundle bundle=new Bundle();
            bundle.putInt(Constants.FROM,Constants.FROM_EXTERNAL);
            mPluginActivity.onCreate(bundle);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public ClassLoader getClassLoader(){
        return mPluginPackage.classLoader;
    }

    public AssetManager getAssets(){
        return mAssetManager;
    }

    public Resources getResources() {
        return mResources;
    }

    public Resources.Theme getTheme(){
        return mTheme;
    }

    public PluginInterface getRemoteActivity(){
        return mPluginActivity;
    }

}











