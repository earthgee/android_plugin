package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.ISessionManagerHookHandle;
import com.earthgee.library.util.ISessionManagerCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ISessionManagerBinderHook extends BinderHook{

    private static final String SERVICE_NAME=Context.MEDIA_SESSION_SERVICE;

    public ISessionManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return ISessionManagerCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ISessionManagerHookHandle(mHostContext);
    }
}
