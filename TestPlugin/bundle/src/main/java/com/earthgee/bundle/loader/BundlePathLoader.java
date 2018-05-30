package com.earthgee.bundle.loader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Created by earthgee on 18/5/25.
 */

public class BundlePathLoader {

    static final String TAG="BundlePathLoader";

    private BundlePathLoader(){
    }

    public static void installBundleDexs(ClassLoader loader
            , File dexDir, List<File> files, boolean isHotFix)
        throws IllegalArgumentException,IllegalAccessException,NoSuchFieldException,
        InstantiationException,InvocationTargetException,NoSuchMethodException,IOException{
        if(!files.isEmpty()){
            V23.install(loader,files,dexDir,isHotFix);
        }
    }

    private static final class V23{
        private static void install(ClassLoader loader,List<File> additionCLassPathEntries
                ,File optimizedDirectory,boolean isHotFix) throws IllegalArgumentException,IllegalAccessException,NoSuchFieldException,
                InstantiationException,InvocationTargetException,NoSuchMethodException,IOException{
            Field pathListField=findField(loader,"pathList");
            Object dexPathList=pathListField.get(loader);
            Field dexElement=findField(dexPathList,"dexElements");
            Class<?> elementType=dexElement.getType().getComponentType();
            Method loadDex=findMethod(dexPathList,"loadDexFile",File.class,File.class);
            Object dex=loadDex.invoke(dexPathList,additionCLassPathEntries.get(0),optimizedDirectory);
            Constructor<?> constructor=elementType.getConstructor(File.class,boolean.class,File.class, DexFile.class);
            Object element=constructor.newInstance(new File(""),false,additionCLassPathEntries.get(0),dex);
            Object[] newEles=new Object[1];
            newEles[0]=element;
            expandFieldArray(dexPathList,"dexElements",newEles,isHotFix);
        }
    }

    private static void expandFieldArray(Object instance,String fieldName,
                                         Object[] extraElements,boolean isHotFix)
                                        throws NoSuchFieldException,IllegalArgumentException,IllegalAccessException{
       synchronized (BundlePathLoader.class){
           Field jlrField=findField(instance,fieldName);
           Object[] original= (Object[]) jlrField.get(instance);
           Object[] combined= (Object[]) Array.newInstance(original.getClass().getComponentType(),
                   original.length+extraElements.length);
           if(isHotFix){
               System.arraycopy(extraElements,0,combined,0,extraElements.length);
               System.arraycopy(original,0,combined,extraElements.length,original.length);
           }else{
               System.arraycopy(original,0,combined,0,original.length);
               System.arraycopy(extraElements,0,combined,original.length,extraElements.length);
           }
           jlrField.set(instance,combined);
       }
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

    private static Method findMethod(Object instance,String name,Class<?>... parameterTypes)
     throws NoSuchMethodException{
        for(Class<?> clazz=instance.getClass();clazz!=null;clazz=clazz.getSuperclass()){
            try{
                Method method=clazz.getDeclaredMethod(name, parameterTypes);

                if(!method.isAccessible()){
                    method.setAccessible(true);
                }

                return method;
            }catch (NoSuchMethodException e){

            }
        }

        throw new NoSuchMethodException("Method "+name+" with parameters "+ Arrays.asList(parameterTypes)+" no found in "
                +instance.getClass());
    }

}
