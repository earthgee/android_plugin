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

    /**
     * 从接口中查找合适方法
     * @param cls
     * @param methodName
     * @param parameterTypes
     * @return
     */
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

    /**
     * 从父类中查找合适方法
     * @param cls
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private static Method getAccessibleMethodFromSuperclass(final Class<?> cls
            ,final String methodName,final Class<?>... parameterTypes){
        Class<?> parentClass=cls.getSuperclass();
        while (parentClass!=null){
            if(Modifier.isPublic(parentClass.getModifiers())){
                try{
                    return parentClass.getMethod(methodName,parameterTypes);
                }catch (NoSuchMethodException e){
                    return null;
                }
            }
            parentClass=parentClass.getSuperclass();
        }
        return null;
    }

    /**
     * 判断找到的方法是否完全可用
     * @param method
     * @return
     */
    private static Method getAccessibleMethod(Method method){
        if(!MemberUtils.isAccessible(method)){
            return null;
        }

        final Class<?> cls=method.getDeclaringClass();
        if(Modifier.isPublic(cls.getModifiers())){
            return method;
        }

        final String methodName=method.getName();
        final Class<?>[] parameterTypes=method.getParameterTypes();

        //从实现的接口中找合适的方法
        method=getAccessibleMethodFromInterfaceNest(cls,methodName,parameterTypes);

        if(method==null){
            //从父类中找合适的方法
            method=getAccessibleMethodFromSuperclass(cls,methodName,parameterTypes);
        }
        return method;
    }

    /**
     * 通过反射调用静态方法
     * @param clazz 反射的类名
     * @param methodName 反射的方法名
     * @param args 参数
     * @return
     * @throws Exception
     */
    public static Object invokeStaticMethod(final Class clazz, final String methodName,
                                            Object... args) throws Exception {
        args = Utils.nullToEmpty(args);
        //将参数的类类型都提取到一个数组里
        final Class<?>[] parameterTypes = Utils.toClass(args);
        return invokeStaticMethod(clazz, methodName, args, parameterTypes);
    }

    /**
     *
     * @param clazz 反射的类名
     * @param methodName 反射的方法名
     * @param args 参数
     * @param parameterTypes 参数的类类型
     * @return
     * @throws Exception
     */
    public static Object invokeStaticMethod(final Class clazz, final String methodName,
                                            Object[] args, Class<?>[] parameterTypes) throws Exception {
        parameterTypes=Utils.nullToEmpty(parameterTypes);
        args=Utils.nullToEmpty(args);
        //通过给定参数找到合适的方法
        final Method method=getMatchingAccessibleMethod(clazz,methodName,parameterTypes);
        if(method==null){
            throw new NoSuchMethodException("No such accessible method: "+
                    methodName+"() on object: "+clazz.getName());
        }
        return method.invoke(null,args);
    }

    /**
     * 寻找合适方法
     * @param cls 类信息
     * @param methodName 方法信息
     * @param parameterTypes 参数信息
     * @return
     */
    private static Method getMatchingAccessibleMethod(final Class<?> cls,final String methodName,
                                                      final Class<?>... parameterTypes){
        //生成缓存key
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
            //直接根据方法名和参数信息去查找方法
            final Method method=cls.getMethod(methodName,parameterTypes);
            MemberUtils.setAccessibleWorkaround(method);
            synchronized (sMethodCache){
                sMethodCache.put(key,method);
            }
            return method;
        }catch (NoSuchMethodException e){

        }
        //如果没有找到完全一致的方法，可以降级，寻找比较匹配的方法
        Method bestMatch=null;
        final Method[] methods=cls.getMethods();
        for(final Method method:methods){
            if(method.getName().equals(methodName)&&
                    MemberUtils.isAssignable(parameterTypes,method.getParameterTypes(),true)){
                //如果可以匹配，那么进一步处理
                final Method accessibleMethod=getAccessibleMethod(method);
                if(accessibleMethod!=null&&
                        (bestMatch==null
                                ||MemberUtils.compareParameterTypes(accessibleMethod.getParameterTypes(),
                                bestMatch.getParameterTypes(),parameterTypes)<0)){
                    bestMatch=accessibleMethod;
                }
            }
        }
        if(bestMatch!=null){
            MemberUtils.setAccessibleWorkaround(bestMatch);
        }
        synchronized (sMethodCache){
            sMethodCache.put(key,bestMatch);
        }
        return bestMatch;
    }

    

}
















