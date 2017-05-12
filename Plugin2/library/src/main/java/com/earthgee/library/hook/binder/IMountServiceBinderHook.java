package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IMountServiceHookHandle;
import com.earthgee.library.util.IMountServiceCompat;

/**
 * Created by zhaoruixuan on 2017/4/25.
 * hook挂载服务
 */
public class IMountServiceBinderHook extends BinderHook{

    private static final String SERVICE_NAME="mount";

    public IMountServiceBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IMountServiceCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IMountServiceHookHandle(mHostContext);
    }
}
