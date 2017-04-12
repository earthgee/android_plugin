package com.earthgee.library.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.handle.IClipboardHookHandle;
import com.earthgee.library.util.IClipboardCompat;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class IClipboardBinderHook extends BinderHook{

    private final static String CLIPBOARD_SERVICE="clipboard";

    public IClipboardBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public String getServiceName() {
        return CLIPBOARD_SERVICE;
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder=MyServiceManager.getOriginService(CLIPBOARD_SERVICE);
        return IClipboardCompat.asInterface(iBinder);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IClipboardHookHandle(mHostContext);
    }

}
