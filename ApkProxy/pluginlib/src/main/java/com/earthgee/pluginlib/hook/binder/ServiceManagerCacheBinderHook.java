package com.earthgee.pluginlib.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.pluginlib.helper.MyProxy;
import com.earthgee.pluginlib.helper.compat.MyServiceManager;
import com.earthgee.pluginlib.helper.compat.ServiceManagerCompat;
import com.earthgee.pluginlib.hook.BaseHookHandle;
import com.earthgee.pluginlib.hook.Hook;
import com.earthgee.pluginlib.hook.HookedMethodHandler;
import com.earthgee.pluginlib.reflect.FieldUtils;
import com.earthgee.pluginlib.reflect.Utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

public class ServiceManagerCacheBinderHook extends Hook implements InvocationHandler {

    private String mServiceName;

    protected ServiceManagerCacheBinderHook(Context hostContext,String serviceName) {
        super(hostContext);
        mServiceName=serviceName;
        setEnable(true);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ServiceManagerHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Exception {
        Object sCacheObj= FieldUtils.readStaticField(ServiceManagerCompat.Class(),"sCache");
        if(sCacheObj instanceof Map){
            Map sCache= (Map) sCacheObj;
            Object obj=sCache.get(mServiceName);
            sCache.remove(mServiceName);
            IBinder mServiceIBinder=ServiceManagerCompat.getService(mServiceName);
            if(mServiceIBinder==null){
                if(obj!=null&&obj instanceof IBinder&&!Proxy.isProxyClass(obj.getClass())){
                    mServiceIBinder= (IBinder) obj;
                }
            }
            if(mServiceIBinder!=null){
                MyServiceManager.addOriginService(mServiceName,mServiceIBinder);
                Class clazz=mServiceIBinder.getClass();
                List<Class<?>> interfaces= Utils.getAllInterfaces(clazz);
                Class[] ifs=interfaces!=null&&interfaces.size()>0?interfaces.toArray(new Class[interfaces.size()]):new Class[0];
                IBinder mProxyServiceIBinder= (IBinder) MyProxy.newProxyInstance(clazz.getClassLoader(),ifs,this);
                sCache.put(mServiceName,mProxyServiceIBinder);
                MyServiceManager.addProxiedServiceCache(mServiceName,mProxyServiceIBinder);
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try{
            IBinder originService=MyServiceManager.getOriginService(mServiceName);
            if(!isEnable()){
                return method.invoke(originService,args);
            }
            HookedMethodHandler hookedMethodHandler=mHookHandles.getHookedMethodHandler(method);
            if(hookedMethodHandler!=null){
                return hookedMethodHandler.doHookInner(originService,method,args);
            }else{
                return method.invoke(originService,args);
            }
        }catch (Exception e){
            //ignore
        }
        return null;
    }

    private class ServiceManagerHookHandle extends BaseHookHandle{

        public ServiceManagerHookHandle(Context hostContext) {
            super(hostContext);
        }

        @Override
        protected void init() {
            sHookMethodHandlers.put("queryLocalInterface",new QueryLocalInterface(mHostContext));
        }

        class QueryLocalInterface extends HookedMethodHandler{

            public QueryLocalInterface(Context hostContext) {
                super(hostContext);
            }

            @Override
            protected void afterInvoke(Object receiver, Method method, Object[] args, Object invokeResult) throws Exception {
                Object localInterface=invokeResult;
                Object proxiedObj=MyServiceManager.getProxiedObj(mServiceName);
                if(localInterface==null&&proxiedObj!=null){
                    setFakedResult(proxiedObj);
                }
            }
        }

    }

}









