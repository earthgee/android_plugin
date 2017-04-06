package com.earthgee.library.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoruixuan on 2017/4/6.
 */
public class MethodUtils {

    private static Map<String,Method> sMethodCache=new HashMap<>();

    private static String getKey(final Class<?> cls,final String methodName,
                                 final Class<?>... parameterTypes){
        StringBuilder sb=new StringBuilder();
        sb.append(cls.toString()).append("#").append(methodName);
        if(parameterTypes!=null&&parameterTypes.length>0){
            for (Class<?> parameterType:parameterTypes){
                sb.append(parameterType.toString()).append("#");
            }
        }else{
            sb.append(Void.class.toString());
        }
        return sb.toString();
    }

    private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls
            ,final String methodName,final Class<?>... parameterTypes){
        for(;cls!=null;cls=cls.getSuperclass()){
            final Class<?>[] interfaces=cls.getInterfaces();
            for(int i=0;i<interfaces.length;i++){
                if(!Modifier.isPublic(interfaces[i].getModifiers())){
                    continue;
                }

                try{
                    return interfaces[i].getDeclaredMethod(methodName,parameterTypes);
                }catch (final NoSuchMethodException e){

                }

                Method method=getAccessibleMethodFromInterfaceNest(interfaces[i],methodName,parameterTypes);
                if(method!=null) return method;
            }
        }
        return null;
    }

    private static Method getAccessibleMethodFromSuperclass(final Class<?> cls
            ,final String methodName,final Class<?>... parameterTypes){
        Class<?> parentClass=cls.getSuperclass();
        while (parentClass)
    }

    private static Method getAccessibleMethod(Method method){
        if(MemberUtils.isAccessible(method)){
            return null;
        }

        final Class<?> cls=method.getDeclaringClass();
        if(Modifier.isPublic(cls.getModifiers())){
            return method;
        }

        final String methodName=method.getName();
        final Class<?>[] parameterTypes=method.getParameterTypes();

        method=getAccessibleMethodFromInterfaceNest(cls,methodName,parameterTypes);

        if(method==null){
            method=getAccessibleMethodFromSuperclass(cls,methodName,parameterTypes);
        }
        return method;
    }

    public static Object invokeStaticMethod(final Class clazz, final String methodName,
                                            Object... args) throws Exception {
        args = Utils.nullToEmpty(args);
        final Class<?>[] parameterTypes = Utils.toClass(args);
        return invokeStaticMethod(clazz, methodName, args, parameterTypes);
    }

    public static Object invokeStaticMethod(final Class clazz, final String methodName,
                                            Object[] args, Class<?>[] parameterTypes) throws Exception {
        parameterTypes=Utils.nullToEmpty(parameterTypes);
        args=Utils.nullToEmpty(args);
        final Method method=getMatchingAccessibleMethod(clazz,methodName,parameterTypes);
    }

    private static Method getMatchingAccessibleMethod(final Class<?> cls,final String methodName,
                                                      final Class<?>... parameterTypes){
        String key=getKey(cls,methodName,parameterTypes);
        Method cacheMethod;
        synchronized (sMethodCache){
            cacheMethod=sMethodCache.get(key);
        }
        if(cacheMethod!=null){
            if(!cacheMethod.isAccessible()){
                cacheMethod.setAccessible(true);
            }
            return cacheMethod;
        }

        try{
            final Method method=cls.getMethod(methodName,parameterTypes);
            MemberUtils.setAccessibleWorkaround(method);
            synchronized (sMethodCache){
                sMethodCache.put(key,method);
            }
            return method;
        }catch (NoSuchMethodException e){

        }
        Method bestMatch=null;
        final Method[] methods=cls.getMethods();
        for(final Method method:methods){
            if(method.getName().equals(methodName)&&
                    MemberUtils.isAssignable(parameterTypes,method.getParameterTypes(),true)){
                final Method accessibleMethod=getAccessibleMethod(method);

            }
        }
    }

}
















