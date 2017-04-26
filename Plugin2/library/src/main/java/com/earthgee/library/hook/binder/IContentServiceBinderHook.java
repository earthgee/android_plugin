package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IContentServiceHandle;
import com.earthgee.library.util.IContentServiceCompat;

/**
 * Created by zhaoruixuan on 2017/4/26.
 */
public class IContentServiceBinderHook extends BinderHook{

    private static final String CONTENT_SERVICE_NAME="content";

    public IContentServiceBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return CONTENT_SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(CONTENT_SERVICE_NAME);
        return IContentServiceCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IContentServiceHandle(mHostContext);
    }
}
