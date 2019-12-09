package com.earthgee.pluginlib.reflect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class Utils {

    static final Class<?>[] EMPTY_CLASS_ARRAY=new Class[0];

    public static List<Class<?>> getAllInterfaces(Class<?> cls){
        if(cls==null){
            return null;
        }
        final LinkedHashSet<Class<?>> interfacesFound=new LinkedHashSet<>();
        getAllInterfaces(cls,interfacesFound);
        return new ArrayList<Class<?>>(interfacesFound);
    }

    private static void getAllInterfaces(Class<?> cls, HashSet<Class<?>> interfacesFound){
        while (cls!=null){
            Class<?>[] interfaces=cls.getInterfaces();

            for(Class<?> i:interfaces){
                if(interfacesFound.add(i)){
                    getAllInterfaces(i,interfacesFound);
                }
            }

            cls=cls.getSuperclass();
        }
    }

    static Class<?>[] toClass(Object... array){
        if(array==null){
            return null;
        }else if(array.length==0){
            return EMPTY_CLASS_ARRAY;
        }
        final Class<?>[] classes=new Class[array.length];
        for(int i=0;i<array.length;i++){
            classes[i]=array[i]==null?null:array[i].getClass();
        }
        return classes;
    }

}












