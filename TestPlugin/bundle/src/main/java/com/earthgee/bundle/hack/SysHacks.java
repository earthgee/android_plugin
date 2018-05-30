package com.earthgee.bundle.hack;

import android.app.Application;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.earthgee.bundle.log.Logger;
import com.earthgee.bundle.log.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2018/4/24.
 */
public class SysHacks implements Hack.AssertionFailureHandler{
    //class
    public static Hack.HackedClass<Object> LoadedApk;
    public static Hack.HackedClass<Object> ActivityThread;
    public static Hack.HackedClass<Resources> Resources;
    public static Hack.HackedClass<Application> Application;
    public static Hack.HackedClass<AssetManager> AssetManager;
    public static Hack.HackedClass<Object> IPackageManager;
    public static Hack.HackedClass<Service> Service;
    public static Hack.HackedClass<Object> ContextImpl;
    public static Hack.HackedClass<ContextThemeWrapper> ContextThemeWrapper;
    public static Hack.HackedClass<ContextWrapper> ContextWrapper;
    public static Hack.HackedClass<Object> Instrumentation;

    //field
    public static Hack.HackedField<Object, android.app.Instrumentation> ActivityThread_mInstrumentation;
    public static Hack.HackedField<Object, ArrayList<Application>> ActivityThread_mAllApplications;
    public static Hack.HackedField<Object, Map<String, Object>> ActivityThread_mPackages;
    public static Hack.HackedField<Object,Object> ActivityThread_sPackageManager;
    public static Hack.HackedField<Object,Application> LoadedApk_mApplication;
    public static Hack.HackedField<Object,Resources> LoadedApk_mResources;
    public static Hack.HackedField<Object,String> LoadedApk_mAppDir;
    public static Hack.HackedField<Object,String> LoadedApk_mResDir;
    public static Hack.HackedField<Object,Resources> ContextImpl_mResources;
    public static Hack.HackedField<Object, android.content.res.Resources.Theme> ContextImpl_mTheme;
    public static Hack.HackedField<ContextThemeWrapper, Context> ContextThemeWrapper_mBase;
    public static Hack.HackedField<ContextThemeWrapper, Resources> ContextThemeWrapper_mResources;
    public static Hack.HackedField<ContextThemeWrapper, android.content.res.Resources.Theme> ContextThemeWrapper_mTheme;
    public static Hack.HackedField<android.content.ContextWrapper,Context> ContextWrapper_mBase;
    public static Hack.HackedField<Resources,Object> Resources_mAssets;

    public static Hack.HackedMethod ActivityThread_currentActivityThread;
    public static Hack.HackedMethod Application_attach;
    public static Hack.HackedMethod AssetManager_addAssetPath;

    public static ArrayList<Hack.HackedMethod> GeneratePackageInfoList;
    public static ArrayList<Hack.HackedMethod> GetPackageInfoList;

    public static boolean sIsIgnoreFailure;
    public static boolean sIsReflectAvailable;
    public static boolean sIsReflectChecked;
    private AssertionArrayException mExceptionArray;
    static final Logger log;

    public SysHacks(){
        this.mExceptionArray=null;
    }

    static {
        log= LoggerFactory.getLogcatLogger(SysHacks.class);
        sIsIgnoreFailure=false;
        sIsReflectAvailable=false;
        sIsReflectChecked=false;
        GeneratePackageInfoList=new ArrayList<>();
        GetPackageInfoList=new ArrayList<>();
    }

    public static boolean defineAndVerify() throws AssertionArrayException{
        if(sIsReflectChecked){
            return sIsReflectAvailable;
        }
        SysHacks atlasHacks=new SysHacks();
        try{
            Hack.setAssertionFailureHandler(atlasHacks);
            allClasses();
            allConstructors();
            allFields();
            allMethods();
            sIsReflectAvailable=true;
            return sIsReflectAvailable;
        }catch (Throwable e){
            sIsReflectAvailable = false;
            log.log("HackAssertionException", Logger.LogLevel.ERROR, e);
            throw new AssertionArrayException("defineAndVerify HackAssertionException");
        }finally {
            Hack.setAssertionFailureHandler(null);
            sIsReflectChecked=true;
        }
    }

    public static void allClasses() throws Hack.HackDeclaration.HackAssertionException{
        LoadedApk=Hack.into("android.app.LoadedApk");
        ActivityThread=Hack.into("android.app.ActivityThread");
        Resources=Hack.into(Resources.class);
        Application=Hack.into(Application.class);
        AssetManager=Hack.into(AssetManager.class);
        IPackageManager=Hack.into("android.content.pm.IPackageManager");
        Service=Hack.into(Service.class);
        ContextImpl=Hack.into("android.app.ContextImpl");
        ContextThemeWrapper=Hack.into(ContextThemeWrapper.class);
        ContextWrapper=Hack.into("android.content.ContextWrapper");
        sIsIgnoreFailure=true;
        Instrumentation=Hack.into("android.app.Instrumentation");
        sIsIgnoreFailure=false;
    }

    public static void allConstructors() throws Hack.HackDeclaration.HackAssertionException{

    }

    public static void allFields() throws Hack.HackDeclaration.HackAssertionException{
        ActivityThread_mInstrumentation=ActivityThread.field("mInstrumentation");
        ActivityThread_mInstrumentation.ofType(android.app.Instrumentation.class);
        ActivityThread_mAllApplications=ActivityThread.field("mAllApplications");
        ActivityThread_mAllApplications.ofGenericType(ArrayList.class);
        ActivityThread_mPackages=ActivityThread.field("mPackages");
        ActivityThread_mPackages.ofGenericType(Map.class);
        ActivityThread_sPackageManager=ActivityThread.staticField("sPackageManager").ofType(IPackageManager.getmClass());
        LoadedApk_mApplication=LoadedApk.field("mApplication");
        LoadedApk_mApplication.ofType(android.app.Application.class);
        LoadedApk_mResources=LoadedApk.field("mResources");
        LoadedApk_mResources.ofType(android.content.res.Resources.class);
        LoadedApk_mResDir=LoadedApk.field("mResDir");
        LoadedApk_mResDir.ofType(String.class);
        LoadedApk_mAppDir=LoadedApk.field("mAppDir");
        LoadedApk_mAppDir.ofType(String.class);
        ContextImpl_mResources=ContextImpl.field("mResources");
        ContextImpl_mResources.ofType(android.content.res.Resources.class);
        ContextImpl_mTheme=ContextImpl.field("mTheme");
        ContextImpl_mTheme.ofType(android.content.res.Resources.Theme.class);
        sIsIgnoreFailure=true;
        ContextThemeWrapper_mBase=ContextThemeWrapper.field("mBase");
        ContextThemeWrapper_mBase.ofType(Context.class);
        sIsIgnoreFailure=false;
        ContextThemeWrapper_mTheme=ContextThemeWrapper.field("mTheme");
        ContextThemeWrapper_mTheme.ofType(android.content.res.Resources.Theme.class);
        try{
            if(Build.VERSION.SDK_INT>=17&&ContextThemeWrapper.getmClass().getDeclaredField("mResources")!=null){
                ContextThemeWrapper_mResources=ContextThemeWrapper.field("mResources");
                ContextThemeWrapper_mResources.ofType(android.content.res.Resources.class);
            }
        }catch (NoSuchFieldException e){
            log.log("Not found ContextThemeWrapper.mResources on VERSION " + Build.VERSION.SDK_INT, Logger.LogLevel.WARN);
        }
        ContextWrapper_mBase=ContextWrapper.field("mBase");
        ContextWrapper_mBase.ofType(Context.class);
        Resources_mAssets=Resources.field("mAssets");
    }

    public static void allMethods() throws Hack.HackDeclaration.HackAssertionException{
        ActivityThread_currentActivityThread=ActivityThread.method("currentActivityThread",new Class[0]);
        AssetManager_addAssetPath=AssetManager.method("addAssetPath",String.class);
        Application_attach=Application.method("attach",Context.class);
    }

    @Override
    public boolean onAssertionFailure(Hack.HackDeclaration.HackAssertionException hackAssertionException) {
        if(!sIsIgnoreFailure){
            if(this.mExceptionArray==null){
                mExceptionArray=new AssertionArrayException("Hack assert failed");
            }
            this.mExceptionArray.addException(hackAssertionException);
        }
        return true;
    }
}
