//package com.earthgee.corelibrary.internal;
//
//import android.content.Context;
//import android.content.res.AssetManager;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.hardware.display.DisplayManager;
//import android.os.Build;
//import android.util.DisplayMetrics;
//
//import com.earthgee.corelibrary.PluginManager;
//import com.earthgee.corelibrary.utils.ReflectUtil;
//
//import java.lang.ref.WeakReference;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by zhaoruixuan on 2017/7/25.
// */
//public class ResourcesManager {
//
//    public static synchronized Resources createResources(Context hostContext,String apk){
//        Resources hostResources=hostContext.getResources();
//        Resources newResources=null;
//        AssetManager assetManager;
//        try{
//            if(Build.VERSION.SDK_INT< Build.VERSION_CODES.LOLLIPOP){
//                assetManager=AssetManager.class.newInstance();
//                ReflectUtil.invoke(AssetManager.class,assetManager,"addAssetPath",
//                        hostContext.getApplicationInfo().sourceDir);
//            }else{
//                assetManager=hostResources.getAssets();
//            }
//            ReflectUtil.invoke(AssetManager.class,assetManager,"addAssetPath",apk);
//            List<LoadedPlugin> pluginList= PluginManager.getInstance(hostContext).
//                    getAllLoadedPlugins();
//            for(LoadedPlugin plugin:pluginList){
//                ReflectUtil.invoke(AssetManager.class,assetManager,"addAssetPath",plugin.getLocation());
//            }
//            if(isMiui(hostResources)){
//                newResources=MiuiResourcesCompat.createResources(hostResources,assetManager);
//            }else if(isVivo(hostResources)){
//                newResources=VivoResourcesCompat.createResources(hostContext,hostResources,assetManager);
//            }else if(isNubia(hostResources)){
//                newResources=NubiaResourcesCompat.createResources(hostResources,assetManager);
//            }else if(isNotRawResources(hostResources)){
//                newResources=AdaptationResourcesCompat.createResources(hostResources,assetManager);
//            }else{
//               newResources=new Resources(assetManager,hostResources.getDisplayMetrics(),hostResources.getConfiguration());
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        return newResources;
//    }
//
//    public static void hookResources(Context base,Resources resources){
//        if(Build.VERSION.SDK_INT>=24){
//            return;
//        }
//
//        try{
//            ReflectUtil.setField(base.getClass(),base,"mResources",resources);
//            Object loadedApk=ReflectUtil.getPackageInfo(base);
//            ReflectUtil.setField(loadedApk.getClass(),loadedApk,"mResources",resources);
//
//            Object activityThread=ReflectUtil.getActivityThread(base);
//            Object resManager=ReflectUtil.getField(activityThread.getClass(),activityThread,"mResourcesManager");
//            Map<Object,WeakReference<Resources>> map= (Map<Object, WeakReference<Resources>>)
//                    ReflectUtil.getField(resManager.getClass(),resManager,"mActiveResources");
//            Object key=map.keySet().iterator().next();
//            map.put(key,new WeakReference<Resources>(resources));
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    private static boolean isMiui(Resources resources){
//        return resources.getClass().getName().equals("android.content.res.MiuiResources");
//    }
//
//    private static boolean isVivo(Resources resources){
//        return resources.getClass().getName().equals("android.content.res.VivoResources");
//    }
//
//    private static boolean isNubia(Resources resources){
//        return resources.getClass().getName().equals("android.content.res.NubiaResources");
//    }
//
//    private static boolean isNotRawResources(Resources resources){
//        return !resources.getClass().getName().equals("android.content.res.Resources");
//    }
//
//    private static final class MiuiResourcesCompat{
//        private static Resources createResources(Resources hostResources,AssetManager assetManager) throws Exception{
//            Class resourcesClazz=Class.forName("android.content.res.MiuiResources");
//            Resources newResources= (Resources) ReflectUtil.invokeConstructor(resourcesClazz,new Class[]{AssetManager.class,
//                    DisplayMetrics.class, Configuration.class},new Object[]{assetManager,hostResources.getDisplayMetrics(),
//                    hostResources.getConfiguration()});
//            return newResources;
//        }
//    }
//
//    private static final class VivoResourcesCompat{
//        private static Resources createResources(Context hostContext,Resources hostResources,AssetManager assetManager)
//                throws Exception{
//            Class resourcesClazz=Class.forName("android.content.res.VivoResources");
//            Resources newResources= (Resources) ReflectUtil.invokeConstructor(resourcesClazz,new Class[]{
//                    AssetManager.class,DisplayMetrics.class,Configuration.class
//            },new Object[]{
//                    assetManager,hostResources.getDisplayMetrics(),hostResources.getConfiguration()
//            });
//            ReflectUtil.invokeNoException(resourcesClazz,newResources,"init",new Class[]{
//                    String.class
//            },hostContext.getPackageName());
//            Object themeValues=ReflectUtil.getFieldNoException(resourcesClazz,hostResources,"mThemeValues");
//            ReflectUtil.setFieldNoException(resourcesClazz,newResources,"mThemeValues",themeValues);
//            return newResources;
//        }
//    }
//
//    private static final class NubiaResourcesCompat{
//        private static Resources createResources(Resources hostResources,AssetManager assetManager)
//                throws Exception{
//            Class resourcesClazz=Class.forName("android.content.res.NubiaResources");
//            Resources newResources= (Resources) ReflectUtil.invokeConstructor(resourcesClazz,new Class[]{
//                    AssetManager.class,DisplayMetrics.class,Configuration.class
//            },new Object[]{
//                    assetManager,hostResources.getDisplayMetrics(),hostResources.getConfiguration()
//            });
//            return newResources;
//        }
//    }
//
//    private static final class AdaptationResourcesCompat{
//        private static Resources createResources(Resources hostResources,AssetManager assetManager) throws Exception{
//            Resources newResources;
//            try{
//                Class resourcesClazz=hostResources.getClass();
//                newResources= (Resources) ReflectUtil.invokeConstructor(resourcesClazz,new Class[]{
//                        AssetManager.class,DisplayMetrics.class,Configuration.class
//                },new Object[]{
//                        assetManager,hostResources.getDisplayMetrics(),hostResources.getConfiguration()
//                });
//            }catch (Exception e){
//                newResources=new Resources(assetManager,hostResources.getDisplayMetrics(),hostResources.getConfiguration());
//            }
//
//            return newResources;
//        }
//    }
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
