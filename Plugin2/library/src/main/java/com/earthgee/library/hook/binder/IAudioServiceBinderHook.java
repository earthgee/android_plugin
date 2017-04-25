package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IAudioServiceHookHandle;
import com.earthgee.library.util.IAudioServiceCompat;

/**
 * Created by zhaoruixuan on 2017/4/25.
 */
public class IAudioServiceBinderHook extends BinderHook{

    private final static String SERVICE_NAME= Context.AUDIO_SERVICE;

    public IAudioServiceBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(SERVICE_NAME);
        return IAudioServiceCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IAudioServiceHookHandle(mHostContext);
    }
}
