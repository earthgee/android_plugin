package com.earthgee.library.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/6.
 */
public class MemberUtils {

    private static final int ACCESS_TEST=Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE;

    private static boolean isPackageAccess(final int modifiers){
        return (modifiers&ACCESS_TEST)==0;
    }

    static boolean isAccessible(final Member m){
        return m!=null&&Modifier.isPublic(m.getModifiers())&&!m.isSynthetic();
    }

    static boolean setAccessibleWorkaround(final AccessibleObject o){
        if(o==null||o.isAccessible()){
            return false;
        }
        final Member m= (Member) o;
        if(!o.isAccessible()&& Modifier.isPublic(m.getModifiers())&&
                isPackageAccess(m.getDeclaringClass().getModifiers())){
            try{
                o.setAccessible(true);
                return true;
            }catch (SecurityException e){

            }
        }
        return false;
    }

    static boolean isAssignable(Class<?>[] classArray,
                                Class<?>[] toClassArray,final boolean autoboxing){
        if(Utils.isSameLength(classArray,toClassArray)==false){
            return false;
        }
        if(classArray==null){
            classArray=Utils.EMPTY_CLASS_ARRAY;
        }
        if(toClassArray==null){
            toClassArray=Utils.EMPTY_CLASS_ARRAY;
        }
        for(int i=0;i<classArray.length;i++){
            if(isAssignable(classArray[i],toClassArray[i],autoboxing)==false){
                return false;
            }
        }
        return true;
    }

    static boolean isAssignable(Class<?> cls,
                                final Class<?> toClass,final boolean autoBoxing){
        if(toClass==null){
            return false;
        }
        if(cls==null){
            return !toClass.isPrimitive();
        }
        if(autoBoxing){
            if(cls.isPrimitive()&&!toClass.isPrimitive()){
                cls=primitiveToWrapper(cls);
                if(cls==null){
                    return false;
                }
            }
            if(toClass.isPrimitive()&&!cls.isPrimitive()){
                cls=wrapperToPrimitive(cls);
                if(cls==null){
                    return false;
                }
            }
        }
        if(cls.equals(toClass)){
            return true;
        }
        if(cls.isPrimitive()){
            if(toClass.isPrimitive()==false){
                return false;
            }
            if(Integer.TYPE.equals(cls)){
                return Long.TYPE.equals(toClass)||Float.TYPE.equals(toClass)||Double.TYPE.equals(toClass);
            }
            if(Long.TYPE.equals(cls)){
                return Float.TYPE.equals(toClass)||Double.TYPE.equals(toClass);
            }
            if(Boolean.TYPE.equals(cls)){
                return false;
            }
            if (Double.TYPE.equals(cls)) {
                return false;
            }
            if (Float.TYPE.equals(cls)) {
                return Double.TYPE.equals(toClass);
            }
            if (Character.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Short.TYPE.equals(cls)) {
                return Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            if (Byte.TYPE.equals(cls)) {
                return Short.TYPE.equals(toClass)
                        || Integer.TYPE.equals(toClass)
                        || Long.TYPE.equals(toClass)
                        || Float.TYPE.equals(toClass)
                        || Double.TYPE.equals(toClass);
            }
            return false;
        }
        return toClass.isAssignableFrom(cls);
    }

    private static final Map<Class<?>,Class<?>> primitiveWrapperMap=new HashMap<>();

    static {
        primitiveWrapperMap.put(Boolean.TYPE,Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE,Byte.class);
        primitiveWrapperMap.put(Character.TYPE,Character.class);
        primitiveWrapperMap.put(Short.TYPE,Short.class);
        primitiveWrapperMap.put(Integer.TYPE,Integer.class);
        primitiveWrapperMap.put(Long.TYPE,Long.class);
        primitiveWrapperMap.put(Double.TYPE,Double.class);
        primitiveWrapperMap.put(Float.TYPE,Float.class);
        primitiveWrapperMap.put(Void.TYPE,Void.class);
    }

    private static final Map<Class<?>,Class<?>> wrapperPrimitiveMap=new HashMap<>();

    static {
        for(final Class<?> primitiveClass:primitiveWrapperMap.keySet()){
            final Class<?> wrapperClass=primitiveWrapperMap.get(primitiveClass);
            if(!primitiveClass.equals(wrapperClass)){
                wrapperPrimitiveMap.put(wrapperClass,primitiveClass);
            }
        }
    }

    static Class<?> primitiveToWrapper(final Class<?> cls){
        Class<?> convertedClass=cls;
        if(cls!=null&&cls.isPrimitive()){
            convertedClass=primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }

    static Class<?> wrapperToPrimitive(final Class<?> cls){
        return wrapperPrimitiveMap.get(cls);
    }

}
