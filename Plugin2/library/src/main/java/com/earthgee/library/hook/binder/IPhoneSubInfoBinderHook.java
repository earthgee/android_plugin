package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IPhoneSubInfoHookHandle;
import com.earthgee.library.util.IPhoneSubInfoCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IPhoneSubInfoBinderHook extends BinderHook{

    private static final String SERVICE_NAME = "iphonesubinfo";

    public IPhoneSubInfoBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder = MyServiceManager.getOriginService(SERVICE_NAME);
        return IPhoneSubInfoCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IPhoneSubInfoHookHandle(mHostContext);
    }
}
