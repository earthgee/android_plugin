package com.earthgee.corelibrary.utils;

import android.content.Context;
import android.os.Build;

import com.earthgee.corelibrary.Systems;
import com.earthgee.corelibrary.internal.Constants;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by zhaoruixuan on 2017/7/25.
 */
public class DexUtil {

    private static boolean sHasInsertedNativeLibrary=false;

    public static void insertDex(DexClassLoader dexClassLoader) throws Exception {
        Object baseDexElements=getDexElements(getPathList(getPathClassLoader()));
        Object newDexElements=getDexElements(getPathList(dexClassLoader));
        Object allDexElements=combineArray(baseDexElements,newDexElements);
        Object pathList=getPathList(getPathClassLoader());
        ReflectUtil.setField(pathList.getClass(),pathList,"dexElements",allDexElements);

        insertNativeLibrary(dexClassLoader);
    }

    private static PathClassLoader getPathClassLoader(){
        PathClassLoader pathClassLoader= (PathClassLoader) DexUtil.class.getClassLoader();
        return pathClassLoader;
    }

    private static Object getPathList(Object baseDexClassLoader) throws Exception{
        return ReflectUtil.getField(Class.forName("dalvik.system.BaseDexClassLoader"),
                baseDexClassLoader,"pathList");
    }

    private static Object getDexElements(Object pathList) throws Exception{
        return ReflectUtil.getField(pathList.getClass(),pathList,"dexElements");
    }

    private static Object combineArray(Object firstArray,Object secondArray){
        Class<?> localClass=firstArray.getClass().getComponentType();
        int firstArrayLength= Array.getLength(firstArray);
        int allLength=firstArrayLength+Array.getLength(secondArray);
        Object result=Array.newInstance(localClass,allLength);
        for(int k=0;k<allLength;k++){
            if(k<firstArrayLength){
                Array.set(result,k,Array.get(firstArray,k));
            }else{
                Array.set(result, k, Array.get(secondArray, k - firstArrayLength));
            }
        }
        return result;
    }

    private static synchronized void insertNativeLibrary(DexClassLoader dexClassLoader) throws Exception{
        if(sHasInsertedNativeLibrary){
            return;
        }
        sHasInsertedNativeLibrary=true;

        Object basePathList=getPathList(getPathClassLoader());
        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1){
            List<File> nativeLibraryDirectories= (List<File>)
                    ReflectUtil.getField(basePathList.getClass(),
                            basePathList,"nativeLibraryDirectories");
            nativeLibraryDirectories.add(Systems.getContext().getDir(Constants.NATIVE_DIR, Context.MODE_PRIVATE));

            Object baseNativeLibraryPathElements=ReflectUtil.getField(basePathList.getClass(),
                    basePathList,"nativeLibraryPathElements");
            final int baseArrayLength=Array.getLength(baseNativeLibraryPathElements);

            Object newPathList=getPathList(dexClassLoader);
            Object newNativeLibraryPathElements=ReflectUtil.getField(newPathList.getClass(),newPathList,
                    "nativeLibraryPathElements");
            Class<?> elementClass=newNativeLibraryPathElements.getClass().getComponentType();
            Object allNativeLibraryPathElements=Array.newInstance(elementClass,baseArrayLength+1);
            System.arraycopy(baseNativeLibraryPathElements,0,allNativeLibraryPathElements,0,baseArrayLength);

            Field dirField=elementClass.getDeclaredField("dir");
            dirField.setAccessible(true);
            final int newArrayLength=Array.getLength(newNativeLibraryPathElements);
            for(int i=0;i<newArrayLength;i++){
                Object element=Array.get(newNativeLibraryPathElements,i);
                String dir=((File)dirField.get(element)).getAbsolutePath();
                if(dir.contains(Constants.NATIVE_DIR)){
                    Array.set(allNativeLibraryPathElements,baseArrayLength,element);
                    break;
                }
            }

            ReflectUtil.setField(basePathList.getClass(),basePathList,
                    "nativeLibraryPathElements",allNativeLibraryPathElements);
        }else{
            File[] nativeLibraryDirectories= (File[]) ReflectUtil.getFieldNoException(basePathList.getClass(),
                    basePathList,"nativeLibraryDirectories");
            final int N=nativeLibraryDirectories.length;
            File[] newNativeLibraryDirectories=new File[N+1];
            System.arraycopy(nativeLibraryDirectories,0,newNativeLibraryDirectories,0,N);
            newNativeLibraryDirectories[N]=Systems.getContext().getDir(Constants.NATIVE_DIR,Context.MODE_PRIVATE);
            ReflectUtil.setField(basePathList.getClass(),basePathList,
                    "nativeLibraryDirectories",newNativeLibraryDirectories);
        }
    }


}








































