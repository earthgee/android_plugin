package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IMmsHookHandle;
import com.earthgee.library.util.IMmsCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IMmsBinderHook extends BinderHook{
    private static final String SERVICE_NAME = "imms";

    public IMmsBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder = MyServiceManager.getOriginService(SERVICE_NAME);
        return IMmsCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IMmsHookHandle(mHostContext);
    }
}
