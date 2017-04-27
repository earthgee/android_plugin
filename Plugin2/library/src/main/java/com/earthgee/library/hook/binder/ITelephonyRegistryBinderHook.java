package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.ITelephonyRegistryHookHandle;
import com.earthgee.library.util.ITelephonyRegistryCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ITelephonyRegistryBinderHook extends BinderHook{

    private static final String SERVICE_NAME="telephony.registry";

    public ITelephonyRegistryBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return ITelephonyRegistryCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ITelephonyRegistryHookHandle(mHostContext);
    }
}














