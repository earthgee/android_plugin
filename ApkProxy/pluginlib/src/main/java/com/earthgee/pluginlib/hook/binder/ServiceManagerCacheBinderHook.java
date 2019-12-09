package com.earthgee.pluginlib.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.pluginlib.helper.compat.ServiceManagerCompat;
import com.earthgee.pluginlib.hook.BaseHookHandle;
import com.earthgee.pluginlib.hook.Hook;
import com.earthgee.pluginlib.reflect.FieldUtils;

import java.lang.reflect.Proxy;
import java.util.Map;

public class ServiceManagerCacheBinderHook extends Hook {

    private String mServiceName;

    protected ServiceManagerCacheBinderHook(Context hostContext,String serviceName) {
        super(hostContext);
        mServiceName=serviceName;
        setEnable(true);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
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
                //MyServiceManager.addOriginService(mServiceName,mServiceIBinder);
            }
        }
    }

}
