package com.earthgee.library;


import java.lang.reflect.Array;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by zhaoruixuan on 2017/6/30.
 */
public class DexUtils {

    public static void injectDexAtFirst(String dexPath,String defaultDexOptPath) throws Exception{
        DexClassLoader dexClassLoader=new DexClassLoader(dexPath,defaultDexOptPath,dexPath,getPathClassLoader());
        Object baseDexElements=getDexElements(getPathList(getPathClassLoader()));
        Object newDexElements=getDexElements(getPathList(dexClassLoader));
        Object allDexElements=combineArray(newDexElements,baseDexElements);
        Object pathList=getPathList(getPathClassLoader());
        ReflectionUtils.setField(pathList,pathList.getClass(),"dexElements",allDexElements);
    }

    private static PathClassLoader getPathClassLoader(){
        PathClassLoader pathClassLoader= (PathClassLoader) DexUtils.class.getClassLoader();
        return pathClassLoader;
    }

    private static Object getPathList(Object baseDexClassLoader) throws Exception{
        return ReflectionUtils.getFiled(baseDexClassLoader,Class.forName("dalvik.system.BaseDexClassLoader"),"pathList");
    }

    private static Object getDexElements(Object paramObject) throws Exception{
        return ReflectionUtils.getFiled(paramObject,paramObject.getClass(),"dexElements");
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
                Array.set(result,k,Array.get(secondArray,k-firstArrayLength));
            }
        }
        return result;
    }

}


















