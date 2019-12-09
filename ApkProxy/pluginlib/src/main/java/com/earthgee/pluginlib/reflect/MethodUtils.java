package com.earthgee.pluginlib.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class MethodUtils {

    private static Map<String, Method> sMethodCache=new HashMap<>();

    private static String getKey(Class<?> cls,String methodName,Class<?>... parameterTypes){
        StringBuilder sb=new StringBuilder();
        sb.append(cls.toString()).append("#").append(methodName);
        if(parameterTypes!=null&&parameterTypes.length>0){
            for(Class<?> parameterType:parameterTypes){
                sb.append(parameterTypes.toString()).append("#");
            }
        }else{
            sb.append(Void.class.toString());
        }
        return sb.toString();
    }

    private static Method getAccessibleMethodFromSuperClass(
            Class<?> cls,String methodName, Class<?>... parameterTypes){
        Class<?> parentClass=cls.getSuperclass();
        while (parentClass!=null){
            if(Modifier.isPublic(parentClass.getModifiers())){
                try {
                    return parentClass.getMethod(methodName,parameterTypes);
                } catch (NoSuchMethodException e) {
                    return null;
                }
            }
            parentClass=parentClass.getSuperclass();
        }
        return null;
    }

    private static Method getAccessibleMethodFromInterfaceNest(
            Class<?> cls,String methodName,Class<?>... parameterTypes){
        for(;cls!=null;cls=cls.getSuperclass()){
            Class<?>[] interfaces=cls.getInterfaces();
            for(int i=0;i<interfaces.length;i++){
                if(!Modifier.isPublic(interfaces[i].getModifiers())){
                    continue;
                }

                try {
                    return interfaces[i].getDeclaredMethod(methodName,parameterTypes);
                } catch (NoSuchMethodException e) {

                }

                Method method=getAccessibleMethodFromInterfaceNest(interfaces[i],methodName,parameterTypes);
                if(method!=null){
                    return method;
                }
            }
        }

        return null;
    }

    private static Method getAccessibleMethod(Method method){
        Class<?> cls=method.getDeclaringClass();
        if(Modifier.isPublic(cls.getModifiers())){
            return method;
        }

        String methodName=method.getName();
        Class<?>[] parameterTypes=method.getParameterTypes();

        method=getAccessibleMethodFromInterfaceNest(cls,methodName,parameterTypes);

        if(method==null){
            method=getAccessibleMethodFromSuperClass(cls,methodName,parameterTypes);
        }
        return method;
    }

    private static Method getAccessibleMethod(
            Class<?> cls,String methodName,Class<?>... parameterTypes) throws NoSuchMethodException {
        String key=getKey(cls, methodName, parameterTypes);
        Method method;
        synchronized (sMethodCache){
            method=sMethodCache.get(key);
        }

        if(method!=null&&!method.isAccessible()){
            method.setAccessible(true);
            return method;
        }

        Method accessibleMethod=getAccessibleMethod(cls.getMethod(methodName,parameterTypes));
        synchronized (sMethodCache){
            sMethodCache.put(key,accessibleMethod);
        }
        return accessibleMethod;
    }

    public static Object invokeMethod(
            Object object,String methodName,Object[] args,Class<?>[] parameterTypes)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method=getAccessibleMethod(object.getClass(),methodName,parameterTypes);
        if(method!=null){
            return method.invoke(object,args);
        }

        return null;
    }

    public static Object invokeStaticMethod(Class clazz,String methodName,
                                            Object[] args,Class<?>[] parameterTypes) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method=getAccessibleMethod(clazz,methodName,parameterTypes);
        if(method==null){
            return null;
        }
        return method.invoke(null,args);
    }

    public static Object invokeStaticMethod(Class clazz,String methodName,Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] parameterTypes=Utils.toClass(args);
        return invokeStaticMethod(clazz, methodName, args,parameterTypes);
    }

    public static Object invokeMethod(Object object,String methodName,Object... args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?>[] parameterTypes=Utils.toClass(args);
        return invokeMethod(object, methodName, args, parameterTypes);
    }

    public static <T> T invokeConstructor(Class<T> cls,Object... args) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?>[] parameterTypes=Utils.toClass(args);
        return invokeConstructor(cls,args,parameterTypes);
    }

    public static <T> T invokeConstructor(Class<T> cls,Object[] args,Class<?>[] parameterTypes) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> ctor=getMatchingAccessibleConstructor(cls,parameterTypes);
        if(ctor!=null){
            return ctor.newInstance(args);
        }
        return null;
    }

    public static <T> Constructor<T> getMatchingAccessibleConstructor(
            Class<T> cls,Class<?>... parameterTypes)  {
        try {
            Constructor<T> ctor=cls.getConstructor(parameterTypes);
            ctor.setAccessible(true);
            return ctor;
        } catch (NoSuchMethodException e) {
        }

        return null;
    }

}














