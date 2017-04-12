package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.util.INotificationManagerCompat;

/**
 * Created by zhaoruixuan on 2017/4/12.
 */
public class INotificationManagerBinderHook extends BinderHook{

    public static final String SERVICE_NAME="notification";

    public INotificationManagerBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return INotificationManagerCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new INotificationManagerHookHandle();
    }
}
