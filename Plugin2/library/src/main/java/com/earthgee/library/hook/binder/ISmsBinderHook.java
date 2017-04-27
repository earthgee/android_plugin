package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.ISmsHookHandle;
import com.earthgee.library.util.ISmsCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ISmsBinderHook extends BinderHook{
    private static final String SERVICE_NAME = "isms";

    public ISmsBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder = MyServiceManager.getOriginService(SERVICE_NAME);
        return ISmsCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ISmsHookHandle(mHostContext);
    }
}
