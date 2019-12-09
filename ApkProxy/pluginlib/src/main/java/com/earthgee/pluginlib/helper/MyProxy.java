package com.earthgee.pluginlib.helper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class MyProxy {

    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, InvocationHandler invocationHandler){
        return Proxy.newProxyInstance(loader, interfaces, invocationHandler);
    }

}
