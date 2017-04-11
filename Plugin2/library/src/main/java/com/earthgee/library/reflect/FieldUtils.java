package com.earthgee.library.reflect;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/7.
 */
public class FieldUtils {

    private static Map<String,Field> sFieldCache=new HashMap<>();

    private static String getKey(Class<?> cls,String fieldName){
        StringBuilder sb=new StringBuilder();
        sb.append(cls.toString()).append("#").append(fieldName);
        return sb.toString();
    }

    public static Object readField(final Object target,final String fieldName) throws Exception{
        Validate.isTrue(target!=null,"target object must not be null");
        final Class<?> cls=target.getClass();
        final Field field=getField(cls,fieldName,true);
        Validate.isTrue(field!=null,"cannot locate field %s on %s",fieldName,cls);
        return readField(field,target,false);
    }

    public static Object readField(final Object target,
                                   final String fieldName,final boolean forceAccess) throws Exception{
        Validate.isTrue(target!=null,"target object must not be null");
        final Class<?> cls=target.getClass();
        final Field field=getField(cls,fieldName,forceAccess);
        Validate.isTrue(field!=null,"cannot locate field %s on %s",fieldName,cls);
        return readField(field,target,forceAccess);
    }

    public static Object readField(final Field field,final Object target,final boolean forceAccess) throws Exception{
        Validate.isTrue(field!=null,"the field must not be null");
        if(forceAccess&&!field.isAccessible()){
            field.setAccessible(true);
        }else{
            MemberUtils.setAccessibleWorkaround(field);
        }
        return field.get(target);
    }

    private static Field getField(Class<?> cls,String fieldName,final boolean forceAccess){
        Validate.isTrue(cls!=null,"the class must not be null");
        Validate.isTrue(!TextUtils.isEmpty(fieldName),"The field name must not be blank/empty");

        String key=getKey(cls, fieldName);
        Field cacheField;
        synchronized(sFieldCache){
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
                final Field field=acls.getDeclaredField(fieldName);
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
            }catch (NoSuchFieldException e){

            }
        }

        Field match=null;
        for(final Class<?> class1:Utils.getAllInterfaces(cls)){
            try {
                Field test=class1.getField(fieldName);
                match=test;
            }catch (NoSuchFieldException e){

            }
        }
        synchronized (sFieldCache){
            sFieldCache.put(key,match);
        }
        return match;
    }

    public static void writeField(final Object target,
                                  final String fieldName,final Object value) throws Exception{
        writeField(target,fieldName,value,true);
    }

    public static void writeField(final Object target,final String fieldName,
                                  final Object value,final boolean forceAccess) throws Exception{
        Validate.isTrue(target!=null,"target object must not be null");
        final Class<?> cls=target.getClass();
        final Field field=getField(cls,fieldName,true);
        Validate.isTrue(field!=null,"Cannot locate declared field %s.%s",cls.getName(),fieldName);;
        writeField(field,target,value,forceAccess);
    }

    public static void writeField(final Field field,final Object target,final Object value) throws Exception{
        writeField(field, target, value,true);
    }

    public static void writeField(final Field field,final Object target,
                                  final Object value,final boolean forceAccess) throws Exception{
        Validate.isTrue(field!=null,"The field must not be null");
        if(forceAccess&&!field.isAccessible()){
            field.setAccessible(true);
        }else{
            MemberUtils.setAccessibleWorkaround(field);
        }
        field.set(target,value);
    }

    public static Object readStaticField(final Class<?> cls
            , final String fieldName) throws Exception{
        final Field field=getField(cls,fieldName,true);
        Validate.isTrue(field!=null,"cannot locate field '%s' on %s",fieldName,cls);
        return readStaticField(field,true);
    }

    public static Object readStaticField(final Field field
            ,final boolean forceAccess) throws Exception{
        Validate.isTrue(field!=null,"The field must not be null");
        Validate.isTrue(Modifier.isStatic(field.getModifiers()),
                "The field '%s' is not static",field.getName());
        return readField(field,(Object) null,forceAccess);
    }

}

































