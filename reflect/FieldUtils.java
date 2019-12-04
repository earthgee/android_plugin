package com.earthgee.pluginlib.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class FieldUtils {

    private static Map<String, Field> sFieldCache=new HashMap<>();

    private static String getKey(Class<?> cls,String fieldName){
        StringBuilder sb=new StringBuilder();
        sb.append(cls.toString()).append("#").append(fieldName);
        return sb.toString();
    }

    public static Object readField(Field field,Object target,boolean forceAccess) throws IllegalAccessException{
        field.setAccessible(true);
        return field.get(target);
    }

    public static Object readField(Field field,Object target) throws IllegalAccessException {
        return readField(field, target,true);
    }

    public static Object readField(Object target,String fieldName) throws IllegalAccessException{
        if(target==null){
            return null;
        }

        Class<?> cls=target.getClass();
        Field field=getField(cls,fieldName,true);
        if(field==null){
            return null;
        }

        return readField(field,target,true);
    }

    public static Object readStaticField(Field field,boolean forceAccess) throws IllegalAccessException {
        return readField(field, null, forceAccess);
    }

    public static Object readStaticField(Class<?> cls,String fieldName) throws IllegalAccessException {
        Field field=getField(cls,fieldName,true);
        if(field==null){
            return null;
        }
        return readStaticField(field,true);
    }

    public static void writeField(Field field,Object target,Object value,boolean forceAccess) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(target,value);
    }

    public static void writeField(Object target,String fieldName,Object value) throws IllegalAccessException {
        if(target==null){
            return;
        }

        Class<?> cls=target.getClass();
        Field field=getField(cls,fieldName,true);
        if(field==null){
            return;
        }

        writeField(field,target,value,true);
    }

    public static void writeStaticField(Field field,Object value,boolean forceAccess) throws IllegalAccessException {
        writeField(field,null,value,true);
    }

    public static void writeStaticField(Class<?> cls,String fieldName,Object value) throws IllegalAccessException {
        Field field=getField(cls,fieldName,true);
        writeStaticField(field,value,true);
    }

    public static Object getField(Class<?> cls,String fieldName){
        return getField(cls, fieldName,true);
    }

    private static Field getField(Class<?> cls,String fieldName,boolean forceAccess){
        String key=getKey(cls,fieldName);
        Field cacheField;
        synchronized (sFieldCache){
            cacheField=sFieldCache.get(key);
        }
        if(cacheField!=null){
            if(forceAccess&&!cacheField.isAccessible()){
                cacheField.setAccessible(true);
            }
            return cacheField;
        }

        for(Class<?> acls=cls;acls!=null;acls=acls.getSuperclass()){
            try{
                Field field=acls.getDeclaredField(fieldName);
                if(!Modifier.isPublic(field.getModifiers())){
                    if(forceAccess){
                        field.setAccessible(true);
                    }else{
                        continue;
                    }
                }
                synchronized (sFieldCache){
                    sFieldCache.put(key,field);
                }
                return field;
            }catch (NoSuchFieldException ex){

            }
        }

        for(Class<?> icls:Utils.getAllInterfaces(cls)){
            try {
                Field field=icls.getField(fieldName);
                synchronized (sFieldCache){
                    sFieldCache.put(key,field);
                }
                return field;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static Field getDeclaredField(Class<?> cls,String fieldName){
        try {
            Field field=cls.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field;
        } catch (NoSuchFieldException e) {

        }
        return null;
    }

    public static void writeDeclaredField(Object target,String fieldName,Object value) throws IllegalAccessException {
        Class<?> cls=target.getClass();
        Field field=getDeclaredField(cls,fieldName);
        if(field==null){
            return;
        }

        writeField(field,target,value,true);
    }

}














