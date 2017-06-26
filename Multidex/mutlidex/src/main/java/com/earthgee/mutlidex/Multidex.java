package com.earthgee.mutlidex;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhaoruixuan on 2017/6/19.
 */
public class Multidex {

    private static final String OLD_SECONDARY_FOLDER_NAME="secondary-dexes";
    private static final String CODE_CACHE_NAME="code_cache";
    private static final String CODE_CACHE_SECONDARY_FOLDER_NAME="secondary-dexes";

    private static final Set<String> installedApk=new HashSet<>();

    public static void install(Context context){
        try{
            ApplicationInfo applicationInfo=getApplicationInfo(context);
            if(applicationInfo==null){
                return;
            }

            synchronized (installedApk){
                // /data/app/packagename-1/base.apk
                String apkPath=applicationInfo.sourceDir;
                Log.d("earthgee1","apkPath="+apkPath);
                if(installedApk.contains(apkPath)){
                    return;
                }
                installedApk.add(apkPath);

                ClassLoader loader;
                try{
                    //loadedApk中获取
                    loader=context.getClassLoader();
                }catch (RuntimeException e){
                    return;
                }

                if(loader==null){
                    return;
                }

                try{
                    clearOldDexDir(context);
                }catch (Throwable t){
                }

                File dexDir=getDexDir(context,applicationInfo);
                Log.d("earthgee1","new dexdir="+dexDir.getAbsolutePath());
                List<File> files=MultiDexExtractor.load(context,applicationInfo,dexDir,false);
            }
        }catch (Exception e){

        }
    }

    private static ApplicationInfo getApplicationInfo(Context context) throws PackageManager.NameNotFoundException{
        PackageManager pm;
        String packageName;
        try{
            pm=context.getPackageManager();
            packageName=context.getPackageName();
        }catch (RuntimeException e){
            return null;
        }

        if(pm==null||packageName==null){
            return null;
        }

        ApplicationInfo applicationInfo=pm.getApplicationInfo(packageName,PackageManager.GET_META_DATA);
        return applicationInfo;
    }

    private static void clearOldDexDir(Context context) throws Exception{
        File dexDir=new File(context.getFilesDir(),OLD_SECONDARY_FOLDER_NAME);
        Log.d("earthgee1","dexDir="+dexDir.getAbsolutePath());
        if(dexDir.isDirectory()){
            File[] files=dexDir.listFiles();
            if(files==null){
                return;
            }
            for(File oldFile:files){
                oldFile.delete();
            }
            dexDir.delete();
        }
    }

    private static File getDexDir(Context context,ApplicationInfo applicationInfo) throws IOException{
        File cache=new File(applicationInfo.dataDir,CODE_CACHE_NAME);
        Log.d("earthgee1","cache="+cache.getAbsolutePath());
        try{
            mkdirChecked(cache);
        }catch (IOException e){
            cache=new File(context.getFilesDir(),CODE_CACHE_NAME);
            mkdirChecked(cache);
        }
        File dexDir=new File(cache,CODE_CACHE_SECONDARY_FOLDER_NAME);
        mkdirChecked(dexDir);
        return dexDir;
    }

    private static void mkdirChecked(File dir) throws IOException{
        dir.mkdir();
        //...
    }

}
