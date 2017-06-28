package com.earthgee.mutlidex;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.zip.ZipFile;

import dalvik.system.DexFile;

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
                if(checkValidZipFiles(files)){
                    installSecondaryDexes(loader,dexDir,files);
                }else{
                    files=MultiDexExtractor.load(context,applicationInfo,dexDir,true);

                    if(checkValidZipFiles(files)){
                        installSecondaryDexes(loader,dexDir,files);
                    }else{
                        throw new RuntimeException("Zip files were not valid.");
                    }
                }
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

    private static void installSecondaryDexes(ClassLoader loader,File dexDir,List<File> files) throws Exception{
        if(!files.isEmpty()){
            if(Build.VERSION.SDK_INT>=19){
                V19.install(loader,files,dexDir);
            }else if(Build.VERSION.SDK_INT>=14){
                V14.install(loader,files,dexDir);
            }else{
                V4.install(loader,files);
            }
        }
    }

    private static boolean checkValidZipFiles(List<File> files){
        for(File file:files){
            if(!MultiDexExtractor.verifyZipFile(file)){
                return false;
            }
        }
        return true;
    }

    private static Field findField(Object instance,String name) throws NoSuchFieldException{
        for(Class<?> clazz=instance.getClass();clazz!=null;clazz=clazz.getSuperclass()){
            try{
                Field field=clazz.getDeclaredField(name);

                if(!field.isAccessible()){
                    field.setAccessible(true);
                }

                return field;
            }catch (NoSuchFieldException e){
            }
        }

        throw new NoSuchFieldException("Field "+name+" not found in "+instance.getClass());
    }

    private static Method findMethod(Object instance,String name,Class<?>... parameterTypes) throws NoSuchMethodException{
        for(Class<?> clazz=instance.getClass();clazz!=null;clazz=clazz.getSuperclass()){
            try{
                Method method=clazz.getDeclaredMethod(name,parameterTypes);

                if(!method.isAccessible()){
                    method.setAccessible(true);
                }
                return method;
            }catch (NoSuchMethodException e){
            }
        }

        throw new NoSuchMethodException("Method "+name+" with parameters "+
                Arrays.asList(parameterTypes)+" not found in "+instance.getClass());
    }

    private static void expandFieldArray(Object instance,String fieldName,Object[] extraElements) throws Exception{
        Field jlrField=findField(instance,fieldName);
        Object[] original= (Object[]) jlrField.get(instance);
        Object[] combined= (Object[]) Array.newInstance(original.getClass().getComponentType(),original.length+extraElements.length);
        System.arraycopy(original,0,combined,0,original.length);
        System.arraycopy(extraElements,0,combined,original.length,extraElements.length);
        jlrField.set(instance,combined);
    }

    private static final class V19{
        private static void install(ClassLoader loader,List<File> additionalClassPathEntries,File optimizedDirectory) throws Exception{
            Field pathListField=findField(loader,"pathList");
            Object dexPathList=pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions=new ArrayList<>();
            expandFieldArray(dexPathList,"dexElements",makeDexElements(dexPathList,new ArrayList<File>(additionalClassPathEntries)
                    ,optimizedDirectory,suppressedExceptions));
            if(suppressedExceptions.size()>0){
                Field suppressedExceptionsField=findField(dexPathList,"dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions= (IOException[]) suppressedExceptionsField.get(dexPathList);

                if(dexElementsSuppressedExceptions==null){
                    dexElementsSuppressedExceptions=suppressedExceptions.toArray(new IOException[suppressedExceptions.size()]);
                }else{
                    IOException[] combined=new IOException[suppressedExceptions.size()+dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions,0,combined,
                            suppressedExceptions.size(),dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions=combined;
                }

                suppressedExceptionsField.set(dexPathList,dexElementsSuppressedExceptions);
            }
        }

        private static Object[] makeDexElements(Object dexPathList,ArrayList<File> files
                    , File optimizedDirectory,ArrayList<IOException> suppressedExceptions) throws Exception{
            Method makeDexElements=findMethod(dexPathList,"makeDexElements",ArrayList.class,File.class,ArrayList.class);

            return (Object[]) makeDexElements.invoke(dexPathList,files,optimizedDirectory,suppressedExceptions);
        }

    }

    private static final class V14{

        private static void install(ClassLoader loader,List<File> additionalClassPathEntries,File optimizeDirectory) throws Exception{
            Field pathListField=findField(loader,"pathList");
            Object dexPathList=pathListField.get(loader);
            expandFieldArray(dexPathList,"dexElements",makeDexElements(dexPathList,new ArrayList<File>(additionalClassPathEntries),optimizeDirectory));
        }

        private static Object[] makeDexElements(Object dexPathList,ArrayList<File> files,File optimizedDirectory) throws Exception{
            Method makeDexElements=findMethod(dexPathList,"makeDexElements",ArrayList.class,File.class);
            return (Object[]) makeDexElements.invoke(dexPathList,files,optimizedDirectory);
        }

    }

    private static final class V4{
        private static void install(ClassLoader loader,List<File> additionalClassPathEntries) throws Exception{
            int extraSize=additionalClassPathEntries.size();

            Field pathField=findField(loader,"path");

            StringBuilder path=new StringBuilder((String)pathField.get(loader));
            String[] extraPaths=new String[extraSize];
            File[] extraFiles=new File[extraSize];
            ZipFile[] extraZips=new ZipFile[extraSize];
            DexFile[] extraDexs=new DexFile[extraSize];
            for(ListIterator<File> iterator=additionalClassPathEntries.listIterator();iterator.hasNext();){
                File additionalEntry=iterator.next();
                String entryPath=additionalEntry.getAbsolutePath();
                path.append(":").append(entryPath);
                int index=iterator.previousIndex();
                extraPaths[index]=entryPath;
                extraFiles[index]=additionalEntry;
                extraZips[index]=new ZipFile(additionalEntry);
                extraDexs[index]=DexFile.loadDex(entryPath,entryPath+".dex",0);
            }

            pathField.set(loader,path.toString());
            expandFieldArray(loader,"mPaths",extraPaths);
            expandFieldArray(loader,"mFiles",extraFiles);
            expandFieldArray(loader,"mZips",extraZips);
            expandFieldArray(loader,"mDexs",extraDexs);
        }
    }



}
















