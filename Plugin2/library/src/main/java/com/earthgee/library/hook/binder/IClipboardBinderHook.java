package com.earthgee.library.hook.binder;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;

/**
 * Created by zhaoruixuan on 2017/4/11.
 */
public class IClipboardBinderHook extends BinderHook{

    public IClipboardBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

}
