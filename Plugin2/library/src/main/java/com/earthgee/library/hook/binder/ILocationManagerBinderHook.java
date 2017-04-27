package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.ILocationManagerHookHandle;
import com.earthgee.library.util.ILocationManagerCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ILocationManagerBinderHook extends BinderHook{

    private static final String SERVICE_NAME=Context.LOCATION_SERVICE;

    public ILocationManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return ILocationManagerCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ILocationManagerHookHandle(mHostContext);
    }
}
