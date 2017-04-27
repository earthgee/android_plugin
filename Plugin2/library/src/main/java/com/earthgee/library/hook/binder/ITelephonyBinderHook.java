package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.ITelephonyHookHandle;
import com.earthgee.library.util.ITelephonyCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class ITelephonyBinderHook extends BinderHook{

    public ITelephonyBinderHook(Context hostContext) {
        super(hostContext);
    }


    private final static String SERVICE_NAME = Context.TELEPHONY_SERVICE;

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder = MyServiceManager.getOriginService(SERVICE_NAME);
        return ITelephonyCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new ITelephonyHookHandle(mHostContext);
    }
}
