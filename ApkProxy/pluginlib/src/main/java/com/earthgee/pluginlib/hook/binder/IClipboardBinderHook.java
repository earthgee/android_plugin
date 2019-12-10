package com.earthgee.pluginlib.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.earthgee.pluginlib.helper.compat.IClipboardCompat;
import com.earthgee.pluginlib.helper.compat.MyServiceManager;
import com.earthgee.pluginlib.hook.BaseHookHandle;

public class IClipboardBinderHook extends BinderHook{
    private final static String CLIPBOARD_SERVICE="clipboard";

    public IClipboardBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    Object getOldObj() throws Exception {
        IBinder iBinder= MyServiceManager.getOriginService(CLIPBOARD_SERVICE);
        return IClipboardCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return CLIPBOARD_SERVICE;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

}
