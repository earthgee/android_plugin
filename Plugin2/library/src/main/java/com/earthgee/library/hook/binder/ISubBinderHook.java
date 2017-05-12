package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.ISubBinderHookHandle;
import com.earthgee.library.util.ISubCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ISubBinderHook extends BinderHook{

    private final static String SERVICE_NAME = "isub";

    public ISubBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder = MyServiceManager.getOriginService(SERVICE_NAME);
        return ISubCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ISubBinderHookHandle(mHostContext);
    }
}
