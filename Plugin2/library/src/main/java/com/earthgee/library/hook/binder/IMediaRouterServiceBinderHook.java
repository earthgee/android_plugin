package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IMediaRouterServiceHookHandle;
import com.earthgee.library.util.IMediaRouterServiceCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IMediaRouterServiceBinderHook extends BinderHook{

    private static final String SERVICE_NAME=Context.MEDIA_ROUTER_SERVICE;

    public IMediaRouterServiceBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IMediaRouterServiceCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IMediaRouterServiceHookHandle(mHostContext);
    }
}








