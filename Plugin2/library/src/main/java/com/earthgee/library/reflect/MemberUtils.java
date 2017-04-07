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
    private static final Class<?>[] ORDERED_PRIMITIVE_TYPES={Byte.TYPE,Short.TYPE,Character.TYPE,
            Integer.TYPE,Long.TYPE,Float.TYPE,Double.TYPE};

    private static boolean isPackageAccess(final int modifiers){
        return (modifiers&ACCESS_TEST)==0;
    }

    static boolean isAccessible(final Member m){
        return m!=null&&Modifier.isPublic(m.getModifiers())&&!m.isSynthetic();
    }

    /**
     * ??? 这里我的理解是尽量不去反射private的method,并且对于内部类中的public方法赋予其权限
     * @param o
     * @return
     */
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

    static boolean isAssignable(final Class<?> cls,final Class<?> toClass){
        return isAssignable(cls,toClass,true);
    }

    /**
     * classArray是否在toClassArray的范围之内
     * @param classArray
     * @param toClassArray
     * @param autoboxing 是否允许两边是装箱的类型
     * @return
     */
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
            //每个class比较
            if(isAssignable(classArray[i],toClassArray[i],autoboxing)==false){
                return false;
            }
        }
        return true;
    }

    /**
     * cls是否在toClass范围之内
     * @param cls
     * @param toClass
     * @param autoBoxing
     * @return
     */
    static boolean isAssignable(Class<?> cls,
                                final Class<?> toClass,final boolean autoBoxing){
        if(toClass==null){
            return false;
        }
        if(cls==null){
            return !toClass.isPrimitive();
        }
        //允许装箱
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

    //装箱类型判断
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

    /**
     * 用cost来衡量谁和actual的参数类型更匹配
     * @param left
     * @param right
     * @param actual
     * @return
     */
    static int compareParameterTypes(final Class<?>[] left,final Class<?>[] right,final Class<?>[] actual){
        final float leftCost=getTotalTransformationCost(actual,left);
        final float rightCost=getTotalTransformationCost(actual,right);
        return leftCost<rightCost?-1:rightCost<leftCost?1:0;
    }

    /**
     * @param srcArgs 衡量的基石
     * @param destArgs 待计算对象
     * @return
     */
    private static float getTotalTransformationCost(final Class<?>[] srcArgs,
                                                    final Class<?>[] destArgs){
        float totalCost=0.0f;
        for(int i=0;i<srcArgs.length;i++){
            Class<?> srcClass,destClass;
            srcClass=srcArgs[i];
            destClass=destArgs[i];
            totalCost+=getObjectTransformationCost(srcClass,destClass);
        }
        return totalCost;
    }

    /**
     *
     * @param srcClass 衡量的基石
     * @param destClass 待计算对象
     * @return
     */
    private static float getObjectTransformationCost(Class<?> srcClass,final Class<?> destClass){
        //基本类型的分支
        if(destClass.isPrimitive()){
            return getPrimitivePromotionCost(srcClass,destClass);
        }

        //引用类型的分支
        float cost=0.0f;
        while (srcClass!=null&&!destClass.equals(srcClass)){
            if(destClass.isInterface()&&isAssignable(srcClass, destClass)){
                cost+=0.25f;
                break;
            }
            cost++;
            srcClass=srcClass.getSuperclass();
        }

        if(srcClass==null){
            cost+=1.5f;
        }
        return cost;
    }

    private static float getPrimitivePromotionCost(final Class<?> srcClass,
                                                   final Class<?> destClass){
        float cost=0.0f;
        Class<?> cls=srcClass;
        if(!cls.isPrimitive()){
            cost+=0.1f;
            cls=wrapperToPrimitive(cls);
        }
        for(int i=0;cls!=destClass&&i<ORDERED_PRIMITIVE_TYPES.length;i++){
            if(cls==ORDERED_PRIMITIVE_TYPES[i]){
                cost+=0.1f;
                if (i < ORDERED_PRIMITIVE_TYPES.length - 1) {
                    cls = ORDERED_PRIMITIVE_TYPES[i + 1];
                }
            }
        }
        return cost;
    }

}
