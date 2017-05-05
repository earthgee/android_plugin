package com.earthgee.library.hook.proxy;

import android.content.Context;

import com.earthgee.library.hook.BaseHookHandle;
import com.earthgee.library.hook.Hook;
import com.earthgee.library.hook.handle.PluginCallback;

import java.util.List;

/**
 * Created by zhaoruixuan on 2017/5/5.
 */
public class PluginCallbackHook extends Hook{
    private List<PluginCallback>

    protected PluginCallbackHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {

    }
}
