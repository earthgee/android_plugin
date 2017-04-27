package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IWifiManagerHookHandle;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.util.IWifiManagerCompat;
import com.earthgee.library.util.ServiceManagerCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IWifiManagerBinderHook extends BinderHook{

    private static final String SERVICE_NAME="wifi";

    public IWifiManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IWifiManagerCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IWifiManagerHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        super.onInstall(classLoader);
        fixZTESecurity();
    }

    private void fixZTESecurity(){
        try {
            Object proxyServiceIBinder=MyServiceManager.getProxiedObj(getServiceName());
            IBinder serviceIBinder= ServiceManagerCompat.getService(getServiceName());
            if(serviceIBinder!=null&&proxyServiceIBinder!=null&&
                    "com.zte.ZTESecurity.ZTEWifiService".equals(serviceIBinder.getClass().getName())){
                Object obj= FieldUtils.readField(serviceIBinder,"mIWifiManager");
                setOldObj(obj);
                FieldUtils.writeField(serviceIBinder,"mIWifiManager",proxyServiceIBinder);
            }
        }catch (Exception e){
        }
    }
}





















