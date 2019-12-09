package com.earthgee.pluginlib.hook.binder;

import android.content.Context;

import com.earthgee.pluginlib.hook.BaseHookHandle;

public class IClipboardBinderHook extends BinderHook{
    private final static String CLIPBOARD_SERVICE="clipboard";

    protected IClipboardBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    Object getOldObj() throws Exception {
        return null;
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
