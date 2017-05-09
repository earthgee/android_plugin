package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IDisplayManagerHookHandle;
import com.earthgee.library.reflect.FieldUtils;
import com.earthgee.library.reflect.MethodUtils;
import com.earthgee.library.util.IDisplayManagerCompat;

/**
 * Created by zhaoruixuan on 2017/5/9.
 */
public class IDisplayManagerBinderHook extends BinderHook{
    private static final String SERVICE_NAME=Context.DISPLAY_SERVICE;

    public IDisplayManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IDisplayManagerCompat.asInterface(iBinder);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {
        super.onInstall(classLoader);
        Class displayManagerGlobalClass=Class.forName("android.hardware.display.DisplayManagerGlobal");
        Object displayManagerGlobal= MethodUtils.invokeStaticMethod(displayManagerGlobalClass,"getInstance");
        FieldUtils.writeField(displayManagerGlobal,"mDm",MyServiceManager.getProxiedObj(SERVICE_NAME));
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IDisplayManagerHookHandle(mHostContext);
    }
}

















