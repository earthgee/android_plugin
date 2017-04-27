package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IAppOpsServiceHookHandle;
import com.earthgee.library.util.IAppOpsServiceCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IAppOpsServiceBinderHook extends BinderHook{
    private static final String SERVICE_NAME = Context.APP_OPS_SERVICE;

    public IAppOpsServiceBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder = MyServiceManager.getOriginService(SERVICE_NAME);
        return IAppOpsServiceCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IAppOpsServiceHookHandle(mHostContext);
    }
}
