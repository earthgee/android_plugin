package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IInputMethodManagerHookHandle;
import com.earthgee.library.util.IInputMethodManagerCompat;

/**
 * Created by zhaoruixuan on 2017/4/27.
 */
public class IInputMethodManagerBinderHook extends BinderHook{

    private final static String SERVICE_NAME=Context.INPUT_METHOD_SERVICE;

    public IInputMethodManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IInputMethodManagerCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IInputMethodManagerHookHandle(mHostContext);
    }
}
