package com.earthgee.library;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import com.earthgee.library.utils.Constants;
import com.earthgee.library.utils.SoLibManager;

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

    public static final int START_RESULT_SUCCESS=0;
    public static final int START_RESULT_NO_PKG=1;
    public static final int START_RESULT_NO_CLASS=2;
    public static final int START_REULT_TYPE_ERROR=3;

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
        return pluginPackage;
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
        }

        return null;
    }

    private Resources createResources(AssetManager assetManager){
        Resources superRes=mContext.getResources();
        Resources resources=new Resources(assetManager,superRes.getDisplayMetrics(),superRes.getConfiguration());
        return resources;
    }

    public PluginPackage getPackage(String packageName){
        return mPackageHolder.get(packageName);
    }

    private void copySoLib(String dexPath){
        SoLibManager.getSoLoader().copyPluginSoLib(mContext,dexPath,mNativeLibDir);
    }

    public int startPluginActivity(Context context,PluginIntent pluginIntent){
        return startPluginActivityForResult(context,pluginIntent,-1);
    }

    public int startPluginActivityForResult(Context context,PluginIntent pluginIntent,int requestCode){
        if(mFrom==Constants.FROM_INTERNAL){
            pluginIntent.setClassName(context,pluginIntent.getPluginClass());
            performStartActivityForResult(context,pluginIntent,requestCode);
            return START_RESULT_SUCCESS;
        }

        String packageName=pluginIntent.getPluginPackage();
        PluginPackage pluginPackage=mPackageHolder.get(packageName);
        if(pluginPackage==null){
            return START_RESULT_NO_PKG;
        }

        String className=getPluginActivityFullPath(pluginIntent,pluginPackage);
        Class<?> clazz=loadPluginClass(pluginPackage.classLoader,className);
        if(clazz==null){
            return START_RESULT_NO_CLASS;
        }

        Class<? extends Activity> activityClass=getProxyActivityClass(clazz);
        if(activityClass==null){
            return START_REULT_TYPE_ERROR;
        }

        pluginIntent.putExtra(Constants.EXTRA_CLASS,className);
        pluginIntent.putExtra(Constants.EXTRA_PACKAGE,packageName);
        pluginIntent.setClass(mContext,activityClass);
        performStartActivityForResult(context,pluginIntent,requestCode);
        return START_RESULT_SUCCESS;
    }

    private String getPluginActivityFullPath(PluginIntent pluginIntent,PluginPackage pluginPackage){
        String className=pluginIntent.getPluginClass();
        className=(className==null?pluginPackage.defaultActivity:className);
        if(className.startsWith(".")){
            className=pluginIntent.getPluginPackage()+className;
        }
        return className;
    }

    private Class<?> loadPluginClass(ClassLoader classLoader,String className){
        Class<?> clazz=null;
        try {
            clazz=Class.forName(className,true,classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clazz;
    }

    private Class<? extends Activity> getProxyActivityClass(Class<?> clazz){
        Class<? extends Activity> activityClass=null;
        if(BasePluginActivity.class.isAssignableFrom(clazz)){
            activityClass=PluginProxyActivity.class;
        }else if(BasePluginFragmentActivity.class.isAssignableFrom(clazz)){
            activityClass=PluginProxyFragmentActivity.class;
        }

        return activityClass;
    }

    private void performStartActivityForResult(Context context,PluginIntent pluginIntent
            ,int requestCode){
        if(context instanceof Activity){
            ((Activity)context).startActivityForResult(pluginIntent,requestCode);
        }else{
            context.startActivity(pluginIntent);
        }
    }

}

















