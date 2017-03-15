package com.earthgee.library;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.earthgee.library.utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

/**
 * Created by zhaoruixuan on 2017/3/15.
 */
public class PluginManager {

    private static final String TAG="PluginManager";
    private static PluginManager instance;

    private Context mContext;
    private String mNativeLibDir;

    private int mFrom= Constants.FROM_INTERNAL;

    private final HashMap<String,PluginPackage> mPackageHolder=
            new HashMap<>();

    private PluginManager(Context context){
        mContext=context;
        mNativeLibDir=mContext.getDir("pluginlib",Context.MODE_PRIVATE)
                .getAbsolutePath();
    }

    public static PluginManager getInstance(Context context){
        if(instance==null){
            synchronized (PluginManager.class){
                if(instance==null){
                    instance=new PluginManager(context);
                }
            }
        }

        return instance;
    }

    public PluginPackage loadApk(String dexPath){
        return loadApk(dexPath,true);
    }

    public PluginPackage loadApk(final String dexPath,boolean hasSoLib){
        mFrom=Constants.FROM_EXTERNAL;

        PackageInfo packageInfo=mContext.
                getPackageManager().getPackageArchiveInfo(dexPath,
                PackageManager.GET_ACTIVITIES|PackageManager.GET_SERVICES);
        if(packageInfo==null) return null;

        PluginPackage pluginPackage=preparePluginEnv(packageInfo,dexPath);
        if(hasSoLib){
            copySoLib(dexPath);
        }

        return pluginPackage;
    }

    private PluginPackage preparePluginEnv(PackageInfo packageInfo,String dexPath){
        PluginPackage pluginPackage=mPackageHolder.get(packageInfo.packageName);
        if(pluginPackage!=null){
            return pluginPackage;
        }

        DexClassLoader dexClassLoader=createDexClassLoader(dexPath);
        AssetManager assetManager=createAssetManager(dexPath);
        Resources resources=createResources(assetManager);

        pluginPackage=new PluginPackage(dexClassLoader,resources,packageInfo);
        mPackageHolder.put(packageInfo.packageName,pluginPackage);
    }

    private String dexOutputPath;

    private DexClassLoader createDexClassLoader(String dexPath){
        File dexOutputDir=mContext.getDir("dex",Context.MODE_PRIVATE);
        dexOutputPath=dexOutputDir.getAbsolutePath();
        DexClassLoader loader=new DexClassLoader(dexPath,dexOutputPath,mNativeLibDir,mContext.getClassLoader());
        return loader;
    }

    private AssetManager createAssetManager(String dexPath){
        try {
            AssetManager assetManager=AssetManager.class.newInstance();
            Method addAssetPath=assetManager.getClass().getMethod("addAssetPath",String.class);
            addAssetPath.invoke(assetManager,dexPath);
            return assetManager;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }finally {
            return null;
        }
    }

    private Resources createResources(AssetManager assetManager){
        Resources superRes=mContext.getResources();
        Resources resources=new Resources(assetManager,superRes.getDisplayMetrics(),superRes.getConfiguration());
        return resources;
    }

}

















